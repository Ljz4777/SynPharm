from torch import load, device as torch_device, nn
from pathlib import Path
from core.config import settings


class ModelLoader:
    _instance = None
    _models = {}

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._load_models()
        return cls._instance

    def _load_models(self):
        model_dir = Path(settings.model_dir)
        device = torch_device(settings.device)

        if (model_dir / "dti_model.pt").exists():
            self._models["dti"] = load(model_dir / "dti_model.pt", map_location=device)
            self._models["dti"].eval()

        if (model_dir / "ppi_model.pt").exists():
            self._models["ppi"] = load(model_dir / "ppi_model.pt", map_location=device)
            self._models["ppi"].eval()

        if (model_dir / "ddi_model.pt").exists():
            self._models["ddi"] = load(model_dir / "ddi_model.pt", map_location=device)
            self._models["ddi"].eval()

    def get_model(self, model_name: str) -> nn.Module:
        return self._models.get(model_name)

    def has_model(self, model_name: str) -> bool:
        return model_name in self._models
