from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_dir: str = "models/"
    device: str = "cuda:0"
    batch_size: int = 50
    api_keys: list[str] = []
    log_level: str = "INFO"
    request_timeout: int = 120
    max_batch_size: int = 1000

    model_config = SettingsConfigDict(env_file=".env", env_nested_delimiter="__")


settings = Settings()