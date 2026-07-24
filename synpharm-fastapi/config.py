from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    model_dir: str = "models/"
    device: str = "cuda:0"
    batch_size: int = 50

    class Config:
        env_file = ".env"


settings = Settings()
