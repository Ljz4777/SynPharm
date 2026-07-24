from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from api.v1 import predict, health

app = FastAPI(title="SynPharm AI Prediction Engine", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(predict.router, prefix="/v1/predict", tags=["predict"])
app.include_router(health.router, prefix="/health", tags=["health"])


@app.get("/")
async def root():
    return {"message": "SynPharm AI Prediction Engine is running"}
