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
    
    except InvalidInputError:
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
        
        logger.info(f"Batch prediction completed: algo_type={req.algo_type}, results={len(results)}")
        return {"status": "success", "total": len(results), "results": results}
    
    except InvalidInputError:
        raise
    except Exception as e:
        logger.error(f"Batch prediction failed: {str(e)}", exc_info=True)
        raise PredictionError(f"批量预测失败: {str(e)}")