from fastapi import APIRouter, HTTPException
from core.schemas import (
    SingleRequest, BatchPredictionRequest,
    AlgoResponse, BatchPredictionResponse
)
from services.dti_service import DTIService
from services.ppi_service import PPIService
from services.ddi_service import DDIService
from services.batch_service import BatchPredictor

router = APIRouter()

dti_engine = DTIService()
ppi_engine = PPIService()
ddi_engine = DDIService()
batch_predictor = BatchPredictor()


@router.post("/single", response_model=AlgoResponse)
async def predict_single(req: SingleRequest):
    try:
        if req.algo_type == "DTI":
            if not req.drug_smiles or not req.target_seq:
                raise HTTPException(status_code=400, detail="DTI预测需要drug_smiles和target_seq")
            result = dti_engine.predict({
                "drug_smiles": req.drug_smiles,
                "target_seq": req.target_seq
            })
        elif req.algo_type == "PPI":
            if not req.protein_a or not req.protein_b:
                raise HTTPException(status_code=400, detail="PPI预测需要protein_a和protein_b")
            result = ppi_engine.predict({
                "protein_a": req.protein_a,
                "protein_b": req.protein_b
            })
        elif req.algo_type == "DDI":
            if not req.drug_a or not req.drug_b:
                raise HTTPException(status_code=400, detail="DDI预测需要drug_a和drug_b")
            result = ddi_engine.predict({
                "drug_a": req.drug_a,
                "drug_b": req.drug_b
            })
        else:
            raise HTTPException(status_code=400, detail=f"未知算法类型: {req.algo_type}")

        return {"status": "success", "metrics": result}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"预测失败: {str(e)}")


@router.post("/batch", response_model=BatchPredictionResponse)
async def predict_batch(req: BatchPredictionRequest):
    try:
        data_list = [item.dict() for item in req.data_list]
        results = batch_predictor.run(data_list, req.algo_type)
        return {"status": "success", "total": len(results), "results": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"批量预测失败: {str(e)}")