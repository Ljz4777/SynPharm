from core.loader import ModelLoader
from core.schemas import PredictionMetrics, InteractionInfo
from services.base_algo import BaseAlgo
import torch
import numpy as np
import random


class DTIService(BaseAlgo):

    def __init__(self):
        self.model = ModelLoader().get_model("dti")
        self.device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

    def predict(self, data: dict) -> PredictionMetrics:
        smiles = data.get("drug_smiles", "")
        target_seq = data.get("target_seq", "")

        if self.model is not None:
            molecular_features = self._featurize_smiles(smiles)
            target_features = self._featurize_sequence(target_seq)

            molecular_tensor = torch.tensor(molecular_features, dtype=torch.float32).unsqueeze(0).to(self.device)
            target_tensor = torch.tensor(target_features, dtype=torch.float32).unsqueeze(0).to(self.device)

            with torch.no_grad():
                prediction = self.model(molecular_tensor, target_tensor)

            return self._convert_to_metrics(prediction)
        else:
            return self._generate_mock_result("DTI", smiles)

    def _featurize_smiles(self, smiles: str) -> list:
        features = np.random.rand(128).tolist()
        return features

    def _featurize_sequence(self, seq: str) -> list:
        features = np.random.rand(256).tolist()
        return features

    def _convert_to_metrics(self, prediction) -> PredictionMetrics:
        affinity = prediction["affinity"].item() if isinstance(prediction, dict) else -9.0
        confidence = prediction["confidence"].item() if isinstance(prediction, dict) else 0.9

        level = "high" if confidence >= 0.9 else "medium" if confidence >= 0.8 else "low"

        interactions = [
            InteractionInfo(
                residue=f"ASP{100 + random.randint(0, 100)}",
                type="氢键",
                distance=round(2.8 + random.random() * 0.5, 2)
            ),
            InteractionInfo(
                residue=f"LYS{400 + random.randint(0, 100)}",
                type="疏水作用",
                distance=round(3.5 + random.random() * 1.0, 2)
            )
        ]

        return PredictionMetrics(
            target_id="P00533",
            target_name="EGFR",
            binding_affinity=round(affinity, 2),
            confidence_score=round(confidence, 2),
            confidence_level=level,
            interactions=interactions
        )

    def _generate_mock_result(self, type_name: str, input_data: str) -> PredictionMetrics:
        binding_affinity = -5.0 - random.random() * 10
        confidence_score = 0.7 + random.random() * 0.3
        level = "high" if confidence_score >= 0.9 else "medium" if confidence_score >= 0.8 else "low"

        interactions = [
            InteractionInfo(
                residue=f"ASP{100 + random.randint(0, 100)}",
                type="氢键",
                distance=round(2.8 + random.random() * 0.5, 2)
            )
        ]

        return PredictionMetrics(
            target_id="P00533",
            target_name="EGFR",
            binding_affinity=round(binding_affinity, 2),
            confidence_score=round(confidence_score, 2),
            confidence_level=level,
            interactions=interactions
        )
