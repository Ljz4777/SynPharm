import json
import logging

from pydantic_settings import BaseSettings, SettingsConfigDict

logger = logging.getLogger(__name__)


class Settings(BaseSettings):
    """SynPharm 算法引擎配置（环境变量 + .env 文件加载）。"""

    model_dir: str = "models/"
    device: str = "cpu"          # 推理设备：cpu / cuda:0（默认 CPU，兼容无 GPU 环境）
    batch_size: int = 50
    # 逗号分隔的 API Key 列表；留空则关闭认证（仅限内网开发）
    # 使用 str 类型存储，避免 pydantic-settings 对 list 复杂类型 env 解析报错
    api_keys: str = ""
    log_level: str = "INFO"
    request_timeout: int = 120
    max_batch_size: int = 1000

    model_config = SettingsConfigDict(
        env_file=".env",
        env_nested_delimiter="__",
        env_ignore_empty=True,
    )

    @property
    def api_key_list(self) -> list[str]:
        """解析 API Key 列表，兼容三种写法。

        历史坑：``.env.example`` 里给的是 JSON 数组 ``["k1","k2"]``，
        而这里只按逗号切分，于是整个字符串被当成一个 Key，照抄模板必然全 401。
        """
        raw = (self.api_keys or "").strip()
        if not raw:
            return []
        if raw.startswith("["):
            try:
                parsed = json.loads(raw)
            except json.JSONDecodeError:
                logger.warning("API_KEYS 以 [ 开头但不是合法 JSON，按逗号分隔处理")
            else:
                if isinstance(parsed, list):
                    return [str(k).strip() for k in parsed if str(k).strip()]
        return [k.strip() for k in raw.split(",") if k.strip()]


settings = Settings()