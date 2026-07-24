from pydantic import BaseModel
from typing import List, Optional


class InteractionInfo(BaseModel):
    residue: str
    type: str
    distance: float


class PredictionMetrics(BaseModel):
    target_id: Optional[str] = None
    target_name: Optional[str] = None
    binding_affinity: Optional[float] = None
    confidence_score: float
    confidence_level: str
    interactions: List[InteractionInfo] = []


class AlgoResponse(BaseModel):
    status: str
    metrics: PredictionMetrics


class SingleRequest(BaseModel):
    algo_type: str
    drug_smiles: Optional[str] = None
    target_seq: Optional[str] = None
    protein_a: Optional[str] = None
    protein_b: Optional[str] = None
    drug_a: Optional[str] = None
    drug_b: Optional[str] = None


class BatchItem(BaseModel):
    algo_type: str
    drug_smiles: Optional[str] = None
    target_seq: Optional[str] = None
    protein_a: Optional[str] = None
    protein_b: Optional[str] = None
    drug_a: Optional[str] = None
    drug_b: Optional[str] = None


class BatchPredictionRequest(BaseModel):
    data_list: List[BatchItem]
    algo_type: str


class BatchPredictionResponse(BaseModel):
    status: str
    total: int
    results: List[dict]