"""权重加载适配层：把三个算法（DDI-LLM / KAN-MoDTI / FlashPPI）的真实权重接进引擎。

三个算法的 ``model.py`` 同名，且内部用 ``from model import X`` 相对导入，直接塞进
``sys.path`` 会互相覆盖。这里用 importlib 按**绝对路径 + 唯一模块名**加载，并把目标算法的
``model.py`` 临时注入 ``sys.modules['model']``（KAN-MoDTI 还额外注入 ``kan``），
保证每个算法解析到自己的模块。

三个 getter 都是**懒加载单例**：首次调用才加载权重，加载结果缓存；加载失败
（缺依赖 / 缺权重）缓存 ``None``，上层 service 据此抛 ``ModelNotFoundError``（404），
避免在启动时因某个算法权重没就绪而整体崩溃。

输入非法时抛出 ``ValueError``（不在此处 import core.exceptions，避免把 fastapi 依赖
拖进本模块），由 service 层统一转成 ``InvalidInputError``（HTTP 400）。
"""

from __future__ import annotations

import csv
import importlib.util
import logging
import sys
import time
from pathlib import Path

import torch

from config import settings

logger = logging.getLogger(__name__)

_APP_DIR = Path(__file__).resolve().parent.parent


def _resolve_model_dir() -> Path:
    """模型目录：settings.model_dir 为绝对路径时直接用，否则相对项目根解析。

    原先硬编码为 <项目根>/models，恰好与容器里 MODEL_DIR=/app/models 一致；
    改成读配置后，本地把模型放到别处也能生效。
    """
    raw = Path(settings.model_dir)
    return raw if raw.is_absolute() else (_APP_DIR / raw).resolve()


MODELS_DIR = _resolve_model_dir()

_UNSET = object()
_cache: dict = {}


class DrugNotInGraphError(ValueError):
    """输入合法，但药物不在模型训练图内（DDI-LLM 是转导式模型）。

    继承 ValueError 以保持旧的捕获路径可用，同时让 service 层能单独分流成 422。
    """


def _auto_device() -> str:
    """推理设备：以 settings.device 为准；配置了 cuda 但环境无 GPU 时回退 cpu。"""
    want = (settings.device or "cpu").strip()
    if want.startswith("cuda") and not torch.cuda.is_available():
        logger.warning("配置 device=%s，但当前环境无可用 CUDA，已回退 cpu", want)
        return "cpu"
    return want


def _load_module(module_name: str, file_path: Path):
    """按绝对路径把单个 .py 文件加载为独立模块（不依赖 sys.path 顺序）。"""
    spec = importlib.util.spec_from_file_location(module_name, file_path)
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module
# 加载失败后的冷却时间（秒）。
#
# 此前加载失败会把 None 永久写进 _cache，导致：补上权重后**必须重启容器**才能恢复
# （问题总账 E-02）。现在改为冷却期内不重试、冷却过后自动重试：
#   * 既避免每个请求都触发一次昂贵的加载（PPI 权重 2.8GB）
#   * 又能让"补齐权重/依赖后自愈"
_LOAD_FAIL_TTL = 300.0

# 各算法的权重文件（相对 MODELS_DIR），用于健康检查做**廉价**的就绪探测
_WEIGHT_FILES: dict[str, tuple[str, ...]] = {
    "dti": ("KAN-MoDTI", "weights", "human_final.pth"),
    "ddi": ("DDI-LLM", "weights", "ddi_gcn_morgan.pt"),
    "ppi": ("FlashPPI", "weights", "model.safetensors"),
}

# key -> (失败时刻 monotonic, 错误摘要)
_load_failures: dict[str, tuple[float, str]] = {}


def _get_or_load(key: str, loader):
    """懒加载 + 缓存；加载失败只冷却 `_LOAD_FAIL_TTL` 秒，冷却过后允许重试。"""
    cached = _cache.get(key)
    if cached is not None:
        return cached

    failure = _load_failures.get(key)
    if failure is not None and (time.monotonic() - failure[0]) < _LOAD_FAIL_TTL:
        # 冷却期内直接返回失败，不重复触发昂贵加载
        return None

    try:
        obj = loader()
        err = ""
    except Exception as e:  # noqa: BLE001 —— 兜底，任何异常都视为加载失败
        logger.warning("加载 %s 失败: %s", key, e)
        obj, err = None, f"{type(e).__name__}: {e}"

    if obj is None:
        if not err:
            err = "加载器返回 None（权重缺失或依赖不可用）"
        _load_failures[key] = (time.monotonic(), err)
        logger.warning("标记 %s 加载失败，%d 秒后允许重试: %s", key, int(_LOAD_FAIL_TTL), err)
        return None

    _cache[key] = obj
    _load_failures.pop(key, None)
    return obj


def get_model_status() -> dict:
    """各算法的就绪状态，供 `/health` 使用（问题总账 E-01）。

    刻意**不触发加载**：PPI 权重 2.8GB，而健康检查每 30 秒跑一次，
    真加载会把探活变成重活。这里只做两件廉价的事：

    1. 权重文件是否存在（历史上 PPI 不可用就是权重缺失 / 依赖错位）
    2. 若已尝试过加载，报告其失败原因

    state 取值：
      * ``ready``          —— 已成功加载
      * ``weights_missing``—— 权重文件不存在
      * ``load_failed``    —— 权重在但加载报错（依赖缺失等）
      * ``not_loaded``     —— 权重在、尚未被触发加载（正常，不箨降级）
    """
    status: dict = {}
    for key, parts in _WEIGHT_FILES.items():
        path = MODELS_DIR.joinpath(*parts)
        present = path.exists()
        if _cache.get(key) is not None:
            state = "ready"
        elif not present:
            state = "weights_missing"
        elif key in _load_failures:
            state = "load_failed"
        else:
            state = "not_loaded"
        status[key] = {
            "state": state,
            "weights_path": str(path),
            "weights_present": present,
            "error": (_load_failures.get(key) or (None, None))[1],
        }
    return status


# --------------------------------------------------------------------------- #
# DDI-LLM：转导式 GCN，图内药物点积 -> sigmoid 概率
# --------------------------------------------------------------------------- #
_ddi_drug_index: dict = {}


def _read_drug_names(csv_path: Path) -> dict:
    """读 DDI-LLM 自带的 Drug_description.csv，返回 Drug ID -> Drug Name。"""
    if not csv_path.exists():
        return {}
    names: dict = {}
    try:
        with csv_path.open("r", encoding="utf-8", errors="replace", newline="") as f:
            for row in csv.DictReader(f):
                drug_id = (row.get("Drug ID") or "").strip()
                if drug_id:
                    names[drug_id] = (row.get("Drug Name") or "").strip()
    except Exception as e:  # noqa: BLE001 —— 药名仅供展示，取不到不影响推理
        logger.warning("[DDI-LLM] 读取药名表失败: %s", e)
    return names


def get_ddi_drug_index() -> dict:
    """返回训练图内药物 {DrugBank ID: 药名}；模型未就绪时返回空 dict。

    转导式模型只学到图内节点的 embedding，图外新药数学上无法预测，
    所以这份白名单就是 DDI 的真实能力边界。
    """
    if not _ddi_drug_index:
        get_ddi_predictor()      # 触发懒加载，顺带填充白名单
    return _ddi_drug_index


def _load_ddi_predictor():
    base = MODELS_DIR / "DDI-LLM"
    ckpt_path = base / "weights" / "ddi_gcn_morgan.pt"
    if not ckpt_path.exists():
        logger.warning("[DDI-LLM] 权重缺失: %s", ckpt_path)
        return None

    # 先注册 model 模块，再导入 inference（inference.py 内部 from model import GCN）
    model_mod = _load_module("ddi_llm.model", base / "model.py")
    sys.modules["model"] = model_mod
    try:
        inf = _load_module("ddi_llm.inference", base / "inference.py")
    finally:
        sys.modules.pop("model", None)

    ckpt, z = inf.load_model(str(ckpt_path))
    node_id_map = ckpt["node_id_map"]

    # 填充白名单，供 GET /v1/ddi/drugs 导出能力边界
    global _ddi_drug_index
    _names = _read_drug_names(base / "data" / "Drug_description.csv")
    _ddi_drug_index = {d: _names.get(d, "") for d in sorted(node_id_map.keys())}

    def predict(drug_a: str, drug_b: str) -> float:
        if drug_a not in node_id_map or drug_b not in node_id_map:
            missing = [d for d in (drug_a, drug_b) if d not in node_id_map]
            raise DrugNotInGraphError(
                f"药物不在 DDI-LLM 训练图内（图内共 {len(node_id_map)} 个药物），"
                f"无法预测: {', '.join(missing)}。"
                f"图内药物清单见 GET /v1/ddi/drugs"
            )
        u = node_id_map[drug_a]
        v = node_id_map[drug_b]
        logit = float((z[u] * z[v]).sum().item())
        return float(torch.sigmoid(torch.tensor(logit)).item())

    logger.info("[DDI-LLM] 权重加载完成（%d 个图内药物）", len(node_id_map))
    return predict


def get_ddi_predictor():
    """返回 ``(drug_a, drug_b) -> 概率`` 的可调用对象；加载失败返回 None。"""
    return _get_or_load("ddi", _load_ddi_predictor)


# --------------------------------------------------------------------------- #
# KAN-MoDTI：药物-靶点二分类，KANMoDTIPredictor.predict -> (类别, 概率)
# --------------------------------------------------------------------------- #
def _load_dti_predictor():
    base = MODELS_DIR / "KAN-MoDTI"
    weight = base / "weights" / "human_final.pth"
    if not weight.exists():
        logger.warning("[KAN-MoDTI] 权重缺失: %s", weight)
        return None

    # 依赖链：inference.py -> model.py -> kan.py
    kan_mod = _load_module("kan_modti.kan", base / "kan.py")
    sys.modules["kan"] = kan_mod
    model_mod = _load_module("kan_modti.model", base / "model.py")
    sys.modules["model"] = model_mod
    try:
        inf = _load_module("kan_modti.inference", base / "inference.py")
    finally:
        sys.modules.pop("model", None)
        sys.modules.pop("kan", None)

    predictor = inf.KANMoDTIPredictor(device=_auto_device())
    logger.info("[KAN-MoDTI] 权重加载完成")
    return predictor


def get_dti_predictor():
    """返回 ``KANMoDTIPredictor`` 实例；加载失败返回 None。"""
    return _get_or_load("dti", _load_dti_predictor)


# --------------------------------------------------------------------------- #
# FlashPPI：蛋白-蛋白相互作用，FlashPPIPredictor.predict -> dict
# --------------------------------------------------------------------------- #
def _load_ppi_predictor():
    base = MODELS_DIR / "FlashPPI"
    weight = base / "weights" / "model.safetensors"
    if not weight.exists():
        logger.warning("[FlashPPI] 权重缺失: %s", weight)
        return None

    inf = _load_module("flashppi.inference", base / "inference.py")
    predictor = inf.FlashPPIPredictor(device=_auto_device())
    logger.info("[FlashPPI] 权重加载完成")
    return predictor


def get_ppi_predictor():
    """返回 ``FlashPPIPredictor`` 实例；加载失败返回 None。"""
    return _get_or_load("ppi", _load_ppi_predictor)
