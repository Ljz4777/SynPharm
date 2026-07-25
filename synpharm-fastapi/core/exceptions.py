from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from pydantic import ValidationError
import logging

logger = logging.getLogger(__name__)


class PredictionError(Exception):
    def __init__(self, message: str, code: int = 500):
        self.message = message
        self.code = code


class ModelNotFoundError(PredictionError):
    def __init__(self, model_name: str):
        super().__init__(f"Model not found: {model_name}", 404)


class InvalidInputError(PredictionError):
    def __init__(self, message: str):
        super().__init__(message, 400)


def register_exception_handlers(app: FastAPI):
    @app.exception_handler(PredictionError)
    async def prediction_error_handler(request: Request, exc: PredictionError):
        logger.error(f"Prediction error: {exc.message}")
        return JSONResponse(
            status_code=exc.code,
            content={"status": "error", "detail": exc.message}
        )

    @app.exception_handler(RequestValidationError)
    async def validation_error_handler(request: Request, exc: RequestValidationError):
        errors = []
        for error in exc.errors():
            field = ".".join(str(loc) for loc in error["loc"])
            errors.append(f"{field}: {error['msg']}")
        logger.warning(f"Validation error: {', '.join(errors)}")
        return JSONResponse(
            status_code=422,
            content={"status": "error", "detail": ", ".join(errors)}
        )

    @app.exception_handler(Exception)
    async def generic_error_handler(request: Request, exc: Exception):
        logger.error(f"Unexpected error: {str(exc)}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"status": "error", "detail": "Internal server error"}
        )