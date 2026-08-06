from pydantic_settings import BaseSettings, SettingsConfigDict


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
        """将逗号分隔的 API Key 字符串解析为列表。"""
        return [k.strip() for k in self.api_keys.split(",") if k.strip()]


settings = Settings()