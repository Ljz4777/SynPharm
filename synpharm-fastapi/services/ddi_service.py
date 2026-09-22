from core.schemas import PredictionMetrics
from core.base_algo import BaseAlgo
from core.exceptions import InvalidInputError, ModelNotFoundError, OutOfGraphError
from services.algorithm_adapters import DrugNotInGraphError, get_ddi_predictor


class DDIService(BaseAlgo):

    def __init__(self):
        self._predictor = None
        self._loaded = False

    def _get_predictor(self):
        if not self._loaded:
            self._predictor = get_ddi_predictor()
            self._loaded = True
        return self._predictor

    def predict(self, data: dict) -> PredictionMetrics:
        drug_a = data.get("drug_a", "")
        drug_b = data.get("drug_b", "")

        predictor = self._get_predictor()
        if predictor is None:
            # 权重/依赖缺失：直接报错，不再返回 mock
            raise ModelNotFoundError("DDI")

        # 真实推理：图内药物点积 -> sigmoid 概率
        try:
            confidence = predictor(drug_a, drug_b)
        except DrugNotInGraphError as e:
            # 输入合法，只是超出模型能力范围 -> 422，与"格式写错"(400) 区分开
            raise OutOfGraphError(str(e))
        except ValueError as e:
            raise InvalidInputError(str(e))
        return self._to_metrics(drug_a, drug_b, confidence)

    def _to_metrics(self, drug_a: str, drug_b: str, confidence: float) -> PredictionMetrics:
        """组装 DDI 的指标。

        DDI 的预测对象是「药物对」，不存在单一靶点，所以：

        * `target_id` 用药物对本身标识（DrugBank ID 以 `-` 连接），
          与后端 {@code PredictUtils} 中 DDI 的既有约定一致；
          此前硬编码的 `DDI_TARGET` 是一个看起来像真实靶点的伪造标识。
        * `target_name` 保留为人类可读的语义标签，供前端直接展示。
        * `binding_affinity` / `interactions` 对 DDI 无意义，保持为空。
        """
        return PredictionMetrics(
            target_id=f"{drug_a}-{drug_b}",
            target_name="药物相互作用",
            confidence_score=round(confidence, 4),
            confidence_level=_confidence_level(confidence),
            interactions=[]
        )


def _confidence_level(score: float) -> str:
    if score >= 0.8:
        return "high"
    if score >= 0.6:
        return "medium"
    return "low"
