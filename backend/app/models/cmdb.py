from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, JSON, Boolean, Enum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.db.session import Base
import enum


class CILifecycleStatus(enum.Enum):
    PLANNING = "planning"
    ACTIVE = "active"
    MAINTENANCE = "maintenance"
    DECOMMISSIONED = "decommissioned"
    DISPOSED = "disposed"


class RelationType(enum.Enum):
    CONTAINS = "contains"
    DEPENDS_ON = "depends_on"
    DEPLOYED_ON = "deployed_on"
    RUNS_ON = "runs_on"
    CONNECTED_TO = "connected_to"
    MANAGED_BY = "managed_by"
    OWNS = "owns"
    PART_OF = "part_of"
    CUSTOM = "custom"


class CI_Type(Base):
    __tablename__ = "ci_types"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), unique=True, index=True, nullable=False)
    display_name = Column(String(200), nullable=False)
    description = Column(Text, nullable=True)
    is_active = Column(Boolean, default=True)
    parent_type_id = Column(Integer, ForeignKey("ci_types.id"), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    parent_type = relationship("CI_Type", remote_side=[id])
    attributes = relationship("CI_Attribute", back_populates="ci_type", cascade="all, delete-orphan")
    cis = relationship("CI", back_populates="ci_type", cascade="all, delete-orphan")


class CI_Attribute(Base):
    __tablename__ = "ci_attributes"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    display_name = Column(String(200), nullable=False)
    data_type = Column(String(50), nullable=False)  # string, integer, float, boolean, datetime, json, etc.
    is_required = Column(Boolean, default=False)
    default_value = Column(Text, nullable=True)
    validation_rule = Column(Text, nullable=True)
    ci_type_id = Column(Integer, ForeignKey("ci_types.id"), nullable=False)
    is_system = Column(Boolean, default=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    ci_type = relationship("CI_Type", back_populates="attributes")


class CI(Base):
    __tablename__ = "cis"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, index=True)
    ci_type_id = Column(Integer, ForeignKey("ci_types.id"), nullable=False)
    lifecycle_status = Column(Enum(CILifecycleStatus), default=CILifecycleStatus.ACTIVE, nullable=False)
    description = Column(Text, nullable=True)
    serial_number = Column(String(200), nullable=True, unique=True, index=True)
    asset_number = Column(String(200), nullable=True, unique=True, index=True)
    owner = Column(String(100), nullable=True)
    location = Column(String(200), nullable=True)
    created_by = Column(String(100), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    ci_type = relationship("CI_Type", back_populates="cis")
    ci_data = relationship("CI_Data", back_populates="ci", cascade="all, delete-orphan")
    source_relations = relationship("CI_Relation", foreign_keys="CI_Relation.source_ci_id", back_populates="source_ci", cascade="all, delete-orphan")
    target_relations = relationship("CI_Relation", foreign_keys="CI_Relation.target_ci_id", back_populates="target_ci", cascade="all, delete-orphan")


class CI_Data(Base):
    __tablename__ = "ci_data"
    
    id = Column(Integer, primary_key=True, index=True)
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=False)
    attribute_id = Column(Integer, ForeignKey("ci_attributes.id"), nullable=False)
    value = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    ci = relationship("CI", back_populates="ci_data")
    attribute = relationship("CI_Attribute")


class CI_Relation(Base):
    __tablename__ = "ci_relations"
    
    id = Column(Integer, primary_key=True, index=True)
    source_ci_id = Column(Integer, ForeignKey("cis.id"), nullable=False)
    target_ci_id = Column(Integer, ForeignKey("cis.id"), nullable=False)
    relation_type = Column(Enum(RelationType), nullable=False)
    custom_relation_type = Column(String(100), nullable=True)  # Used when relation_type is CUSTOM
    description = Column(Text, nullable=True)
    attributes = Column(JSON, nullable=True)  # Additional attributes for the relation
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    source_ci = relationship("CI", foreign_keys=[source_ci_id], back_populates="source_relations")
    target_ci = relationship("CI", foreign_keys=[target_ci_id], back_populates="target_relations")


class CI_Change_History(Base):
    __tablename__ = "ci_change_history"
    
    id = Column(Integer, primary_key=True, index=True)
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=False)
    change_type = Column(String(50), nullable=False)  # create, update, delete, status_change
    changed_by = Column(String(100), nullable=True)
    change_description = Column(Text, nullable=True)
    old_values = Column(JSON, nullable=True)
    new_values = Column(JSON, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    ci = relationship("CI")
