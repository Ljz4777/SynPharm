from fastapi import APIRouter

from services.algorithm_adapters import get_model_status

router = APIRouter()


@router.get("/")
def health_check():
    """健康检查：区分「进程存活」与「模型就绪」（问题总账 E-01）。

    此前恒返回 ``{"status": "healthy"}``，与真实就绪情况无关。
    后果：权重全缺失时也报 healthy，上游熔断器据此判定引擎正常，
    排障时只能看到"healthy 但所有预测都 404"。

    现在返回两级状态：

    * ``healthy``  —— 三个算法的权重文件都在（未加载不等于异常）
    * ``degraded`` —— 有权重缺失或加载失败，``algorithms`` 里有逐项细节

    HTTP 状态码刻意保持 200：Docker HEALTHCHECK 只通过 ``curl -fsS`` 判断进程存活，
    不能因为某个算法缺权重就把整个容器的健康状态打掉。
    真正需要区分就绪的调用方（后端熔断器）读响应体里的 ``status``。
    """
    models = get_model_status()
    missing = [k for k, v in models.items() if v["state"] == "weights_missing"]
    failed = [k for k, v in models.items() if v["state"] == "load_failed"]

    return {
        "status": "degraded" if (missing or failed) else "healthy",
        "service": "SynPharm AI Prediction Engine",
        "algorithms": models,
        "summary": {
            "ready": [k for k, v in models.items() if v["state"] == "ready"],
            "not_loaded": [k for k, v in models.items() if v["state"] == "not_loaded"],
            "weights_missing": missing,
            "load_failed": failed,
        },
    }
