import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from api.v1 import predict, health
from core.auth import verify_api_key
from core.exceptions import register_exception_handlers
from core.logging_config import setup_logging
from config import settings

setup_logging()
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # ---- 启动 ----
    logger.info("Starting SynPharm AI Prediction Engine...")
    logger.info(f"Settings: device={settings.device}, batch_size={settings.batch_size}")
    logger.info(f"API Key authentication: {'enabled' if settings.api_key_list else 'disabled'}")
    yield
    # ---- 关闭 ----
    logger.info("Shutting down SynPharm AI Prediction Engine...")


app = FastAPI(
    title="SynPharm AI Prediction Engine",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    lifespan=lifespan,
)

# CORS：本服务仅供 SpringBoot 后端内网调用，保持宽松即可；如需浏览器直连请按环境收紧
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

register_exception_handlers(app)

app.include_router(
    predict.router,
    prefix="/v1/predict",
    tags=["predict"],
    dependencies=[Depends(verify_api_key)]
)
app.include_router(
    health.router,
    prefix="/health",
    tags=["health"]
)


@app.get("/")
async def root():
    return {"message": "SynPharm AI Prediction Engine is running"}