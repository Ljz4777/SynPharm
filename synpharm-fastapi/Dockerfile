# =============================================================================
# SynPharm FastAPI 算法引擎镜像 - 企业级多阶段构建
#
# 阶段一（deps）：安装 Python 依赖（torch 等大体积依赖隔离在构建层）
# 阶段二（runtime）：精简运行镜像 + 非 root
#
# 构建：docker build -t synpharm/fastapi:1.0.0 .
# =============================================================================

# ---------- 阶段一：安装依赖 ----------
FROM python:3.11-slim AS deps

# 依赖下载源：默认走国内镜像，避免 download.pytorch.org / pypi.org 在受限网络下超时导致构建失败。
# 如需改回官方源，构建时覆盖：--build-arg PIP_INDEX_URL=https://pypi.org/simple/
#                              --build-arg PYTORCH_WHEELS_URL=https://download.pytorch.org/whl/cpu/
ARG PIP_INDEX_URL=https://mirrors.aliyun.com/pypi/simple/
ARG PYTORCH_WHEELS_URL=https://mirrors.aliyun.com/pytorch-wheels/cpu/

WORKDIR /deps

# 先安装 CPU 版 PyTorch（约 185MB，避免拉取 ~2GB 的 CUDA 依赖）
# 注：PyTorch wheel 源是扁平目录（非 PEP503 索引），必须用 --find-links；
#     固定 +cpu 本地版本号可确保不会从 PyPI 误选到 CUDA 版本。
#
# 版本选择依据：
#   * transformers 5.x 硬性要求 torch >= 2.5，2.1.0 会被直接禁用 PyTorch 集成
#     （FlashPPI 依赖 AutoModel，禁用后 PPI 直接不可用）。
#   * 上限刻意停在 2.5.x：torch 2.6 起 torch.load 的 weights_only 默认改为 True，
#     而 DTI/DDI 的权重是整包 pickle，升到 2.6 会直接加载失败。
#
# ⚠️ 顺序很重要：本步骤必须在 COPY requirements.txt 之**前**。
#    torch 只由这一行的版本号决定，与 requirements.txt 无关；
#    若放在 COPY 之后，任何 requirements 改动都会让该层缓存失效，
#    每次都重新下载这 175 MB。
RUN pip install --no-cache-dir --prefix=/install "torch==2.5.1+cpu" \
      --find-links "${PYTORCH_WHEELS_URL}" \
      --index-url "${PIP_INDEX_URL}"

COPY requirements.txt .
# 再安装其余依赖（requirements.txt 已不含 torch）
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt \
      --index-url "${PIP_INDEX_URL}"

# ---------- 阶段二：运行 ----------
FROM python:3.11-slim

# 非 root 用户 + 健康检查工具 curl
RUN useradd --system --uid 10001 --create-home appuser \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 拷贝依赖产物
COPY --from=deps /install /usr/local

# 拷贝应用代码（models/ 不进镜像，由 compose 卷挂载）
COPY . .

# 数据目录（模型、日志），赋予运行用户权限
RUN mkdir -p /app/models /app/logs \
    && chown -R appuser:appuser /app

USER appuser

EXPOSE 8000

# 企业级健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8000/health || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
