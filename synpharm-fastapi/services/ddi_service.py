from core.loader import ModelLoader
from core.schemas import PredictionMetrics, InteractionInfo
from core.base_algo import BaseAlgo
import torch
import numpy as np
import random


class DDIService(BaseAlgo):

    def __init__(self):
        self.model = ModelLoader().get_model("ddi")
        self.device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

    def predict(self, data: dict) -> PredictionMetrics:
        drug_a = data.get("drug_a", "")
        drug_b = data.get("drug_b", "")

        if self.model is not None:
            features_a = self._featurize_smiles(drug_a)
            features_b = self._featurize_smiles(drug_b)

            tensor_a = torch.tensor(features_a, dtype=torch.float32).unsqueeze(0).to(self.device)
            tensor_b = torch.tensor(features_b, dtype=torch.float32).unsqueeze(0).to(self.device)

            with torch.no_grad():
                prediction = self.model(tensor_a, tensor_b)

            return self._convert_to_metrics(prediction)
        else:
            return self._generate_mock_result("DDI")

    def _featurize_smiles(self, smiles: str) -> list:
        features = np.random.rand(128).tolist()
        return features

    def _convert_to_metrics(self, prediction) -> PredictionMetrics:
        confidence = prediction["confidence"].item() if isinstance(prediction, dict) else 0.78
        level = "high" if confidence >= 0.9 else "medium" if confidence >= 0.8 else "low"

        return PredictionMetrics(
            target_id="DDI_TARGET",
            target_name="药物相互作用",
            confidence_score=round(confidence, 2),
            confidence_level=level,
            interactions=[]
        )

    def _generate_mock_result(self, type_name: str) -> PredictionMetrics:
        confidence_score = 0.7 + random.random() * 0.3
        level = "high" if confidence_score >= 0.9 else "medium" if confidence_score >= 0.8 else "low"

        return PredictionMetrics(
            target_id="DDI_TARGET",
            target_name="药物相互作用",
            confidence_score=round(confidence_score, 2),
            confidence_level=level,
            interactions=[]
        )
