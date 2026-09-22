"""DDI 能力边界查询。

DDI-LLM 是**转导式**（transductive）模型：训练时只学到训练图内节点的 embedding，
图外新药在数学上无法预测。所以必须把"能预测哪些药物"这件事显式暴露出来，
否则调用方只会反复收到 400，误以为是自己输入格式写错了。
"""
import logging

from fastapi import APIRouter

from core.schemas import DdiDrug, DdiDrugListResponse
from services.algorithm_adapters import get_ddi_drug_index

logger = logging.getLogger(__name__)

router = APIRouter()


@router.get("/drugs", response_model=DdiDrugListResponse)
async def list_ddi_drugs():
    """列出 DDI 模型训练图内的全部药物（DrugBank ID + 药名）。

    返回 `total: 0` 表示 DDI 权重未就绪（此时 `/v1/predict/single` 的 DDI 分支会返回 404）。
    """
    index = get_ddi_drug_index()
    drugs = [DdiDrug(drug_id=k, drug_name=(v or None)) for k, v in index.items()]
    logger.info("导出 DDI 图内药物白名单: %d 个", len(drugs))
    return DdiDrugListResponse(total=len(drugs), drugs=drugs)
