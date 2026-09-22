import logging
from fastapi import APIRouter, HTTPException
from core.schemas import (
    SingleRequest, BatchPredictionRequest,
    AlgoResponse, BatchPredictionResponse
)
from services.dti_service import DTIService
from services.ppi_service import PPIService
from services.ddi_service import DDIService
from services.batch_service import BatchPredictor
from core.exceptions import PredictionError, InvalidInputError
from config import settings

logger = logging.getLogger(__name__)

router = APIRouter()

dti_engine = DTIService()
ppi_engine = PPIService()
ddi_engine = DDIService()
batch_predictor = BatchPredictor()


@router.post("/single", response_model=AlgoResponse)
async def predict_single(req: SingleRequest):
    logger.info(f"Single prediction request: algo_type={req.algo_type}")
    
    try:
        if req.algo_type == "DTI":
            if not req.drug_smiles or not req.target_seq:
                raise InvalidInputError("DTI预测需要drug_smiles和target_seq")
            result = dti_engine.predict({
                "drug_smiles": req.drug_smiles,
                "target_seq": req.target_seq
            })
        elif req.algo_type == "PPI":
            if not req.protein_a or not req.protein_b:
                raise InvalidInputError("PPI预测需要protein_a和protein_b")
            result = ppi_engine.predict({
                "protein_a": req.protein_a,
                "protein_b": req.protein_b
            })
        elif req.algo_type == "DDI":
            if not req.drug_a or not req.drug_b:
                raise InvalidInputError("DDI预测需要drug_a和drug_b")
            result = ddi_engine.predict({
                "drug_a": req.drug_a,
                "drug_b": req.drug_b
            })
        else:
            raise InvalidInputError(f"未知算法类型: {req.algo_type}")

        logger.info(f"Single prediction completed: algo_type={req.algo_type}")
        return {"status": "success", "metrics": result}
    
    except PredictionError:
        # 400/404/422/503 都是"已经分好类"的错误，必须原样透出：
        # 否则调用方只能看到一个笼统的 500（历史上 404 就这样被吞成 500，
        # 导致"模型未就绪"和"服务真故障"无法区分）。
        raise
    except Exception as e:
        logger.error(f"Prediction failed: {str(e)}", exc_info=True)
        raise PredictionError(f"预测失败: {str(e)}")


@router.post("/batch", response_model=BatchPredictionResponse)
async def predict_batch(req: BatchPredictionRequest):
    logger.info(f"Batch prediction request: algo_type={req.algo_type}, size={len(req.data_list)}")
    
    if len(req.data_list) > settings.max_batch_size:
        raise InvalidInputError(f"批量大小超过限制，最大{settings.max_batch_size}条")
    
    try:
        data_list = [item.dict() for item in req.data_list]
        results = batch_predictor.run(data_list, req.algo_type)

        # 逐条统计成败：此前不管全失败与否都固定返回 status=success，
        # 导致上游把"全批失败 + 只有表头的 CSV"判定成批次 SUCCESS。
        failed = sum(1 for r in results if "error" in r)
        succeeded = len(results) - failed
        if failed == 0:
            status = "success"
        elif succeeded == 0:
            status = "error"
        else:
            status = "partial"

        logger.info(
            "Batch prediction completed: algo_type=%s, total=%d, success=%d, failed=%d, status=%s",
            req.algo_type, len(results), succeeded, failed, status,
        )
        return {
            "status": status,
            "total": len(results),
            "success": succeeded,
            "failed": failed,
            "results": results,
        }

    except PredictionError:
        raise
    except Exception as e:
        logger.error(f"Batch prediction failed: {str(e)}", exc_info=True)
        raise PredictionError(f"批量预测失败: {str(e)}")