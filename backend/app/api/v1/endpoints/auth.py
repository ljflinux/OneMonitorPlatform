from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from app.core.config import settings
from app.crud import crud_user
from app.db.session import get_db
from app.schemas import auth as auth_schemas
from app.utils.security import create_access_token, create_refresh_token, verify_password

router = APIRouter()

@router.post("/login", response_model=auth_schemas.Token)
async def login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db = Depends(get_db)
):
    """用户登录"""
    user = crud_user.get_user_by_username(db, username=form_data.username)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="用户名或密码错误",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    if not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="用户名或密码错误",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token = create_access_token(data={"sub": user.username})
    refresh_token = create_refresh_token(data={"sub": user.username})
    
    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }

@router.post("/refresh", response_model=auth_schemas.Token)
async def refresh_token(token: auth_schemas.RefreshToken):
    """刷新访问令牌"""
    # TODO: 实现令牌刷新逻辑
    pass

@router.post("/logout")
async def logout():
    """用户登出"""
    # TODO: 实现登出逻辑
    pass