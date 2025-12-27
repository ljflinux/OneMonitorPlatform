from pydantic import BaseModel, Field, ConfigDict
from typing import Optional, List, Dict, Any
from datetime import datetime
from enum import Enum


class CILifecycleStatus(str, Enum):
    PLANNING = "planning"
    ACTIVE = "active"
    MAINTENANCE = "maintenance"
    DECOMMISSIONED = "decommissioned"
    DISPOSED = "disposed"


class RelationType(str, Enum):
    CONTAINS = "contains"
    DEPENDS_ON = "depends_on"
    DEPLOYED_ON = "deployed_on"
    RUNS_ON = "runs_on"
    CONNECTED_TO = "connected_to"
    MANAGED_BY = "managed_by"
    OWNS = "owns"
    PART_OF = "part_of"
    CUSTOM = "custom"


# CI Type Models
class CITypeBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    display_name: str = Field(..., min_length=1, max_length=200)
    description: Optional[str] = None
    is_active: Optional[bool] = True
    parent_type_id: Optional[int] = None


class CITypeCreate(CITypeBase):
    pass


class CITypeUpdate(BaseModel):
    display_name: Optional[str] = Field(None, min_length=1, max_length=200)
    description: Optional[str] = None
    is_active: Optional[bool] = None
    parent_type_id: Optional[int] = None


class CITypeInDBBase(CITypeBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)


class CIType(CITypeInDBBase):
    pass


# CI Attribute Models
class CIAttributeBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    display_name: str = Field(..., min_length=1, max_length=200)
    data_type: str = Field(..., min_length=1, max_length=50)
    is_required: Optional[bool] = False
    default_value: Optional[str] = None
    validation_rule: Optional[str] = None
    is_system: Optional[bool] = False
    ci_type_id: int


class CIAttributeCreate(CIAttributeBase):
    pass


class CIAttributeUpdate(BaseModel):
    display_name: Optional[str] = Field(None, min_length=1, max_length=200)
    data_type: Optional[str] = Field(None, min_length=1, max_length=50)
    is_required: Optional[bool] = None
    default_value: Optional[str] = None
    validation_rule: Optional[str] = None
    is_system: Optional[bool] = None


class CIAttributeInDBBase(CIAttributeBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)


class CIAttribute(CIAttributeInDBBase):
    pass


# CI Data Models
class CIDataBase(BaseModel):
    ci_id: int
    attribute_id: int
    value: Optional[str] = None


class CIDataCreate(CIDataBase):
    pass


class CIDataUpdate(BaseModel):
    value: Optional[str] = None


class CIDataInDBBase(CIDataBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)


class CIData(CIDataInDBBase):
    pass


# CI Models
class CIBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=200)
    ci_type_id: int
    lifecycle_status: Optional[CILifecycleStatus] = CILifecycleStatus.ACTIVE
    description: Optional[str] = None
    serial_number: Optional[str] = None
    asset_number: Optional[str] = None
    owner: Optional[str] = None
    location: Optional[str] = None
    created_by: Optional[str] = None


class CICreate(CIBase):
    attributes: Optional[Dict[str, Any]] = None


class CIUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=200)
    lifecycle_status: Optional[CILifecycleStatus] = None
    description: Optional[str] = None
    serial_number: Optional[str] = None
    asset_number: Optional[str] = None
    owner: Optional[str] = None
    location: Optional[str] = None
    attributes: Optional[Dict[str, Any]] = None


class CIInDBBase(CIBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)


class CI(CIInDBBase):
    attributes: Optional[Dict[str, Any]] = None


# CI Relation Models
class CIRelationBase(BaseModel):
    source_ci_id: int
    target_ci_id: int
    relation_type: RelationType
    custom_relation_type: Optional[str] = None
    description: Optional[str] = None
    attributes: Optional[Dict[str, Any]] = None


class CIRelationCreate(CIRelationBase):
    pass


class CIRelationUpdate(BaseModel):
    relation_type: Optional[RelationType] = None
    custom_relation_type: Optional[str] = None
    description: Optional[str] = None
    attributes: Optional[Dict[str, Any]] = None


class CIRelationInDBBase(CIRelationBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)


class CIRelation(CIRelationInDBBase):
    pass


# CI Change History Models
class CIChangeHistoryBase(BaseModel):
    ci_id: int
    change_type: str = Field(..., min_length=1, max_length=50)
    changed_by: Optional[str] = None
    change_description: Optional[str] = None
    old_values: Optional[Dict[str, Any]] = None
    new_values: Optional[Dict[str, Any]] = None


class CIChangeHistoryCreate(CIChangeHistoryBase):
    pass


class CIChangeHistoryInDBBase(CIChangeHistoryBase):
    id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class CIChangeHistory(CIChangeHistoryInDBBase):
    pass


# Response Models with Relationships
class CITypeWithAttributes(CIType):
    attributes: Optional[List[CIAttribute]] = []


class CIWithRelations(CI):
    source_relations: Optional[List[CIRelation]] = []
    target_relations: Optional[List[CIRelation]] = []