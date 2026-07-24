from typing import List
from services.dti_service import DTIService
from services.ppi_service import PPIService
from services.ddi_service import DDIService


class BatchPredictor:

    def __init__(self):
        self.dti_engine = DTIService()
        self.ppi_engine = PPIService()
        self.ddi_engine = DDIService()

    def run(self, data_list: List[dict], algo_type: str) -> List[dict]:
        results = []
        engine = self._get_engine(algo_type)

        for item in data_list:
            try:
                result = engine.predict(item)
                result_dict = result.dict()
                result_dict.update(item)
                results.append(result_dict)
            except Exception as e:
                print(f"预测失败: {e}")
                results.append({"error": str(e), **item})

        return results

    def _get_engine(self, algo_type: str):
        if algo_type == "DTI":
            return self.dti_engine
        elif algo_type == "PPI":
            return self.ppi_engine
        elif algo_type == "DDI":
            return self.ddi_engine
        else:
            raise ValueError(f"Unknown algo_type: {algo_type}")
