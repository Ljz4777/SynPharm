from fastapi import Request, HTTPException
from fastapi.security import APIKeyHeader
from config import settings

api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)


async def verify_api_key(request: Request, api_key: str = api_key_header):
    if not settings.api_keys:
        return

    if api_key not in settings.api_keys:
        raise HTTPException(status_code=401, detail="Unauthorized: Invalid API Key")