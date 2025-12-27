-- CMDB Module Database Initialization Script
-- This script creates all tables and relations for the CMDB module

-- Create enum types
CREATE TYPE ci_lifecycle_status AS ENUM (
    'planning', 
    'active', 
    'maintenance', 
    'decommissioned', 
    'disposed'
);

CREATE TYPE relation_type AS ENUM (
    'contains', 
    'depends_on', 
    'deployed_on', 
    'runs_on', 
    'connected_to', 
    'managed_by', 
    'owns', 
    'part_of', 
    'custom'
);

-- Create CI Types table
CREATE TABLE ci_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    parent_type_id INTEGER REFERENCES ci_types(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ci_types_name ON ci_types(name);
CREATE INDEX idx_ci_types_parent ON ci_types(parent_type_id);

-- Create CI Attributes table
CREATE TABLE ci_attributes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    default_value TEXT,
    validation_rule TEXT,
    ci_type_id INTEGER REFERENCES ci_types(id) NOT NULL,
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ci_attributes_type ON ci_attributes(ci_type_id);

-- Create CI table
CREATE TABLE cis (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    ci_type_id INTEGER REFERENCES ci_types(id) NOT NULL,
    lifecycle_status ci_lifecycle_status DEFAULT 'active',
    description TEXT,
    serial_number VARCHAR(200) UNIQUE,
    asset_number VARCHAR(200) UNIQUE,
    owner VARCHAR(100),
    location VARCHAR(200),
    created_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cis_name ON cis(name);
CREATE INDEX idx_cis_type ON cis(ci_type_id);
CREATE INDEX idx_cis_serial ON cis(serial_number);
CREATE INDEX idx_cis_asset ON cis(asset_number);
CREATE INDEX idx_cis_status ON cis(lifecycle_status);

-- Create CI Data table
CREATE TABLE ci_data (
    id SERIAL PRIMARY KEY,
    ci_id INTEGER REFERENCES cis(id) NOT NULL,
    attribute_id INTEGER REFERENCES ci_attributes(id) NOT NULL,
    value TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ci_data_ci ON ci_data(ci_id);
CREATE INDEX idx_ci_data_attribute ON ci_data(attribute_id);

-- Create CI Relations table
CREATE TABLE ci_relations (
    id SERIAL PRIMARY KEY,
    source_ci_id INTEGER REFERENCES cis(id) NOT NULL,
    target_ci_id INTEGER REFERENCES cis(id) NOT NULL,
    relation_type relation_type NOT NULL,
    custom_relation_type VARCHAR(100),
    description TEXT,
    attributes JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ci_relations_source ON ci_relations(source_ci_id);
CREATE INDEX idx_ci_relations_target ON ci_relations(target_ci_id);
CREATE INDEX idx_ci_relations_type ON ci_relations(relation_type);

-- Create CI Change History table
CREATE TABLE ci_change_history (
    id SERIAL PRIMARY KEY,
    ci_id INTEGER REFERENCES cis(id) NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100),
    change_description TEXT,
    old_values JSONB,
    new_values JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ci_change_history_ci ON ci_change_history(ci_id);
CREATE INDEX idx_ci_change_history_type ON ci_change_history(change_type);
CREATE INDEX idx_ci_change_history_date ON ci_change_history(created_at);

-- Insert initial CI Types
INSERT INTO ci_types (name, display_name, description, is_active) VALUES
('organization', '组织', '企业或组织实体', true),
('location', '位置', '物理或虚拟位置', true),
('datacenter', '数据中心', '物理数据中心', true),
('rack', '机架', '服务器机架', true),
('server', '服务器', '物理或虚拟服务器', true),
('network_device', '网络设备', '网络交换机、路由器等', true),
('storage', '存储设备', '存储系统', true),
('virtualization_host', '虚拟化主机', '虚拟化平台主机', true),
('virtual_machine', '虚拟机', '虚拟服务器实例', true),
('container', '容器', '容器实例', true),
('container_cluster', '容器集群', 'Kubernetes集群等', true),
('application', '应用程序', '业务应用程序', true),
('database', '数据库', '数据库系统', true),
('service', '服务', 'IT服务', true),
('software', '软件', '软件包或程序', true),
('user', '用户', '系统用户', true),
('department', '部门', '组织部门', true);

-- Update datacenter parent type
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'location') WHERE name = 'datacenter';
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'datacenter') WHERE name = 'rack';
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'rack') WHERE name = 'server';
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'server') WHERE name = 'virtualization_host';
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'virtualization_host') WHERE name = 'virtual_machine';
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'virtual_machine') WHERE name = 'container';
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'container') WHERE name = 'container_cluster';
UPDATE ci_types SET parent_type_id = (SELECT id FROM ci_types WHERE name = 'organization') WHERE name = 'department';

-- Insert basic attributes for server CI type
INSERT INTO ci_attributes (name, display_name, data_type, is_required, ci_type_id, is_system) VALUES
('hostname', '主机名', 'string', true, (SELECT id FROM ci_types WHERE name = 'server'), true),
('ip_address', 'IP地址', 'string', true, (SELECT id FROM ci_types WHERE name = 'server'), true),
('mac_address', 'MAC地址', 'string', false, (SELECT id FROM ci_types WHERE name = 'server'), true),
('os_type', '操作系统类型', 'string', true, (SELECT id FROM ci_types WHERE name = 'server'), true),
('os_version', '操作系统版本', 'string', true, (SELECT id FROM ci_types WHERE name = 'server'), true),
('cpu_count', 'CPU数量', 'integer', true, (SELECT id FROM ci_types WHERE name = 'server'), true),
('memory_gb', '内存大小(GB)', 'integer', true, (SELECT id FROM ci_types WHERE name = 'server'), true),
('disk_capacity_gb', '磁盘容量(GB)', 'integer', true, (SELECT id FROM ci_types WHERE name = 'server'), true),
('manufacturer', '制造商', 'string', false, (SELECT id FROM ci_types WHERE name = 'server'), true),
('model', '型号', 'string', false, (SELECT id FROM ci_types WHERE name = 'server'), true);

-- Insert basic attributes for application CI type
INSERT INTO ci_attributes (name, display_name, data_type, is_required, ci_type_id, is_system) VALUES
('app_code', '应用代码', 'string', true, (SELECT id FROM ci_types WHERE name = 'application'), true),
('version', '版本', 'string', true, (SELECT id FROM ci_types WHERE name = 'application'), true),
('environment', '环境', 'string', true, (SELECT id FROM ci_types WHERE name = 'application'), true),
('language', '开发语言', 'string', true, (SELECT id FROM ci_types WHERE name = 'application'), true),
('framework', '框架', 'string', false, (SELECT id FROM ci_types WHERE name = 'application'), true),
('port', '端口', 'integer', true, (SELECT id FROM ci_types WHERE name = 'application'), true),
('url', '访问URL', 'string', false, (SELECT id FROM ci_types WHERE name = 'application'), true),
('business_owner', '业务负责人', 'string', false, (SELECT id FROM ci_types WHERE name = 'application'), true),
('technical_owner', '技术负责人', 'string', true, (SELECT id FROM ci_types WHERE name = 'application'), true);
