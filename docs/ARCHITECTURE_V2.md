# OneMonitor 企业级云原生监控平台 - 架构设计文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 版本 | 2.0 |
| 更新日期 | 2024-01-21 |
| 技术栈 | Java + Python 混合架构 |
| 状态 | 设计阶段 |

---

## 1. 项目概述

### 1.1 项目背景

OneMonitor 是一款**企业级云原生运维监控平台**，采用 **Java + Python 混合架构**：
- **Java (核心服务)**: 高性能监控采集、告警引擎、API 网关
- **Python (分析服务)**: 数据分析、机器学习、报表生成
- **CMDB (核心基础)**: 配置项管理、依赖关系、资产追踪

### 1.2 设计目标

| 目标 | 描述 |
|------|------|
| **高性能** | Java 核心服务支撑 100万+ 指标/秒 |
| **可观测性** | 统一 Metrics + Logs + Traces 三位一体 |
| **智能化** | Python AI 分析、异常检测、容量预测 |
| **标准化** | CMDB 驱动监控对象管理 |
| **云原生** | K8s 原生部署，声明式配置 |

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                         OneMonitor 平台                                              │
├─────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                      │
│  ┌────────────────────────────────────────────────────────────────────────────────────────────────┐ │
│  │                                        📊 前端展示层 (Vue 3 + Element Plus)                    │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐           │ │
│  │  │   监控仪表盘    │  │   CMDB 管理     │  │   告警中心      │  │   报表分析      │           │ │
│  │  │  Dashboard     │  │  Configuration  │  │  Alert Center   │  │  Analytics      │           │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘           │ │
│  └────────────────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                │                                                    │
│  ┌────────────────────────────────────────────▼────────────────────────────────────────────────────┐ │
│  │                                   🚪 API Gateway (Spring Cloud Gateway)                         │ │
│  │                                                                                                  │ │
│  │  ┌──────────────────────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                          统一认证 (JWT/OAuth2) + 限流 + 熔断                              │  │ │
│  │  └──────────────────────────────────────────────────────────────────────────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                │                                                    │
│  ┌────────────────────────────────────────────▼────────────────────────────────────────────────────┐ │
│  │                             🔄 服务网格 (Java 微服务 + Python 服务)                             │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────┬───────────────────────────────────────┐              │ │
│  │  │         🟦 Java 核心服务               │         🟨 Python 分析服务             │              │ │
│  │  │                                       │                                       │              │ │
│  │  │  ┌───────────────────────────────┐   │   ┌───────────────────────────────┐   │              │ │
│  │  │  │   monitor-service            │   │   │   analytics-service           │   │              │ │
│  │  │  │   监控采集/指标存储/告警      │   │   │   数据分析/ML模型/报表生成    │   │              │ │
│  │  │  │   (Spring Boot 3.x + R2DBC)  │   │   │   (FastAPI + Pandas + Scikit) │   │              │ │
│  │  │  └───────────────────────────────┘   │   └───────────────────────────────┘   │              │ │
│  │  │                                       │                                       │              │ │
│  │  │  ┌───────────────────────────────┐   │   ┌───────────────────────────────┐   │              │ │
│  │  │  │   cmdb-service               │   │   │   ml-service                  │   │              │ │
│  │  │  │   CI管理/关系/变更追踪       │   │   │   异常检测/容量预测/根因分析  │   │              │ │
│  │  │  │   (Spring Boot 3.x + JPA)   │   │   │   (FastAPI + PyTorch)         │   │              │ │
│  │  │  └───────────────────────────────┘   │   └───────────────────────────────┘   │              │ │
│  │  │                                       │                                       │              │ │
│  │  │  ┌───────────────────────────────┐   │   ┌───────────────────────────────┐   │              │ │
│  │  │  │   alert-service              │   │   │   report-service              │   │              │ │
│  │  │  │   规则引擎/通知发送/升级      │   │   │   报表生成/定时任务/导出      │   │              │ │
│  │  │  │   (Spring Boot 3.x + WebFlux)│   │   │   (FastAPI + Celery)          │   │              │ │
│  │  │  └───────────────────────────────┘   │   └───────────────────────────────┘   │              │ │
│  │  │                                       │                                       │              │ │
│  │  │  ┌───────────────────────────────┐   │   ┌───────────────────────────────┐   │              │ │
│  │  │  │   auth-service               │   │   │   notification-service         │   │              │ │
│  │  │  │   认证/授权/SSO/审计          │   │   │   邮件/企微/钉钉/短信         │   │              │ │
│  │  │  │   (Spring Security 6.x)      │   │   │   (FastAPI + Celery)          │   │              │ │
│  │  │  └───────────────────────────────┘   │   └───────────────────────────────┘   │              │ │
│  │  │                                       │                                       │              │ │
│  │  └───────────────────────────────────────┴───────────────────────────────────────┘              │ │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                │                                                    │
│  ┌────────────────────────────────────────────▼────────────────────────────────────────────────────┐ │
│  │                                    🔌 服务通信层                                                 │ │
│  │                                                                                                  │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐           │ │
│  │  │  REST API      │  │  gRPC (高性能)  │  │  Kafka (异步)   │  │  WebSocket     │           │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘           │ │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────────────────────┐ │
│  │                                    💾 数据存储层                                                 │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────────┬───────────────────────────────────────────┐      │ │
│  │  │           🟦 关系型数据 (PostgreSQL)      │          🟨 分析数据 (ClickHouse)          │      │ │
│  │  │                                               │                                           │      │ │
│  │  │  ┌─────────────────┐  ┌─────────────────┐   │   ┌─────────────────┐  ┌─────────────────┐ │      │ │
│  │  │  │  用户/权限      │  │  CMDB 数据      │   │   │  指标明细       │  │  事件分析       │ │      │ │
│  │  │  │  告警规则       │  │  配置管理       │   │   │  日志分析       │  │  报表数据       │ │      │ │
│  │  │  │  任务计划       │  │  变更记录       │   │   │  追踪数据       │  │  ML 特征        │ │      │ │
│  │  │  └─────────────────┘  └─────────────────┘   │   └─────────────────┘  └─────────────────┘ │      │ │
│  │  └───────────────────────────────────────────┴───────────────────────────────────────────┘      │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                              🟪 缓存层 (Redis Cluster)                                     │  │ │
│  │  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │  │ │
│  │  │  │  会话管理       │  │  配置缓存       │  │  查询缓存       │  │  分布式锁       │     │  │ │
│  │  │  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘     │  │ │
│  │  └───────────────────────────────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                          🟥 时序数据 (VictoriaMetrics Cluster)                             │  │ │
│  │  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │  │ │
│  │  │  │  监控指标       │  │  告警记录       │  │  心跳数据       │  │  降采样数据     │     │  │ │
│  │  │  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘     │  │ │
│  │  └───────────────────────────────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                             🟫 日志数据 (Grafana Loki + MinIO)                             │  │ │
│  │  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                          │  │ │
│  │  │  │  应用日志       │  │  系统日志       │  │  审计日志       │                          │  │ │
│  │  │  └─────────────────┘  └─────────────────┘  └─────────────────┘                          │  │ │
│  │  └───────────────────────────────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                              🟦 链路追踪 (Grafana Tempo + MinIO)                           │  │ │
│  │  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                          │  │ │
│  │  │  │  Trace 数据     │  │  Span 数据      │  │  依赖关系       │                          │  │ │
│  │  │  └─────────────────┘  └─────────────────┘  └─────────────────┘                          │  │ │
│  │  └───────────────────────────────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                                                  │ │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────────────────────┐ │
│  │                                    📡 采集层 (OpenTelemetry)                                   │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                        OTel Agent (DaemonSet/K8s 或主机部署)                               │  │ │
│  │  │                                                                                           │  │ │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │ │
│  │  │  │Prometheus  │  │   OTLP      │  │   Logs      │  │  Traces     │  │  SNMP/IPMI  │   │  │ │
│  │  │  │ Receiver   │  │  Receiver   │  │  Receiver   │  │  Receiver   │  │  Receiver   │   │  │ │
│  │  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘   │  │ │
│  │  │         │                │                │                │                │          │  │ │
│  │  │         ▼                ▼                ▼                ▼                ▼          │  │ │
│  │  │  ┌─────────────────────────────────────────────────────────────────────────────────┐   │  │ │
│  │  │  │  Processors: batch, memory_limiter, k8sattributes, resourcedetection, filter   │   │  │ │
│  │  │  └─────────────────────────────────────────────────────────────────────────────────┘   │  │ │
│  │  │                                        │                                               │  │ │
│  │  │                                        ▼                                               │  │ │
│  │  │  ┌─────────────────────────────────────────────────────────────────────────────────┐   │  │ │
│  │  │  │  Exporters: vmwrite, loki, otlp/tempo, prometheusremote_write                  │   │  │ │
│  │  │  └─────────────────────────────────────────────────────────────────────────────────┘   │  │ │
│  │  └───────────────────────────────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                                                  │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                          OTel Gateway (可选，集中式聚合)                                  │  │ │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                                    │  │ │
│  │  │  │  负载均衡   │  │  数据聚合   │  │  采样控制   │                                    │  │ │
│  │  │  └─────────────┘  └─────────────┘  └─────────────┘                                    │  │ │
│  │  └───────────────────────────────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                                                  │ │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────────────────────┐ │
│  │                                    🖥️ 可视化层 (Grafana)                                       │ │
│  │                                                                                                  │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐           │ │
│  │  │  Dashboards    │  │   Explore      │  │   Alerting     │  │   Logs/Traces  │           │ │
│  │  │  统一仪表盘    │  │   三合一查询    │  │   统一告警      │  │   关联分析      │           │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘           │ │
│  │                                                                                                  │ │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 技术栈总览

#### 🟦 Java 核心服务

| 组件 | 技术选型 | 用途 |
|------|---------|------|
| 框架 | Spring Boot 3.2 + WebFlux | 响应式非阻塞 |
| ORM | Spring Data JPA + R2DBC | 混合同步/响应式 |
| 安全 | Spring Security 6.x + JWT | 认证授权 |
| 网关 | Spring Cloud Gateway | API 网关 |
| 配置 | Spring Cloud Config + Nacos | 配置中心 |
| 注册 | Spring Cloud Consul | 服务发现 |
| 指标 | Micrometer + VictoriaMetrics | 监控指标 |
| 追踪 | OpenTelemetry Java SDK | 链路追踪 |
| 日志 | Logback + Loki4j | 日志采集 |

#### 🟨 Python 分析服务

| 组件 | 技术选型 | 用途 |
|------|---------|------|
| 框架 | FastAPI + Celery | 异步任务 |
| 数据处理 | Pandas + NumPy | 数据分析 |
| 机器学习 | Scikit-learn + PyTorch | 异常检测 |
| 可视化 | Plotly + Dash | 分析图表 |
| 报表 | Jinja2 + WeasyPrint | PDF 报表 |
| 任务队列 | Celery + Redis | 异步任务 |

#### 🟪 基础设施

| 组件 | 技术选型 | 用途 |
|------|---------|------|
| 关系数据库 | PostgreSQL 15 + Patroni | 主数据存储 |
| 时序数据库 | VictoriaMetrics Cluster | 指标存储 |
| 日志系统 | Grafana Loki + MinIO | 日志聚合 |
| 链路追踪 | Grafana Tempo + MinIO | 分布式追踪 |
| 缓存 | Redis Cluster | 缓存/会话 |
| 分析数据库 | ClickHouse (可选) | OLAP 分析 |
| 消息队列 | Apache Kafka | 异步通信 |
| 容器编排 | Kubernetes | 容器编排 |
| CI/CD | GitLab CI + ArgoCD | 持续交付 |

---

## 3. CMDB 核心模块设计

### 3.1 CMDB 在监控平台中的定位

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CMDB 核心地位                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌─────────────────────────────────────────────────────────────────┐   │
│   │                         CMDB (配置管理中心)                       │   │
│   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │   │
│   │  │  资源管理   │  │  关系拓扑   │  │  变更追踪               │  │   │
│   │  │  CI Models  │  │  Relations  │  │  Audit History          │  │   │
│   │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │   │
│   └─────────────────────────────────────────────────────────────────┘   │
│                               │                                           │
│          ┌────────────────────┼────────────────────┐                     │
│          │                    │                    │                     │
│          ▼                    ▼                    ▼                     │
│   ┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐        │
│   │  监控管理   │    │    告警管理     │    │    报表分析     │        │
│   │  (Targets)  │    │   (Alerts)     │    │   (Reports)     │        │
│   │              │    │                 │    │                 │        │
│   │ CI ID 关联   │    │ CI 关联告警     │    │ CI 关联报表     │        │
│   │ 标签驱动采集 │    │ 影响范围分析    │    │ 成本/容量分析   │        │
│   └─────────────┘    └─────────────────┘    └─────────────────┘        │
│                                                                          │
│   ┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐        │
│   │  工单系统   │    │    权限管理     │    │    自动化       │        │
│   │  (Tickets)  │    │   (RBAC)       │    │   (Ansible)     │        │
│   │              │    │                 │    │                 │        │
│   │ CI 关联工单  │    │ CI 级别权限     │    │ 变更自动执行    │        │
│   └─────────────┘    └─────────────────┘    └─────────────────┘        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 CI 数据模型

#### 3.2.1 CI 类型定义

```sql
-- CI 类型表
CREATE TABLE ci_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,           -- 类型标识: server, database, network_device
    display_name VARCHAR(200) NOT NULL,          -- 显示名称: 服务器, 数据库, 网络设备
    description TEXT,
    icon VARCHAR(100),                           -- 图标标识
    color VARCHAR(20),                           -- 颜色标识
    parent_type_id UUID REFERENCES ci_types(id), -- 父类型 (支持层级)
    is_active BOOLEAN DEFAULT TRUE,
    attribute_schema JSONB NOT NULL DEFAULT '{}', -- 属性定义
    metadata JSONB DEFAULT '{}',                  -- 元数据
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 预定义 CI 类型
INSERT INTO ci_types (name, display_name, icon, color, attribute_schema) VALUES
('server', '服务器', 'server', '#409EFF', '{
  "attributes": [
    {"name": "hostname", "type": "string", "required": true, "display_name": "主机名"},
    {"name": "ip_address", "type": "ip", "required": true, "display_name": "IP 地址"},
    {"name": "os", "type": "string", "display_name": "操作系统"},
    {"name": "cpu_cores", "type": "integer", "display_name": "CPU 核心数"},
    {"name": "memory_gb", "type": "integer", "display_name": "内存(G)"},
    {"name": "disk_gb", "type": "integer", "display_name": "磁盘(G)"},
    {"name": "region", "type": "string", "display_name": "区域"},
    {"name": "zone", "type": "string", "display_name": "可用区"}
  ],
  "labels": ["env", "owner", "department"]
}'),

('database', '数据库', 'database', '#67C23A', '{
  "attributes": [
    {"name": "db_name", "type": "string", "required": true, "display_name": "数据库名"},
    {"name": "db_type", "type": "enum", "values": ["MySQL", "PostgreSQL", "Oracle", "MongoDB"], "display_name": "数据库类型"},
    {"name": "version", "type": "string", "display_name": "版本"},
    {"name": "port", "type": "integer", "display_name": "端口"},
    {"name": "connection_pool", "type": "integer", "display_name": "连接池大小"}
  ],
  "labels": ["env", "owner", "department", "db_type"]
}'),

('application', '应用服务', 'application', '#E6A23C', '{
  "attributes": [
    {"name": "app_name", "type": "string", "required": true, "display_name": "应用名称"},
    {"name": "app_type", "type": "enum", "values": ["web", "api", "batch", "service"], "display_name": "应用类型"},
    {"name": "version", "type": "string", "display_name": "版本"},
    {"name": "port", "type": "integer", "display_name": "端口"},
    {"name": "replicas", "type": "integer", "display_name": "副本数"}
  ],
  "labels": ["env", "owner", "app_type", "department"]
}'),

('network_device', '网络设备', 'network', '#909399', '{
  "attributes": [
    {"name": "device_name", "type": "string", "required": true, "display_name": "设备名称"},
    {"name": "device_type", "type": "enum", "values": ["switch", "router", "firewall", "load_balancer"], "display_name": "设备类型"},
    {"name": "vendor", "type": "string", "display_name": "厂商"},
    {"name": "model", "type": "string", "display_name": "型号"},
    {"name": "management_ip", "type": "ip", "required": true, "display_name": "管理IP"}
  ],
  "labels": ["env", "owner", "device_type", "vendor"]
}'),

('middleware', '中间件', 'middleware', '#F56C6C', '{
  "attributes": [
    {"name": "mw_name", "type": "string", "required": true, "display_name": "中间件名称"},
    {"name": "mw_type", "type": "enum", "values": ["redis", "kafka", "elasticsearch", "rabbitmq"], "display_name": "中间件类型"},
    {"name": "version", "type": "string", "display_name": "版本"},
    {"name": "cluster_name", "type": "string", "display_name": "集群名称"}
  ],
  "labels": ["env", "owner", "mw_type", "department"]
}'),

('kubernetes', 'K8s集群', 'kubernetes', '#409EFF', '{
  "attributes": [
    {"name": "cluster_name", "type": "string", "required": true, "display_name": "集群名称"},
    {"name": "kube_version", "type": "string", "display_name": "K8s 版本"},
    {"name": "nodes_count", "type": "integer", "display_name": "节点数"},
    {"name": "master_count", "type": "integer", "display_name": "Master 节点数"},
    {"name": "capacity_cpu", "type": "integer", "display_name": "CPU 总量(核)"},
    {"name": "capacity_memory", "type": "integer", "display_name": "内存总量(G)"}
  ],
  "labels": ["env", "owner", "region"]
}'),

('cloud_resource', '云资源', 'cloud', '#409EFF', '{
  "attributes": [
    {"name": "resource_id", "type": "string", "required": true, "display_name": "资源ID"},
    {"name": "resource_type", "type": "enum", "values": ["ecs", "rds", "slb", "oss", "cdn"], "display_name": "资源类型"},
    {"name": "provider", "type": "enum", "values": ["aliyun", "tencent", "aws", "huawei"], "display_name": "云厂商"},
    {"name": "region", "type": "string", "display_name": "区域"},
    {"name": "specification", "type": "string", "display_name": "规格"},
    {"name": "cost_per_month", "type": "decimal", "display_name": "月成本(元)"}
  ],
  "labels": ["env", "owner", "provider", "resource_type"]
}');
```

#### 3.2.2 CI 实例表

```sql
-- CI 实例表
CREATE TABLE cis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ci_type_id UUID NOT NULL REFERENCES ci_types(id),
    name VARCHAR(500) NOT NULL,                  -- 唯一标识
    display_name VARCHAR(500),                   -- 显示名称
    status VARCHAR(50) DEFAULT 'active',         -- active, inactive, maintenance, decommissioned
    environment VARCHAR(50) DEFAULT 'prod',      -- prod, staging, test, dev
    
    -- 核心属性 (JSON 存储灵活属性)
    attributes JSONB NOT NULL DEFAULT '{}',
    
    -- 标签 (用于监控关联)
    labels JSONB NOT NULL DEFAULT '{}',
    
    -- 元数据
    metadata JSONB DEFAULT '{}',
    
    -- 关系字段
    location VARCHAR(500),                       -- 物理位置
    owner VARCHAR(200),                          -- 负责人
    department VARCHAR(200),                     -- 部门
    business_unit VARCHAR(200),                  -- 业务单元
    
    -- 监控关联
    monitor_enabled BOOLEAN DEFAULT TRUE,
    monitor_tags JSONB DEFAULT '{}',             -- 监控标签映射
    
    -- 审计字段
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(ci_type_id, name)
);

-- 索引
CREATE INDEX idx_cis_type_id ON cis(ci_type_id);
CREATE INDEX idx_cis_status ON cis(status);
CREATE INDEX idx_cis_environment ON cis(environment);
CREATE INDEX idx_cis_labels ON cis USING GIN(labels);
CREATE INDEX idx_cis_owner ON cis(owner);
CREATE INDEX idx_cis_department ON cis(department);
CREATE INDEX idx_cis_created_at ON cis(created_at DESC);
```

#### 3.2.3 CI 关系表

```sql
-- CI 关系类型定义
CREATE TABLE relation_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,           -- contains, depends_on, runs_on, connects_to
    display_name VARCHAR(200) NOT NULL,          -- 包含, 依赖, 部署在, 连接
    description TEXT,
    is_symmetric BOOLEAN DEFAULT FALSE,          -- 是否对称关系
    is_directional BOOLEAN DEFAULT TRUE,         -- 是否有方向
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 预定义关系类型
INSERT INTO relation_types (name, display_name, description, is_directional) VALUES
('contains', '包含', 'A 包含 B', TRUE),
('depends_on', '依赖', 'A 依赖 B', TRUE),
('runs_on', '部署在', 'A 部署在 B 上', TRUE),
('connects_to', '连接', 'A 连接到 B', FALSE),
('belongs_to', '属于', 'A 属于 B', TRUE),
('manages', '管理', 'A 管理 B', TRUE),
('monitors', '监控', 'A 监控 B', TRUE),
('replicates_to', '复制到', 'A 复制到 B', TRUE);

-- CI 关系表
CREATE TABLE ci_relations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    relation_type_id UUID NOT NULL REFERENCES relation_types(id),
    
    -- 源 CI 和目标 CI
    source_ci_id UUID NOT NULL REFERENCES cis(id),
    target_ci_id UUID NOT NULL REFERENCES cis(id),
    
    -- 关系属性
    attributes JSONB DEFAULT '{}',
    description TEXT,
    
    -- 有效期
    valid_from TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    valid_to TIMESTAMP WITH TIME ZONE,
    
    -- 审计
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(relation_type_id, source_ci_id, target_ci_id)
);

-- 索引
CREATE INDEX idx_relations_source ON ci_relations(source_ci_id);
CREATE INDEX idx_relations_target ON ci_relations(target_ci_id);
CREATE INDEX idx_relations_type ON ci_relations(relation_type_id);
```

#### 3.2.4 CI 变更历史表

```sql
-- CI 变更历史表
CREATE TABLE ci_change_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ci_id UUID NOT NULL REFERENCES cis(id),
    change_type VARCHAR(50) NOT NULL,            -- create, update, delete, status_change, relation_change
    change_category VARCHAR(50),                 -- attribute, label, status, relation, metadata
    
    -- 变更内容
    old_values JSONB,                            -- 变更前值
    new_values JSONB,                            -- 变更后值
    change_description TEXT,
    
    -- 变更原因
    change_reason VARCHAR(200),
    ticket_id UUID,                              -- 关联工单
    
    -- 审计
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ip_address VARCHAR(45)
);

-- 索引
CREATE INDEX idx_change_history_ci_id ON ci_change_history(ci_id);
CREATE INDEX idx_change_history_type ON ci_change_history(change_type);
CREATE INDEX idx_change_history_time ON ci_change_history(changed_at DESC);
CREATE INDEX idx_change_history_user ON ci_change_history(changed_by);
```

### 3.3 CI 模型 Java 实现

```java
package com.onemonitor.cmdb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ci_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CIType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String icon;
    private String color;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_type_id")
    private CIType parentType;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attribute_schema", columnDefinition = "jsonb")
    private CIAttributeSchema attributeSchema;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}

@Data
@Builder
public class CIAttributeSchema {
    private java.util.List<CIAttribute> attributes;
    private java.util.List<String> labels;
}

@Data
@Builder
public class CIAttribute {
    private String name;
    private String type;          // string, integer, boolean, datetime, ip, enum, json
    private boolean required;
    private String displayName;
    private Object defaultValue;
    private java.util.List<String> values;  // for enum type
    private String validationRule;
}

@Entity
@Table(name = "cis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CI {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ci_type_id", nullable = false)
    private CIType ciType;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column
    @Builder.Default
    private String status = "active";
    
    @Column
    @Builder.Default
    private String environment = "prod";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> labels = Map.of();
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = Map.of();
    
    private String location;
    private String owner;
    private String department;
    private String businessUnit;
    
    @Column(name = "monitor_enabled")
    @Builder.Default
    private Boolean monitorEnabled = true;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "monitor_tags", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> monitorTags = Map.of();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}

@Entity
@Table(name = "ci_relations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CIRelation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_type_id", nullable = false)
    private RelationType relationType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_ci_id", nullable = false)
    private CI sourceCI;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_ci_id", nullable = false)
    private CI targetCI;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> attributes = Map.of();
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "valid_from")
    @Builder.Default
    private OffsetDateTime validFrom = OffsetDateTime.now();
    
    @Column(name = "valid_to")
    private OffsetDateTime validTo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
```

### 3.4 CMDB 服务 API 设计

```yaml
# CMDB Service API

/api/v1/cmdb:
  # CI 类型管理
  /ci-types:
    GET:    # 获取 CI 类型列表
    POST:   # 创建 CI 类型
    
  /ci-types/{id}:
    GET:    # 获取 CI 类型详情
    PUT:    # 更新 CI 类型
    DELETE: # 删除 CI 类型
    
  # CI 实例管理
  /cis:
    GET:    # 获取 CI 列表 (支持分页、过滤、搜索)
            # Query params: type_id, status, environment, owner, labels, q
            # Response: {items: [], total, page, page_size}
    POST:   # 创建 CI 实例
    
  /cis/{id}:
    GET:    # 获取 CI 详情 (包含属性和关系)
    PUT:    # 更新 CI
    PATCH:  # 部分更新 CI
    DELETE: # 删除 CI
    
  /cis/{id}/attributes:
    GET:    # 获取 CI 属性
    PUT:    # 更新 CI 属性
    
  /cis/{id}/relations:
    GET:    # 获取 CI 关联关系
    POST:   # 添加关联关系
    DELETE: # 删除关联关系
    
  /cis/{id}/history:
    GET:    # 获取 CI 变更历史
    
  /cis/{id}/discover:
    POST:   # 自动发现/同步 CI
    
  /cis/bulk:
    POST:   # 批量操作
    PUT:    # 批量更新
    DELETE: # 批量删除
    
  # 关系管理
  /relations:
    GET:    # 获取关系列表
    POST:   # 创建关系
    
  /relations/{id}:
    GET:    # 获取关系详情
    DELETE: # 删除关系
    
  # 关系类型
  /relation-types:
    GET:    # 获取关系类型列表
    POST:   # 创建关系类型
    
  # 拓扑视图
  /topology:
    GET:    # 获取拓扑图数据
            # Query params: ci_id, depth, relation_types
            # 返回: {nodes: [], links: [], layout: "force"}
    
  # 搜索
  /search:
    GET:    # 搜索 CI
            # Query params: q, type_id, labels, status
    
  # 统计
  /statistics:
    GET:    # 获取统计信息
            # 返回: {type_distribution, status_distribution, environment_distribution}
    
  # 导入导出
  /import:
    POST:   # 导入 CI 数据 (支持 CSV/Excel)
    
  /export:
    GET:    # 导出 CI 数据
            # Query params: type_id, format (csv/json/excel)
```

### 3.5 CMDB 与监控集成

```java
// CI 监控关联服务
@Service
public class CIMonitorIntegrationService {
    
    @Autowired
    private CIRepository ciRepository;
    
    @Autowired
    private VictoriaMetricsService vmService;
    
    @Autowired
    private LabelSyncService labelSyncService;
    
    /**
     * 根据 CI 自动生成监控配置
     */
    public void syncMonitorConfig(CI ci) {
        if (!ci.getMonitorEnabled()) {
            return;
        }
        
        // 1. 生成 Prometheus labels
        Map<String, String> labels = buildPrometheusLabels(ci);
        
        // 2. 更新 VictoriaMetrics 标签
        vmService.updateMetricLabels(
            ci.getId().toString(),
            labels
        );
        
        // 3. 同步到 OTel Collector
        labelSyncService.syncToCollector(ci);
    }
    
    /**
     * 构建 Prometheus 格式的标签
     */
    private Map<String, String> buildPrometheusLabels(CI ci) {
        Map<String, String> labels = new HashMap<>();
        
        // CI 基本信息
        labels.put("ci_id", ci.getId().toString());
        labels.put("ci_name", ci.getName());
        labels.put("ci_type", ci.getCiType().getName());
        labels.put("ci_status", ci.getStatus());
        labels.put("environment", ci.getEnvironment());
        
        // 用户标签
        labels.putAll(ci.getLabels());
        
        // 监控标签
        labels.putAll(ci.getMonitorTags());
        
        // 业务信息
        if (ci.getOwner() != null) {
            labels.put("owner", ci.getOwner());
        }
        if (ci.getDepartment() != null) {
            labels.put("department", ci.getDepartment());
        }
        if (ci.getBusinessUnit() != null) {
            labels.put("business_unit", ci.getBusinessUnit());
        }
        
        return labels;
    }
    
    /**
     * 告警时自动关联 CI 和影响范围分析
     */
    public List<CI> analyzeAlertImpact(CI triggeredCI) {
        // 1. 获取直接依赖该 CI 的所有资源
        List<CIRelation> dependents = ciRelationRepository
            .findBySourceCIAndRelationType(
                triggeredCI,
                RelationType.DEPENDS_ON
            );
        
        // 2. 向上查找所有依赖链
        Set<CI> impactSet = new HashSet<>();
        collectImpactChain(triggeredCI, impactSet, new HashSet<>());
        
        // 3. 返回影响范围
        return new ArrayList<>(impactSet);
    }
    
    /**
     * 递归收集影响链
     */
    private void collectImpactChain(CI ci, Set<CI> impactSet, Set<UUID> visited) {
        if (visited.contains(ci.getId())) {
            return;
        }
        visited.add(ci.getId());
        impactSet.add(ci);
        
        List<CIRelation> dependents = ciRelationRepository
            .findBySourceCIAndRelationType(ci, RelationType.DEPENDS_ON);
        
        for (CIRelation relation : dependents) {
            collectImpactChain(relation.getTargetCI(), impactSet, visited);
        }
    }
}
```

---

## 4. Java 后端架构设计

### 4.1 服务拆分

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Java 微服务架构                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    API Gateway (Spring Cloud Gateway)              │  │
│  │  功能: 路由转发、认证授权、限流熔断、请求日志                        │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                    │                                    │
│         ┌──────────────────────────┼──────────────────────────┐        │
│         │                          │                          │        │
│         ▼                          ▼                          ▼        │
│  ┌─────────────┐          ┌─────────────┐          ┌─────────────┐    │
│  │  auth-      │          │  monitor-   │          │   cmdb-     │    │
│  │  service    │          │  service    │          │  service    │    │
│  │             │          │             │          │             │    │
│  │ 认证/授权   │◄────────►│ 监控采集    │◄────────►│ CMDB 管理   │    │
│  │ JWT/SSO    │   REST   │ 指标存储    │   REST   │ 资源管理    │    │
│  │ 审计日志    │          │ 告警引擎    │          │ 关系拓扑    │    │
│  └─────────────┘          └─────────────┘          └─────────────┘    │
│         │                          │                          │        │
│         │                          │                          │        │
│         ▼                          ▼                          ▼        │
│  ┌─────────────┐          ┌─────────────┐          ┌─────────────┐    │
│  │  alert-     │          │ report-     │          │  gateway-   │    │
│  │  service    │          │  service    │          │  service    │    │
│  │             │          │             │          │             │    │
│  │ 告警规则    │   REST   │ 报表生成    │   REST   │ 外部网关    │    │
│  │ 通知发送    │          │ 定时任务    │          │ 第三方集成  │    │
│  │ 告警升级    │          │ 数据导出    │          │             │    │
│  └─────────────┘          └─────────────┘          └─────────────┘    │
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    公共服务模块 (共享依赖)                           │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────────┐│  │
│  │  │ onemonitor- │  │ onemonitor- │  │  onemonitor-common         ││  │
│  │  │  core       │  │  security   │  │  公共组件: 工具类、异常定义  ││  │
│  │  │  核心配置   │  │  安全模块   │  │                             ││  │
│  │  └─────────────┘  └─────────────┘  └─────────────────────────────┘│  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 目录结构

```
onemonitor-parent/                          # 父 POM
├── pom.xml
│
├── onemonitor-common/                       # 公共模块
│   ├── src/main/java/
│   │   └── com/onemonitor/common/
│   │       ├── exception/                   # 异常定义
│   │       │   ├── BusinessException.java
│   │       │   ├── ErrorCode.java
│   │       │   └── GlobalExceptionHandler.java
│   │       ├── result/                      # 响应封装
│   │       │   ├── Result.java
│   │       │   └── PageResult.java
│   │       ├── utils/                       # 工具类
│   │       │   ├── DateUtils.java
│   │       │   ├── BeanUtils.java
│   │       │   └── ValidationUtils.java
│   │       └── constant/                    # 常量
│   │           └── Constants.java
│   └── src/main/resources/
│       └── application-common.yml
│
├── onemonitor-core/                         # 核心模块
│   ├── src/main/java/
│   │   └── com/onemonitor/core/
│   │       ├── config/                      # 配置类
│   │       │   ├── WebConfig.java
│   │       │   ├── JacksonConfig.java
│   │       │   └── AsyncConfig.java
│   │       ├── trace/                       # 链路追踪
│   │       │   └── TraceContext.java
│   │       └── metrics/                     # 监控指标
│   │           └── OnemonitorMetrics.java
│   └── src/main/resources/
│       └── application-core.yml
│
├── onemonitor-security/                     # 安全模块
│   ├── src/main/java/
│   │   └── com/onemonitor/security/
│   │       ├── config/                      # 安全配置
│   │       │   ├── SecurityConfig.java
│   │       │   └── CorsConfig.java
│   │       ├── filter/                      # 过滤器
│   │       │   ├── JwtAuthenticationFilter.java
│   │       │   └── RateLimitFilter.java
│   │       ├── handler/                     # 处理器
│   │       │   ├── JwtTokenProvider.java
│   │       │   └── AccessDeniedHandler.java
│   │       └── service/                     # 服务
│   │           ├── UserDetailsService.java
│   │           └── AuditLogService.java
│   └── src/main/resources/
│       └── application-security.yml
│
├── onemonitor-api/                          # API 模块 (DTO 定义)
│   ├── src/main/java/
│   │   └── com/onemonitor/api/
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   └── response/
│   │       └── enums/
│   │           └── ResultCode.java
│   └── src/main/resources/
│       └── application-api.yml
│
├── onemonitor-gateway/                      # 网关服务
│   ├── pom.xml (继承父 POM)
│   └── src/main/java/
│       └── com/onemonitor/gateway/
│           ├── GatewayApplication.java
│           ├── config/                      # 网关配置
│           │   ├── RouteConfig.java
│           │   └── RateLimitConfig.java
│           └── filter/                      # 网关过滤器
│               └── AuthFilter.java
│
├── onemonitor-auth/                         # 认证服务
│   ├── pom.xml
│   └── src/main/java/
│       └── com/onemonitor/auth/
│           ├── controller/
│           │   └── AuthController.java
│           ├── service/
│           │   ├── AuthService.java
│           │   └── TokenService.java
│           └── repository/
│               └── UserRepository.java
│
├── onemonitor-cmdb/                         # CMDB 服务
│   ├── pom.xml
│   └── src/main/java/
│       └── com/onemonitor/cmdb/
│           ├── controller/
│           │   ├── CITypeController.java
│           │   ├── CIController.java
│           │   └── RelationController.java
│           ├── service/
│           │   ├── CITypeService.java
│           │   ├── CIService.java
│           │   └── CIMonitorIntegrationService.java
│           ├── repository/
│           │   ├── CITypeRepository.java
│           │   ├── CIRepository.java
│           │   └── CIRelationRepository.java
│           ├── entity/                      # 实体类 (JPA)
│           │   ├── CIType.java
│           │   ├── CI.java
│           │   └── CIRelation.java
│           └── dto/                         # DTO
│               ├── CICreateDTO.java
│               └── CIRelationDTO.java
│
├── onemonitor-monitor/                      # 监控服务
│   ├── pom.xml
│   └── src/main/java/
│       └── com/onemonitor/monitor/
│           ├── controller/
│           │   ├── TargetController.java
│           │   ├── MetricController.java
│           │   └── DashboardController.java
│           ├── service/
│           │   ├── TargetService.java
│           │   ├── MetricService.java
│           │   └── VictoriaMetricsService.java
│           ├── collector/                   # 采集器
│           │   ├── PrometheusCollector.java
│           │   └── OTelCollector.java
│           └── repository/
│               └── TargetRepository.java
│
├── onemonitor-alert/                        # 告警服务
│   ├── pom.xml
│   └── src/main/java/
│       └── com/onemonitor/alert/
│           ├── controller/
│           │   ├── AlertRuleController.java
│           │   └── AlertHistoryController.java
│           ├── service/
│           │   ├── AlertRuleService.java
│           │   ├── AlertEvaluationService.java
│           │   └── NotificationService.java
│           ├── engine/                      # 告警引擎
│           │   ├── RuleEngine.java
│           │   └── PromQLEvaluator.java
│           └── repository/
│               └── AlertRuleRepository.java
│
├── onemonitor-report/                       # 报表服务
│   ├── pom.xml
│   └── src/main/java/
│       └── com/onemonitor/report/
│           ├── controller/
│           │   └── ReportController.java
│           ├── service/
│           │   ├── ReportService.java
│           │   └── ScheduleService.java
│           ├── generator/                   # 报表生成器
│           │   ├── PdfReportGenerator.java
│           │   └── ExcelReportGenerator.java
│           └── scheduler/
│               └── ReportScheduler.java
│
├── onemonitor-notification/                 # 通知服务
│   ├── pom.xml
│   └── src/main/java/
│       └── com/onemonitor/notification/
│           ├── service/
│           │   ├── EmailService.java
│           │   ├── DingTalkService.java
│           │   └── WebhookService.java
│           └── channel/
│               └── NotificationChannel.java
│
└── onemonitor-admin/                        # 管理服务 (可选)
    ├── pom.xml
    └── src/main/java/
        └── com/onemonitor/admin/
            └── controller/
                └── SystemController.java
```

### 4.3 核心 API 设计

#### 4.3.1 认证 API

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @Cacheable(value = "login", key = "#username")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token")
    public Result<LoginResponse> refreshToken(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<UserDTO> getCurrentUser(@CurrentUser UserPrincipal user) {
        return Result.success(authService.getUserById(user.getId()));
    }
}

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String captcha;
    private String captchaKey;
}

@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserDTO user;
}
```

#### 4.3.2 CMDB API

```java
@RestController
@RequestMapping("/api/v1/cmdb/cis")
@RequiredArgsConstructor
@Tag(name = "CI 管理", description = "配置项管理接口")
public class CIController {
    
    private final CIService ciService;
    
    @GetMapping
    @Operation(summary = "获取 CI 列表")
    @PreAuthorize("hasAuthority('cmdb:ci:read')")
    public Result<PageResult<CIResponse>> list(
            @RequestParam(required = false) UUID ciTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) Map<String, String> labels,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return Result.success(ciService.list(
            ciTypeId, status, environment, owner, labels, keyword, pageable
        ));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取 CI 详情")
    @PreAuthorize("hasAuthority('cmdb:ci:read')")
    public Result<CIDetailResponse> getById(@PathVariable UUID id) {
        return Result.success(ciService.getDetail(id));
    }
    
    @PostMapping
    @Operation(summary = "创建 CI")
    @PreAuthorize("hasAuthority('cmdb:ci:write')")
    public Result<CIBasicResponse> create(@Valid @RequestBody CICreateRequest request) {
        return Result.success(ciService.create(request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新 CI")
    @PreAuthorize("hasAuthority('cmdb:ci:write')")
    public Result<CIBasicResponse> update(@PathVariable UUID id, @Valid @RequestBody CIUpdateRequest request) {
        return Result.success(ciService.update(id, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除 CI")
    @PreAuthorize("hasAuthority('cmdb:ci:write')")
    public Result<Void> delete(@PathVariable UUID id) {
        ciService.delete(id);
        return Result.success();
    }
    
    @GetMapping("/{id}/relations")
    @Operation(summary = "获取 CI 关联关系")
    public Result<List<CIRelationResponse>> getRelations(@PathVariable UUID id) {
        return Result.success(ciService.getRelations(id));
    }
    
    @PostMapping("/{id}/relations")
    @Operation(summary = "添加关联关系")
    @PreAuthorize("hasAuthority('cmdb:relation:write')")
    public Result<CIRelationResponse> addRelation(
            @PathVariable UUID id,
            @Valid @RequestBody RelationCreateRequest request) {
        return Result.success(ciService.addRelation(id, request));
    }
    
    @GetMapping("/{id}/history")
    @Operation(summary = "获取 CI 变更历史")
    public Result<PageResult<CIChangeHistoryResponse>> getHistory(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return Result.success(ciService.getChangeHistory(id, pageable));
    }
    
    @GetMapping("/{id}/monitor-config")
    @Operation(summary = "获取监控配置")
    public Result<MonitorConfigResponse> getMonitorConfig(@PathVariable UUID id) {
        return Result.success(ciService.getMonitorConfig(id));
    }
    
    @GetMapping("/topology")
    @Operation(summary = "获取拓扑图数据")
    public Result<TopologyResponse> getTopology(
            @RequestParam(required = false) UUID rootCiId,
            @RequestParam(required = false, defaultValue = "2") Integer depth,
            @RequestParam(required = false) List<String> relationTypes) {
        return Result.success(ciService.getTopology(rootCiId, depth, relationTypes));
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息")
    public Result<CIStatisticsResponse> getStatistics() {
        return Result.success(ciService.getStatistics());
    }
    
    @PostMapping("/bulk")
    @Operation(summary = "批量导入 CI")
    @PreAuthorize("hasAuthority('cmdb:ci:write')")
    public Result<BulkImportResult> bulkImport(@Valid @RequestBody BulkImportRequest request) {
        return Result.success(ciService.bulkImport(request));
    }
}
```

---

## 5. Python 分析服务架构

### 5.1 服务拆分

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       Python 分析服务架构                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    FastAPI 应用层                                   │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │  │
│  │  │  REST API      │  │  WebSocket     │  │  GraphQL (可选) │    │  │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘    │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                    │                                    │
│                                    ▼                                    │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      服务层 (Services)                              │  │
│  │                                                                      │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │  │
│  │  │ analytics-      │  │  ml-service    │  │ report-service │    │  │
│  │  │  service        │  │                │  │                │    │  │
│  │  │  时序分析       │  │  机器学习服务  │  │  报表生成服务  │    │  │
│  │  │  异常检测       │  │  容量预测      │  │  数据导出      │    │  │
│  │  │  根因分析       │  │  根因定位      │  │  定时调度      │    │  │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘    │  │
│  │                                                                      │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                    │                                    │
│                                    ▼                                    │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      任务队列 (Celery)                              │  │
│  │                                                                      │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │  │
│  │  │  Celery Beat   │  │  Worker Nodes  │  │  Flower (监控)  │    │  │
│  │  │  定时调度      │  │  任务执行      │  │  任务监控      │    │  │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘    │  │
│  │                                                                      │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                    │                                    │
│                                    ▼                                    │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      数据访问层                                      │  │
│  │                                                                      │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │  │
│  │  │  ClickHouse    │  │  VictoriaMetrics│  │   PostgreSQL   │    │  │
│  │  │  OLAP 分析     │  │  时序查询       │  │  元数据读取    │    │  │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘    │  │
│  │                                                                      │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 目录结构

```
onemonitor-analytics/
├── app/
│   ├── main.py                              # FastAPI 应用入口
│   ├── config.py                            # 配置管理
│   │
│   ├── api/                                 # API 层
│   │   ├── router.py                        # 路由汇总
│   │   ├── deps.py                          # 依赖注入
│   │   └── v1/
│   │       ├── analytics.py                 # 分析 API
│   │       ├── ml.py                        # ML API
│   │       └── reports.py                   # 报表 API
│   │
│   ├── services/                            # 服务层
│   │   ├── __init__.py
│   │   ├── analytics_service.py             # 分析服务
│   │   ├── anomaly_detection.py             # 异常检测
│   │   ├── capacity_prediction.py           # 容量预测
│   │   ├── root_cause_analysis.py           # 根因分析
│   │   └── report_service.py                # 报表服务
│   │
│   ├── ml/                                  # 机器学习模块
│   │   ├── __init__.py
│   │   ├── models/
│   │   │   ├── anomaly_detector.py          # 异常检测模型
│   │   │   ├── trend_predictor.py           # 趋势预测模型
│   │   │   └── seasonality_detector.py      # 周期检测模型
│   │   ├── training/
│   │   │   └── train_anomaly_model.py       # 模型训练
│   │   └── inference/
│   │       └── predict.py                   # 推理服务
│   │
│   ├── tasks/                               # Celery 任务
│   │   ├── __init__.py
│   │   ├── celery_app.py                    # Celery 配置
│   │   ├── analytics_tasks.py               # 分析任务
│   │   └── report_tasks.py                  # 报表任务
│   │
│   ├── clients/                             # 外部服务客户端
│   │   ├── __init__.py
│   │   ├── victoria_metrics_client.py       # VM 客户端
│   │   ├── clickhouse_client.py             # ClickHouse 客户端
│   │   └── grafana_client.py                # Grafana API 客户端
│   │
│   ├── schemas/                             # Pydantic 模型
│   │   ├── __init__.py
│   │   ├── analytics.py                     # 分析相关
│   │   ├── ml.py                            # ML 相关
│   │   └── report.py                        # 报表相关
│   │
│   └── utils/                               # 工具函数
│       ├── __init__.py
│       ├── date_utils.py                    # 日期处理
│       ├── promql_utils.py                  # PromQL 工具
│       └── logger.py                        # 日志配置
│
├── tests/
│   ├── test_analytics/
│   └── test_ml/
│
├── requirements.txt
├── Dockerfile
└── README.md
```

### 5.3 核心功能实现

```python
# app/services/anomaly_detection.py
from datetime import datetime, timedelta
from typing import List, Dict, Optional
import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler

class AnomalyDetectionService:
    """异常检测服务"""
    
    def __init__(self):
        self.model = IsolationForest(
            contamination=0.01,  # 1% 异常率假设
            n_estimators=100,
            random_state=42
        )
        self.scaler = StandardScaler()
    
    def detect_from_vm(self, metric_name: str, query: str, 
                       start_time: datetime, end_time: datetime) -> List[Dict]:
        """从 VictoriaMetrics 获取数据并检测异常"""
        # 1. 查询数据
        data = self._query_vm(metric_name, query, start_time, end_time)
        
        if len(data) < 100:
            return []
        
        # 2. 准备特征
        features = self._prepare_features(data)
        
        # 3. 检测异常
        predictions = self.model.fit_predict(features)
        scores = self.model.decision_function(features)
        
        # 4. 提取异常点
        anomalies = []
        for i, (pred, score) in enumerate(zip(predictions, scores)):
            if pred == -1:  # -1 表示异常
                anomalies.append({
                    'timestamp': data[i]['timestamp'],
                    'value': data[i]['value'],
                    'score': float(score),
                    'severity': self._calculate_severity(score)
                })
        
        return anomalies
    
    def _prepare_features(self, data: List[Dict]) -> np.ndarray:
        """准备特征矩阵"""
        values = np.array([d['value'] for d in data])
        
        # 计算滑动窗口特征
        window = 10
        rolling_mean = pd.Series(values).rolling(window).mean().fillna(method='bfill').values
        rolling_std = pd.Series(values).rolling(window).std().fillna(method='bfill').values
        
        # 组合特征
        features = np.column_stack([
            values,                    # 原始值
            rolling_mean,             # 滑动均值
            rolling_std,              # 滑动标准差
            (values - rolling_mean),  # 偏离均值
            np.abs(values - rolling_mean) / (rolling_std + 1e-8)  # Z-score
        ])
        
        return self.scaler.fit_transform(features)
    
    def _calculate_severity(self, score: float) -> str:
        """根据异常分数计算严重程度"""
        if score < -0.5:
            return 'critical'
        elif score < -0.3:
            return 'warning'
        else:
            return 'info'
```

---

## 6. 前端架构设计

### 6.1 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | 3.4+ |
| 构建工具 | Vite | 5.0+ |
| UI 组件 | Element Plus | 2.5+ |
| 状态管理 | Pinia | 2.1+ |
| 图表 | Apache ECharts | 5.4+ |
| 图拓扑 | AntV G6 | 5.0+ |
| HTTP | Axios | 1.6+ |

### 6.2 核心页面

```
前端页面结构:

src/views/
├── login/                          # 登录页
│   └── index.vue
│
├── dashboard/                      # 仪表盘
│   ├── index.vue                   # 仪表盘主页
│   ├── components/
│   │   ├── OverviewCard.vue        # 概览卡片
│   │   ├── MetricChart.vue         # 指标图表
│   │   └── AlertSummary.vue        # 告警汇总
│   └── views/
│       ├── overview.vue            # 系统总览
│       └── custom.vue              # 自定义视图
│
├── monitoring/                     # 监控管理
│   ├── index.vue
│   ├── components/
│   │   ├── TargetList.vue          # 目标列表
│   │   └── MetricChart.vue         # 指标图表
│   └── views/
│       ├── targets/                # 监控对象
│       │   ├── index.vue           # 列表
│       │   ├── Detail.vue          # 详情
│       │   └── Create.vue          # 创建
│       └── metrics/                # 指标浏览
│           └── index.vue
│
├── cmdb/                           # CMDB 核心模块
│   ├── index.vue
│   ├── components/
│   │   ├── CITree.vue              # CI 树形结构
│   │   ├── CIForm.vue              # CI 表单
│   │   ├── CIRelationGraph.vue     # 关系拓扑图
│   │   ├── CIAttributeEditor.vue   # 属性编辑器
│   │   └── CIMonitorStatus.vue     # 监控状态
│   └── views/
│       ├── resources/              # 资源管理
│       │   ├── index.vue           # CI 列表
│       │   ├── Detail.vue          # CI 详情
│       │   ├── Create.vue          # 创建 CI
│       │   └── Edit.vue            # 编辑 CI
│       ├── topology/               # 拓扑视图
│       │   └── index.vue           # 拓扑图
│       ├── relations/              # 关系管理
│       │   └── index.vue
│       └── history/                # 变更历史
│           └── index.vue
│
├── alerts/                         # 告警中心
│   ├── index.vue
│   └── views/
│       ├── rules/                  # 告警规则
│       ├── history/                # 告警历史
│       └── channels/               # 通知渠道
│
├── reports/                        # 报表中心
│   └── index.vue
│
└── settings/                       # 系统设置
    └── index.vue
```

### 6.3 CMDB 前端组件

```vue
<!-- src/views/cmdb/components/CIRelationGraph.vue -->
<template>
  <div class="relation-graph">
    <div class="graph-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索节点"
        clearable
        style="width: 200px"
      />
      <el-select v-model="selectedTypes" multiple placeholder="选择 CI 类型">
        <el-option
          v-for="type in ciTypes"
          :key="type.id"
          :label="type.displayName"
          :value="type.id"
        />
      </el-select>
      <el-button @click="expandAll">展开全部</el-button>
      <el-button @click="collapseAll">收起全部</el-button>
    </div>
    
    <div ref="graphContainer" class="graph-container"></div>
    
    <!-- 节点详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="节点详情" size="400px">
      <template v-if="selectedNode">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="名称">
            {{ selectedNode.name }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            {{ selectedNode.ciTypeName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedNode.status)">
              {{ selectedNode.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">
            {{ selectedNode.owner || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider>关联关系</el-divider>
        <el-table :data="selectedNode.relations" size="small">
          <el-table-column prop="type" label="关系类型" />
          <el-table-column prop="targetName" label="目标" />
        </el-table>
        
        <el-divider>操作</el-divider>
        <el-button type="primary" @click="viewDetail(selectedNode)">
          查看详情
        </el-button>
        <el-button @click="navigateToTopology(selectedNode)">
          查看拓扑
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import G6 from '@antv/g6'
import { useRouter } from 'vue-router'

const router = useRouter()
const graphContainer = ref(null)
const searchKeyword = ref('')
const selectedTypes = ref([])
const drawerVisible = ref(false)
const selectedNode = ref(null)

let graph = null

const ciTypes = ref([])  // CI 类型列表
const graphData = ref({ nodes: [], links: [] })

// 初始化图
onMounted(() => {
  initGraph()
  loadData()
})

const initGraph = () => {
  graph = new G6.Graph({
    container: graphContainer.value,
    width: graphContainer.value?.clientWidth || 800,
    height: 600,
    fitView: true,
    fitViewPadding: 50,
    layout: {
      type: 'force',
      preventOverlap: true,
      nodeSize: 60,
      linkDistance: 150,
      alphaDecay: 0.028,
      onTick: () => {}
    },
    modes: {
      default: ['drag-canvas', 'zoom-canvas', 'drag-node']
    },
    defaultNode: {
      type: 'circle',
      size: 60,
      style: {
        fill: '#e6f7ff',
        stroke: '#1890ff',
        lineWidth: 2
      },
      labelCfg: {
        style: {
          fill: '#000',
          fontSize: 12
        }
      }
    },
    defaultEdge: {
      type: 'cubic-horizontal',
      style: {
        stroke: '#b5b5b5',
        endArrow: true
      }
    }
  })
  
  graph.on('node:click', (evt) => {
    const node = evt.item.getModel()
    selectedNode.value = node
    drawerVisible.value = true
  })
}

// 加载数据
const loadData = async () => {
  const params = {
    depth: 2,
    ci_type_ids: selectedTypes.value.join(',')
  }
  const response = await fetch(`/api/v1/cmdb/topology?${new URLSearchParams(params)}`)
  const data = await response.json()
  graphData.value = data.data
  
  renderGraph()
}

// 渲染图
const renderGraph = () => {
  const filteredNodes = graphData.value.nodes.filter(node => {
    if (searchKeyword.value && !node.label.includes(searchKeyword.value)) {
      return false
    }
    if (selectedTypes.value.length > 0 && !selectedTypes.value.includes(node.ciTypeId)) {
      return false
    }
    return true
  })
  
  const nodeIds = new Set(filteredNodes.map(n => n.id))
  const filteredLinks = graphData.value.links.filter(link => {
    return nodeIds.has(link.source) && nodeIds.has(link.target)
  })
  
  graph.data({
    nodes: filteredNodes.map(node => ({
      id: node.id,
      label: node.label || node.name,
      ciTypeName: node.ciTypeName,
      status: node.status,
      owner: node.owner,
      relations: node.relations || [],
      style: {
        fill: getNodeColor(node.ciTypeName),
        stroke: getStatusColor(node.status)
      }
    })),
    edges: filteredLinks.map(link => ({
      source: link.source,
      target: link.target,
      label: link.relationType
    }))
  })
  
  graph.render()
}

// 根据 CI 类型获取颜色
const getNodeColor = (ciTypeName) => {
  const colors = {
    '服务器': '#409EFF',
    '数据库': '#67C23A',
    '应用服务': '#E6A23C',
    '中间件': '#F56C6C',
    '网络设备': '#909399'
  }
  return colors[ciTypeName] || '#909399'
}

// 根据状态获取颜色
const getStatusColor = (status) => {
  const colors = {
    'active': '#67C23A',
    'inactive': '#909399',
    'maintenance': '#E6A23C',
    'fault': '#F56C6C'
  }
  return colors[status] || '#909399'
}

const getStatusType = (status) => {
  const types = {
    'active': 'success',
    'inactive': 'info',
    'maintenance': 'warning',
    'fault': 'danger'
  }
  return types[status] || 'info'
}

const viewDetail = (node) => {
  router.push(`/cmdb/resources/${node.id}`)
}

const navigateToTopology = (node) => {
  router.push(`/cmdb/topology?root=${node.id}`)
}

const expandAll = () => {
  // 展开所有节点
}

const collapseAll = () => {
  // 收起所有节点
}

// 监听搜索和过滤
watch([searchKeyword, selectedTypes], () => {
  renderGraph()
})
</script>

<style scoped>
.relation-graph {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.graph-header {
  padding: 10px;
  display: flex;
  gap: 10px;
  background: #f5f7fa;
}

.graph-container {
  flex: 1;
  min-height: 500px;
}
</style>
```

---

## 7. 部署架构

### 7.1 Docker Compose 配置

```yaml
version: '3.8'

services:
  # ==================== 基础设施 ====================
  
  postgres:
    image: postgres:15-alpine
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./sql/init:/docker-entrypoint-initdb.d
    environment:
      - POSTGRES_DB=onemonitor
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=admin123
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d onemonitor"]
    deploy:
      resources:
        limits:
          memory: 1G
  
  redis:
    image: redis:7-alpine
    command: redis-server --requirepass onemonitor123 --maxmemory 512mb --maxmemory-policy allkeys-lru
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
    deploy:
      resources:
        limits:
          memory: 512M
  
  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=admin
      - MINIO_ROOT_PASSWORD=admin123
    volumes:
      - minio_data:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
  
  # ==================== 可观测性组件 ====================
  
  victoriametrics:
    image: victoriametrics/victoria-metrics:latest
    command: >
      -storageDataPath=/vm-data
      -retentionPeriod=90d
      -httpListenAddr=:8428
      -prometheusListenAddr=:8429
      -vmalert.retentionPeriod=90d
    volumes:
      - vm_data:/vm-data
    ports:
      - "8428:8428"
      - "8429:8429"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8428/health"]
    deploy:
      resources:
        limits:
          memory: 2G
  
  loki:
    image: grafana/loki:2.9.0
    volumes:
      - ./config/loki-config.yaml:/etc/loki/local-config.yaml:ro
    ports:
      - "3100:3100"
    depends_on:
      - minio
    deploy:
      resources:
        limits:
          memory: 512M
  
  tempo:
    image: grafana/tempo:2.3.0
    volumes:
      - ./config/tempo-config.yaml:/etc/tempo/tempo.yaml:ro
      - tempo_data:/tmp/tempo
    ports:
      - "4317:4317"
      - "4318:4318"
      - "16687:16687"
    depends_on:
      - minio
    deploy:
      resources:
        limits:
          memory: 512M
  
  grafana:
    image: grafana/grafana:10.2.0
    volumes:
      - grafana_data:/var/lib/grafana
      - ./config/grafana/provisioning:/etc/grafana/provisioning:ro
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    ports:
      - "3000:3000"
    depends_on:
      - victoriametrics
      - loki
      - tempo
    deploy:
      resources:
        limits:
          memory: 512M
  
  # ==================== Java 后端服务 ====================
  
  onemonitor-gateway:
    build:
      context: ./backend/gateway
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_GATEWAY_ROUTES[0].id=auth
      - SPRING_CLOUD_GATEWAY_ROUTES[0].uri=lb://auth-service
      - SPRING_CLOUD_GATEWAY_ROUTES[0].predicates[0]=Path=/api/v1/auth/**
    depends_on:
      - consul
    deploy:
      resources:
        limits:
          memory: 512M
  
  onemonitor-auth:
    build:
      context: ./backend/auth
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DATABASE_URL=postgresql://admin:admin123@postgres:5432/onemonitor
      - REDIS_URL=redis://:onemonitor123@redis:6379/0
    depends_on:
      - postgres
      - redis
    deploy:
      resources:
        limits:
          memory: 512M
  
  onemonitor-cmdb:
    build:
      context: ./backend/cmdb
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DATABASE_URL=postgresql://admin:admin123@postgres:5432/onemonitor
    depends_on:
      - postgres
    deploy:
      resources:
        limits:
          memory: 1G
  
  onemonitor-monitor:
    build:
      context: ./backend/monitor
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - VICTORIAMETRICS_URL=http://victoriametrics:8428
    depends_on:
      - victoriametrics
    deploy:
      resources:
        limits:
          memory: 1G
  
  onemonitor-alert:
    build:
      context: ./backend/alert
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - VICTORIAMETRICS_URL=http://victoriametrics:8428
    depends_on:
      - victoriametrics
      - redis
    deploy:
      resources:
        limits:
          memory: 1G
  
  # ==================== Python 分析服务 ====================
  
  onemonitor-analytics:
    build:
      context: ./analytics
      dockerfile: Dockerfile
    environment:
      - VICTORIAMETRICS_URL=http://victoriametrics:8428
      - POSTGRES_URL=postgresql://admin:admin123@postgres:5432/onemonitor
      - REDIS_URL=redis://:onemonitor123@redis:6379/0
    depends_on:
      - victoriametrics
      - postgres
      - redis
    deploy:
      resources:
        limits:
          memory: 1G
  
  onemonitor-celery:
    build:
      context: ./analytics
      dockerfile: Dockerfile
    command: celery -A app.tasks.celery_app worker -l info
    environment:
      - CELERY_BROKER_URL=redis://:onemonitor123@redis:6379/0
      - CELERY_RESULT_BACKEND=redis://:onemonitor123@redis:6379/0
    depends_on:
      - redis
    deploy:
      resources:
        limits:
          memory: 1G
  
  # ==================== 前端 ====================
  
  onemonitor-frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    environment:
      - VITE_API_BASE_URL=http://gateway:8080/api/v1
      - VITE_GRAFANA_URL=http://grafana:3000
    ports:
      - "80:80"
    depends_on:
      - onemonitor-gateway
    deploy:
      resources:
        limits:
          memory: 256M
  
  # ==================== 采集层 ====================
  
  otel-collector:
    image: otel/opentelemetry-collector-contrib:0.91.0
    volumes:
      - ./config/otel-collector.yaml:/etc/otel-collector-config.yaml:ro
    command: --config=/etc/otel-collector-config.yaml
    ports:
      - "4317:4317"
      - "4318:4318"
      - "8889:8889"
    depends_on:
      - victoriametrics
      - loki
      - tempo
    deploy:
      resources:
        limits:
          memory: 512M

volumes:
  postgres_data:
  redis_data:
  minio_data:
  vm_data:
  grafana_data:
  tempo_data:

networks:
  default:
    name: onemonitor-network
```

---

## 8. 总结

### 8.1 架构优势

| 优势 | 说明 |
|------|------|
| **高性能** | Java 核心服务支撑 100万+ 指标/秒 |
| **可观测性** | 统一 Metrics + Logs + Traces |
| **智能化** | Python ML 分析，异常检测、容量预测 |
| **标准化** | CMDB 驱动监控对象管理 |
| **云原生** | K8s 原生部署，声明式配置 |
| **混合架构** | Java + Python 各取所长 |

### 8.2 技术栈总结

| 层级 | 技术选型 |
|------|---------|
| **Java 后端** | Spring Boot 3.x + WebFlux + JPA |
| **Python 分析** | FastAPI + Celery + Pandas + Scikit-learn |
| **前端** | Vue 3 + Element Plus + ECharts + G6 |
| **时序存储** | VictoriaMetrics Cluster |
| **日志存储** | Grafana Loki + MinIO |
| **链路追踪** | Grafana Tempo + MinIO |
| **关系存储** | PostgreSQL + JPA |
| **缓存** | Redis Cluster |
| **采集层** | OpenTelemetry Collector |

### 8.3 下一步

1. 确认架构设计
2. 生成项目代码脚手架
3. 实现核心模块代码
4. 编写单元测试
5. 部署验证

---

*文档版本: 2.0*  
*作者: OneMonitor Team*
