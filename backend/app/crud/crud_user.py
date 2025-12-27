from sqlalchemy.orm import Session
from app.models.user import User
from app.schemas import user as user_schemas
from app.utils.security import get_password_hash


def get_user(db: Session, user_id: int):
    """根据ID获取用户"""
    return db.query(User).filter(User.id == user_id).first()


def get_user_by_username(db: Session, username: str):
    """根据用户名获取用户"""
    return db.query(User).filter(User.username == username).first()


def get_user_by_email(db: Session, email: str):
    """根据邮箱获取用户"""
    return db.query(User).filter(User.email == email).first()


def get_users(db: Session, skip: int = 0, limit: int = 100):
    """获取用户列表"""
    return db.query(User).offset(skip).limit(limit).all()


def create_user(db: Session, user: user_schemas.UserCreate):
    """创建新用户"""
    hashed_password = get_password_hash(user.password)
    db_user = User(
        username=user.username,
        email=user.email,
        full_name=user.full_name,
        hashed_password=hashed_password
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


def update_user(db: Session, user_id: int, user: user_schemas.UserUpdate):
    """更新用户信息"""
    db_user = db.query(User).filter(User.id == user_id).first()
    if db_user:
        update_data = user.dict(exclude_unset=True)
        if "password" in update_data:
            update_data["hashed_password"] = get_password_hash(update_data.pop("password"))
        for key, value in update_data.items():
            setattr(db_user, key, value)
        db.commit()
        db.refresh(db_user)
    return db_user


def delete_user(db: Session, user_id: int):
    """删除用户"""
    db_user = db.query(User).filter(User.id == user_id).first()
    if db_user:
        db.delete(db_user)
        db.commit()
    return db_user


def toggle_user_status(db: Session, user_id: int, is_active: bool):
    """启用/禁用用户"""
    db_user = db.query(User).filter(User.id == user_id).first()
    if db_user:
        db_user.is_active = is_active
        db.commit()
        db.refresh(db_user)
    return db_user