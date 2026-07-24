from abc import ABC, abstractmethod
from core.schemas import PredictionMetrics


class BaseAlgo(ABC):

    @abstractmethod
    def predict(self, data: dict) -> PredictionMetrics:
        pass

    def _featurize_smiles(self, smiles: str) -> list:
        pass

    def _featurize_sequence(self, seq: str) -> list:
        pass
