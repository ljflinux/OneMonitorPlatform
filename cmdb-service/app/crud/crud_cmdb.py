from sqlalchemy.orm import Session, joinedload
from sqlalchemy import and_
from typing import List, Optional, Dict, Any
from datetime import datetime
from app.models.cmdb import (
    CI_Type, CI_Attribute, CI, CI_Data, CI_Relation, 
    CI_Change_History, CILifecycleStatus, RelationType
)
from app.schemas import cmdb as cmdb_schemas


# CI Type CRUD

def get_ci_type(db: Session, ci_type_id: int) -> Optional[CI_Type]:
    return db.query(CI_Type).filter(CI_Type.id == ci_type_id).first()


def get_ci_type_by_name(db: Session, name: str) -> Optional[CI_Type]:
    return db.query(CI_Type).filter(CI_Type.name == name).first()


def get_ci_types(db: Session, skip: int = 0, limit: int = 100, is_active: Optional[bool] = None) -> List[CI_Type]:
    query = db.query(CI_Type)
    if is_active is not None:
        query = query.filter(CI_Type.is_active == is_active)
    return query.offset(skip).limit(limit).all()


def get_ci_types_with_attributes(db: Session, skip: int = 0, limit: int = 100) -> List[CI_Type]:
    return db.query(CI_Type).options(joinedload(CI_Type.attributes)).offset(skip).limit(limit).all()


def create_ci_type(db: Session, ci_type: cmdb_schemas.CITypeCreate) -> CI_Type:
    db_ci_type = CI_Type(**ci_type.dict())
    db.add(db_ci_type)
    db.commit()
    db.refresh(db_ci_type)
    return db_ci_type


def update_ci_type(db: Session, ci_type_id: int, ci_type: cmdb_schemas.CITypeUpdate) -> Optional[CI_Type]:
    db_ci_type = get_ci_type(db, ci_type_id)
    if db_ci_type:
        update_data = ci_type.dict(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_ci_type, key, value)
        db.commit()
        db.refresh(db_ci_type)
    return db_ci_type


def delete_ci_type(db: Session, ci_type_id: int) -> Optional[CI_Type]:
    db_ci_type = get_ci_type(db, ci_type_id)
    if db_ci_type:
        db.delete(db_ci_type)
        db.commit()
    return db_ci_type


# CI Attribute CRUD

def get_ci_attribute(db: Session, attribute_id: int) -> Optional[CI_Attribute]:
    return db.query(CI_Attribute).filter(CI_Attribute.id == attribute_id).first()


def get_ci_attributes(db: Session, ci_type_id: Optional[int] = None, skip: int = 0, limit: int = 100) -> List[CI_Attribute]:
    query = db.query(CI_Attribute)
    if ci_type_id:
        query = query.filter(CI_Attribute.ci_type_id == ci_type_id)
    return query.offset(skip).limit(limit).all()


def create_ci_attribute(db: Session, attribute: cmdb_schemas.CIAttributeCreate) -> CI_Attribute:
    db_attribute = CI_Attribute(**attribute.dict())
    db.add(db_attribute)
    db.commit()
    db.refresh(db_attribute)
    return db_attribute


def update_ci_attribute(db: Session, attribute_id: int, attribute: cmdb_schemas.CIAttributeUpdate) -> Optional[CI_Attribute]:
    db_attribute = get_ci_attribute(db, attribute_id)
    if db_attribute:
        update_data = attribute.dict(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_attribute, key, value)
        db.commit()
        db.refresh(db_attribute)
    return db_attribute


def delete_ci_attribute(db: Session, attribute_id: int) -> Optional[CI_Attribute]:
    db_attribute = get_ci_attribute(db, attribute_id)
    if db_attribute:
        db.delete(db_attribute)
        db.commit()
    return db_attribute


# CI Data CRUD

def get_ci_data(db: Session, ci_data_id: int) -> Optional[CI_Data]:
    return db.query(CI_Data).filter(CI_Data.id == ci_data_id).first()


def get_ci_data_by_ci_and_attribute(db: Session, ci_id: int, attribute_id: int) -> Optional[CI_Data]:
    return db.query(CI_Data).filter(
        and_(CI_Data.ci_id == ci_id, CI_Data.attribute_id == attribute_id)
    ).first()


def get_ci_data_for_ci(db: Session, ci_id: int) -> List[CI_Data]:
    return db.query(CI_Data).filter(CI_Data.ci_id == ci_id).all()


def create_ci_data(db: Session, ci_data: cmdb_schemas.CIDataCreate) -> CI_Data:
    db_ci_data = CI_Data(**ci_data.dict())
    db.add(db_ci_data)
    db.commit()
    db.refresh(db_ci_data)
    return db_ci_data


def update_ci_data(db: Session, ci_data_id: int, ci_data: cmdb_schemas.CIDataUpdate) -> Optional[CI_Data]:
    db_ci_data = get_ci_data(db, ci_data_id)
    if db_ci_data:
        update_data = ci_data.dict(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_ci_data, key, value)
        db.commit()
        db.refresh(db_ci_data)
    return db_ci_data


def delete_ci_data(db: Session, ci_data_id: int) -> Optional[CI_Data]:
    db_ci_data = get_ci_data(db, ci_data_id)
    if db_ci_data:
        db.delete(db_ci_data)
        db.commit()
    return db_ci_data


# CI CRUD

def get_ci(db: Session, ci_id: int) -> Optional[CI]:
    return db.query(CI).filter(CI.id == ci_id).first()


def get_ci_with_attributes(db: Session, ci_id: int) -> Optional[CI]:
    return db.query(CI).options(joinedload(CI.ci_data).joinedload(CI_Data.attribute)).filter(CI.id == ci_id).first()


def get_ci_with_relations(db: Session, ci_id: int) -> Optional[CI]:
    return db.query(CI).options(
        joinedload(CI.source_relations),
        joinedload(CI.target_relations)
    ).filter(CI.id == ci_id).first()


def get_cis(db: Session, ci_type_id: Optional[int] = None, status: Optional[CILifecycleStatus] = None, skip: int = 0, limit: int = 100) -> List[CI]:
    query = db.query(CI)
    if ci_type_id:
        query = query.filter(CI.ci_type_id == ci_type_id)
    if status:
        query = query.filter(CI.lifecycle_status == status)
    return query.offset(skip).limit(limit).all()


def create_ci(db: Session, ci: cmdb_schemas.CICreate) -> CI:
    attributes = ci.attributes or {}
    ci_data = ci.dict(exclude={'attributes'})
    
    # Create CI
    db_ci = CI(**ci_data)
    db.add(db_ci)
    db.commit()
    db.refresh(db_ci)
    
    # Create CI Data for custom attributes
    ci_type_attributes = get_ci_attributes(db, ci_type_id=ci.ci_type_id)
    for attr in ci_type_attributes:
        if attr.name in attributes:
            create_ci_data(db, cmdb_schemas.CIDataCreate(
                ci_id=db_ci.id,
                attribute_id=attr.id,
                value=str(attributes[attr.name])
            ))
    
    # Log change history
    create_ci_change_history(db, cmdb_schemas.CIChangeHistoryCreate(
        ci_id=db_ci.id,
        change_type="create",
        change_description=f"Created CI: {db_ci.name}",
        new_values=attributes
    ))
    
    return db_ci


def update_ci(db: Session, ci_id: int, ci: cmdb_schemas.CIUpdate) -> Optional[CI]:
    db_ci = get_ci(db, ci_id)
    if not db_ci:
        return None
    
    # Get current CI data for change history
    current_ci_data = {}
    ci_data_entries = get_ci_data_for_ci(db, ci_id)
    for entry in ci_data_entries:
        current_ci_data[entry.attribute.name] = entry.value
    
    # Update CI basic fields
    update_data = ci.dict(exclude={'attributes'}, exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_ci, key, value)
    
    # Update CI attributes
    if ci.attributes:
        # Get all attributes for this CI type
        ci_type_attributes = get_ci_attributes(db, ci_type_id=db_ci.ci_type_id)
        attribute_dict = {attr.name: attr for attr in ci_type_attributes}
        
        for attr_name, attr_value in ci.attributes.items():
            if attr_name in attribute_dict:
                attr = attribute_dict[attr_name]
                # Check if data already exists
                existing_data = get_ci_data_by_ci_and_attribute(db, ci_id, attr.id)
                if existing_data:
                    update_ci_data(db, existing_data.id, cmdb_schemas.CIDataUpdate(value=str(attr_value)))
                else:
                    create_ci_data(db, cmdb_schemas.CIDataCreate(
                        ci_id=ci_id,
                        attribute_id=attr.id,
                        value=str(attr_value)
                    ))
    
    db.commit()
    db.refresh(db_ci)
    
    # Log change history
    create_ci_change_history(db, cmdb_schemas.CIChangeHistoryCreate(
        ci_id=ci_id,
        change_type="update",
        change_description=f"Updated CI: {db_ci.name}",
        old_values=current_ci_data,
        new_values=ci.attributes
    ))
    
    return db_ci


def delete_ci(db: Session, ci_id: int) -> Optional[CI]:
    db_ci = get_ci(db, ci_id)
    if db_ci:
        db.delete(db_ci)
        db.commit()
    return db_ci


def update_ci_status(db: Session, ci_id: int, status: CILifecycleStatus) -> Optional[CI]:
    db_ci = get_ci(db, ci_id)
    if db_ci:
        old_status = db_ci.lifecycle_status
        db_ci.lifecycle_status = status
        db.commit()
        db.refresh(db_ci)
        
        # Log change history
        create_ci_change_history(db, cmdb_schemas.CIChangeHistoryCreate(
            ci_id=ci_id,
            change_type="status_change",
            change_description=f"Changed CI status from {old_status} to {status}",
            old_values={"lifecycle_status": old_status},
            new_values={"lifecycle_status": status}
        ))
    return db_ci


# CI Relation CRUD

def get_ci_relation(db: Session, relation_id: int) -> Optional[CI_Relation]:
    return db.query(CI_Relation).filter(CI_Relation.id == relation_id).first()


def get_ci_relations(db: Session, source_ci_id: Optional[int] = None, target_ci_id: Optional[int] = None, 
                     relation_type: Optional[RelationType] = None, skip: int = 0, limit: int = 100) -> List[CI_Relation]:
    query = db.query(CI_Relation)
    if source_ci_id:
        query = query.filter(CI_Relation.source_ci_id == source_ci_id)
    if target_ci_id:
        query = query.filter(CI_Relation.target_ci_id == target_ci_id)
    if relation_type:
        query = query.filter(CI_Relation.relation_type == relation_type)
    return query.offset(skip).limit(limit).all()


def create_ci_relation(db: Session, relation: cmdb_schemas.CIRelationCreate) -> CI_Relation:
    db_relation = CI_Relation(**relation.dict())
    db.add(db_relation)
    db.commit()
    db.refresh(db_relation)
    return db_relation


def update_ci_relation(db: Session, relation_id: int, relation: cmdb_schemas.CIRelationUpdate) -> Optional[CI_Relation]:
    db_relation = get_ci_relation(db, relation_id)
    if db_relation:
        update_data = relation.dict(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_relation, key, value)
        db.commit()
        db.refresh(db_relation)
    return db_relation


def delete_ci_relation(db: Session, relation_id: int) -> Optional[CI_Relation]:
    db_relation = get_ci_relation(db, relation_id)
    if db_relation:
        db.delete(db_relation)
        db.commit()
    return db_relation


# CI Change History CRUD

def get_ci_change_history(db: Session, ci_id: int, skip: int = 0, limit: int = 100) -> List[CI_Change_History]:
    return db.query(CI_Change_History).filter(CI_Change_History.ci_id == ci_id).order_by(CI_Change_History.created_at.desc()).offset(skip).limit(limit).all()


def create_ci_change_history(db: Session, history: cmdb_schemas.CIChangeHistoryCreate) -> CI_Change_History:
    db_history = CI_Change_History(**history.dict())
    db.add(db_history)
    db.commit()
    db.refresh(db_history)
    return db_history


# Helper functions

def get_ci_attributes_with_values(db: Session, ci_id: int) -> Dict[str, Any]:
    result = {}
    ci_data_entries = get_ci_data_for_ci(db, ci_id)
    for entry in ci_data_entries:
        # Convert value to appropriate type based on attribute data_type
        attr_type = entry.attribute.data_type
        value = entry.value
        
        try:
            if attr_type == "integer":
                value = int(value)
            elif attr_type == "float":
                value = float(value)
            elif attr_type == "boolean":
                value = value.lower() == "true" or value == "1"
            elif attr_type == "datetime":
                value = datetime.fromisoformat(value)
        except (ValueError, TypeError):
            # Keep as string if conversion fails
            pass
        
        result[entry.attribute.name] = value
    
    return result


def search_cis(db: Session, search_term: str, ci_type_id: Optional[int] = None, skip: int = 0, limit: int = 100) -> List[CI]:
    query = db.query(CI)
    if ci_type_id:
        query = query.filter(CI.ci_type_id == ci_type_id)
    
    # Search in CI name and description
    query = query.filter(
        (CI.name.ilike(f"%{search_term}%") | 
         CI.description.ilike(f"%{search_term}%"))
    )
    
    return query.offset(skip).limit(limit).all()