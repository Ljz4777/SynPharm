from fastapi import Request, HTTPException
from fastapi.security import APIKeyHeader
from config import settings

api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)


async def verify_api_key(request: Request, api_key: str = api_key_header):
    valid_keys = settings.api_key_list
    if not valid_keys:
        return

    if api_key not in valid_keys:
        raise HTTPException(status_code=401, detail="Unauthorized: Invalid API Key")