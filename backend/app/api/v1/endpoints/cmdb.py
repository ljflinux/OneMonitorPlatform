from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional, Dict, Any
from app.db.session import get_db
from app.schemas import cmdb as cmdb_schemas
from app.crud import crud_cmdb
from app.models.cmdb import CILifecycleStatus, RelationType

router = APIRouter()


# CI Type Endpoints

@router.get("/ci-types", response_model=List[cmdb_schemas.CIType])
async def get_ci_types(
    skip: int = 0,
    limit: int = 100,
    is_active: Optional[bool] = None,
    db: Session = Depends(get_db)
):
    """获取CI类型列表"""
    return crud_cmdb.get_ci_types(db, skip=skip, limit=limit, is_active=is_active)


@router.get("/ci-types/with-attributes", response_model=List[cmdb_schemas.CITypeWithAttributes])
async def get_ci_types_with_attributes(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取CI类型列表（包含属性）"""
    return crud_cmdb.get_ci_types_with_attributes(db, skip=skip, limit=limit)


@router.get("/ci-types/{ci_type_id}", response_model=cmdb_schemas.CIType)
async def get_ci_type(
    ci_type_id: int,
    db: Session = Depends(get_db)
):
    """获取单个CI类型"""
    db_ci_type = crud_cmdb.get_ci_type(db, ci_type_id)
    if not db_ci_type:
        raise HTTPException(status_code=404, detail="CI类型不存在")
    return db_ci_type


@router.post("/ci-types", response_model=cmdb_schemas.CIType, status_code=status.HTTP_201_CREATED)
async def create_ci_type(
    ci_type: cmdb_schemas.CITypeCreate,
    db: Session = Depends(get_db)
):
    """创建CI类型"""
    # 检查名称是否已存在
    existing = crud_cmdb.get_ci_type_by_name(db, ci_type.name)
    if existing:
        raise HTTPException(status_code=400, detail="CI类型名称已存在")
    return crud_cmdb.create_ci_type(db, ci_type)


@router.put("/ci-types/{ci_type_id}", response_model=cmdb_schemas.CIType)
async def update_ci_type(
    ci_type_id: int,
    ci_type: cmdb_schemas.CITypeUpdate,
    db: Session = Depends(get_db)
):
    """更新CI类型"""
    db_ci_type = crud_cmdb.update_ci_type(db, ci_type_id, ci_type)
    if not db_ci_type:
        raise HTTPException(status_code=404, detail="CI类型不存在")
    return db_ci_type


@router.delete("/ci-types/{ci_type_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_ci_type(
    ci_type_id: int,
    db: Session = Depends(get_db)
):
    """删除CI类型"""
    db_ci_type = crud_cmdb.delete_ci_type(db, ci_type_id)
    if not db_ci_type:
        raise HTTPException(status_code=404, detail="CI类型不存在")
    return None


# CI Attribute Endpoints

@router.get("/ci-attributes", response_model=List[cmdb_schemas.CIAttribute])
async def get_ci_attributes(
    ci_type_id: Optional[int] = Query(None, description="CI类型ID"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取CI属性列表"""
    return crud_cmdb.get_ci_attributes(db, ci_type_id=ci_type_id, skip=skip, limit=limit)


@router.get("/ci-attributes/{attribute_id}", response_model=cmdb_schemas.CIAttribute)
async def get_ci_attribute(
    attribute_id: int,
    db: Session = Depends(get_db)
):
    """获取单个CI属性"""
    db_attribute = crud_cmdb.get_ci_attribute(db, attribute_id)
    if not db_attribute:
        raise HTTPException(status_code=404, detail="CI属性不存在")
    return db_attribute


@router.post("/ci-attributes", response_model=cmdb_schemas.CIAttribute, status_code=status.HTTP_201_CREATED)
async def create_ci_attribute(
    attribute: cmdb_schemas.CIAttributeCreate,
    db: Session = Depends(get_db)
):
    """创建CI属性"""
    # 检查CI类型是否存在
    ci_type = crud_cmdb.get_ci_type(db, attribute.ci_type_id)
    if not ci_type:
        raise HTTPException(status_code=404, detail="CI类型不存在")
    return crud_cmdb.create_ci_attribute(db, attribute)


@router.put("/ci-attributes/{attribute_id}", response_model=cmdb_schemas.CIAttribute)
async def update_ci_attribute(
    attribute_id: int,
    attribute: cmdb_schemas.CIAttributeUpdate,
    db: Session = Depends(get_db)
):
    """更新CI属性"""
    db_attribute = crud_cmdb.update_ci_attribute(db, attribute_id, attribute)
    if not db_attribute:
        raise HTTPException(status_code=404, detail="CI属性不存在")
    return db_attribute


@router.delete("/ci-attributes/{attribute_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_ci_attribute(
    attribute_id: int,
    db: Session = Depends(get_db)
):
    """删除CI属性"""
    db_attribute = crud_cmdb.delete_ci_attribute(db, attribute_id)
    if not db_attribute:
        raise HTTPException(status_code=404, detail="CI属性不存在")
    return None


# CI Endpoints

@router.get("/cis", response_model=List[cmdb_schemas.CI])
async def get_cis(
    ci_type_id: Optional[int] = Query(None, description="CI类型ID"),
    status: Optional[CILifecycleStatus] = Query(None, description="CI生命周期状态"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取CI列表"""
    return crud_cmdb.get_cis(db, ci_type_id=ci_type_id, status=status, skip=skip, limit=limit)


@router.get("/cis/{ci_id}", response_model=cmdb_schemas.CI)
async def get_ci(
    ci_id: int,
    db: Session = Depends(get_db)
):
    """获取单个CI"""
    db_ci = crud_cmdb.get_ci(db, ci_id)
    if not db_ci:
        raise HTTPException(status_code=404, detail="CI不存在")
    
    # Get CI attributes with values
    ci_with_attributes = cmdb_schemas.CI.model_validate(db_ci)
    ci_with_attributes.attributes = crud_cmdb.get_ci_attributes_with_values(db, ci_id)
    
    return ci_with_attributes


@router.get("/cis/{ci_id}/with-relations", response_model=cmdb_schemas.CIWithRelations)
async def get_ci_with_relations(
    ci_id: int,
    db: Session = Depends(get_db)
):
    """获取单个CI（包含关系）"""
    db_ci = crud_cmdb.get_ci_with_relations(db, ci_id)
    if not db_ci:
        raise HTTPException(status_code=404, detail="CI不存在")
    return db_ci


@router.post("/cis", response_model=cmdb_schemas.CI, status_code=status.HTTP_201_CREATED)
async def create_ci(
    ci: cmdb_schemas.CICreate,
    db: Session = Depends(get_db)
):
    """创建CI"""
    # 检查CI类型是否存在
    ci_type = crud_cmdb.get_ci_type(db, ci.ci_type_id)
    if not ci_type:
        raise HTTPException(status_code=404, detail="CI类型不存在")
    return crud_cmdb.create_ci(db, ci)


@router.put("/cis/{ci_id}", response_model=cmdb_schemas.CI)
async def update_ci(
    ci_id: int,
    ci: cmdb_schemas.CIUpdate,
    db: Session = Depends(get_db)
):
    """更新CI"""
    db_ci = crud_cmdb.update_ci(db, ci_id, ci)
    if not db_ci:
        raise HTTPException(status_code=404, detail="CI不存在")
    
    # Get updated CI attributes with values
    ci_with_attributes = cmdb_schemas.CI.model_validate(db_ci)
    ci_with_attributes.attributes = crud_cmdb.get_ci_attributes_with_values(db, ci_id)
    
    return ci_with_attributes


@router.patch("/cis/{ci_id}/status", response_model=cmdb_schemas.CI)
async def update_ci_status(
    ci_id: int,
    status: CILifecycleStatus,
    db: Session = Depends(get_db)
):
    """更新CI状态"""
    db_ci = crud_cmdb.update_ci_status(db, ci_id, status)
    if not db_ci:
        raise HTTPException(status_code=404, detail="CI不存在")
    return db_ci


@router.delete("/cis/{ci_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_ci(
    ci_id: int,
    db: Session = Depends(get_db)
):
    """删除CI"""
    db_ci = crud_cmdb.delete_ci(db, ci_id)
    if not db_ci:
        raise HTTPException(status_code=404, detail="CI不存在")
    return None


@router.get("/cis/{ci_id}/attributes", response_model=Dict[str, Any])
async def get_ci_attributes_values(
    ci_id: int,
    db: Session = Depends(get_db)
):
    """获取CI的属性值"""
    # 检查CI是否存在
    db_ci = crud_cmdb.get_ci(db, ci_id)
    if not db_ci:
        raise HTTPException(status_code=404, detail="CI不存在")
    return crud_cmdb.get_ci_attributes_with_values(db, ci_id)


@router.get("/cis/search", response_model=List[cmdb_schemas.CI])
async def search_cis(
    q: str = Query(..., description="搜索关键词"),
    ci_type_id: Optional[int] = Query(None, description="CI类型ID"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """搜索CI"""
    return crud_cmdb.search_cis(db, search_term=q, ci_type_id=ci_type_id, skip=skip, limit=limit)


# CI Relation Endpoints

@router.get("/relations", response_model=List[cmdb_schemas.CIRelation])
async def get_relations(
    source_ci_id: Optional[int] = Query(None, description="源CI ID"),
    target_ci_id: Optional[int] = Query(None, description="目标CI ID"),
    relation_type: Optional[RelationType] = Query(None, description="关系类型"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取CI关系列表"""
    return crud_cmdb.get_ci_relations(db, source_ci_id=source_ci_id, target_ci_id=target_ci_id, 
                                     relation_type=relation_type, skip=skip, limit=limit)


@router.get("/relations/{relation_id}", response_model=cmdb_schemas.CIRelation)
async def get_relation(
    relation_id: int,
    db: Session = Depends(get_db)
):
    """获取单个CI关系"""
    db_relation = crud_cmdb.get_ci_relation(db, relation_id)
    if not db_relation:
        raise HTTPException(status_code=404, detail="CI关系不存在")
    return db_relation


@router.post("/relations", response_model=cmdb_schemas.CIRelation, status_code=status.HTTP_201_CREATED)
async def create_relation(
    relation: cmdb_schemas.CIRelationCreate,
    db: Session = Depends(get_db)
):
    """创建CI关系"""
    # 检查源CI和目标CI是否存在
    source_ci = crud_cmdb.get_ci(db, relation.source_ci_id)
    target_ci = crud_cmdb.get_ci(db, relation.target_ci_id)
    
    if not source_ci or not target_ci:
        raise HTTPException(status_code=404, detail="源CI或目标CI不存在")
    
    return crud_cmdb.create_ci_relation(db, relation)


@router.put("/relations/{relation_id}", response_model=cmdb_schemas.CIRelation)
async def update_relation(
    relation_id: int,
    relation: cmdb_schemas.CIRelationUpdate,
    db: Session = Depends(get_db)
):
    """更新CI关系"""
    db_relation = crud_cmdb.update_ci_relation(db, relation_id, relation)
    if not db_relation:
        raise HTTPException(status_code=404, detail="CI关系不存在")
    return db_relation


@router.delete("/relations/{relation_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_relation(
    relation_id: int,
    db: Session = Depends(get_db)
):
    """删除CI关系"""
    db_relation = crud_cmdb.delete_ci_relation(db, relation_id)
    if not db_relation:
        raise HTTPException(status_code=404, detail="CI关系不存在")
    return None


# CI Change History Endpoints

@router.get("/cis/{ci_id}/change-history", response_model=List[cmdb_schemas.CIChangeHistory])
async def get_ci_change_history(
    ci_id: int,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取CI变更历史"""
    # 检查CI是否存在
    db_ci = crud_cmdb.get_ci(db, ci_id)
    if not db_ci:
        raise HTTPException(status_code=404, detail="CI不存在")
    return crud_cmdb.get_ci_change_history(db, ci_id=ci_id, skip=skip, limit=limit)
