from fastapi import APIRouter
from app.api.v1.endpoints import cmdb

api_router = APIRouter()
api_router.include_router(cmdb.router, prefix="/cmdb", tags=["cmdb"])