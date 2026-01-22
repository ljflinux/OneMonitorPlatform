# OneMonitor 云原生监控平台架构设计文档

## 1. 项目概述

### 1.1 项目背景

OneMonitor 是一款企业级云原生运维监控平台，采用现代化的可观测性技术栈（Metrics + Logs + Traces），为企业提供全面、高效、智能的 IT 基础设施和业务系统监控解决方案。

### 1.2 设计目标

- **统一可观测性**：整合 Metrics、Logs、Traces 三种遥测数据
- **云原生优先**：容器化部署，声明式配置，Kubernetes 原生
- **成本效益**：使用对象存储降低存储成本，优化资源使用
- **高可用**：支持水平扩展，多组件容灾
- **易于运维**：统一采集层，简化运维复杂度

### 1.3 技术选型

| 组件 | 选型 | 版本 | 用途 |
|------|------|------|------|
| 时序数据库 | VictoriaMetrics | latest | 指标存储与查询 (PromQL兼容) |
| 日志系统 | Grafana Loki | latest | 日志聚合与查询 |
| 链路追踪 | Grafana Tempo | latest | 分布式追踪存储 |
| 采集代理 | OpenTelemetry Collector | latest | 统一遥测数据采集 |
| 可视化 | Grafana | latest | 仪表盘与Explore查询 |
| 后端框架 | FastAPI | 0.109+ | 业务API服务 |
| 前端框架 | Vue 3 | 3.4+ | Web管理界面 |
| 关系数据库 | PostgreSQL | 15+ | 业务数据存储 |
| 缓存 | Redis | 7+ | 会话与配置缓存 |
| 对象存储 | MinIO | latest | Loki/Tempo后端存储 |

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              OneMonitor 云原生监控平台                               │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                          🎯 统一采集层 (OpenTelemetry)                          │ │
│  │                                                                                 │ │
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   │ │
│  │  │                      OTel Agent (DaemonSet/K8s)                          │ │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐       │   │ │
│  │  │  │Prometheus│  │  OTLP   │  │  Logs   │  │ Traces  │  │ JMX/SNMP│       │   │ │
│  │  │  │Receiver │  │ Receiver│  │Receiver │  │Receiver │  │ Receiver│       │   │ │
│  │  │  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘       │   │ │
│  │  └───────┼────────────┼────────────┼────────────┼────────────┼─────────────┘   │ │
│  │          │            │            │            │            │                  │ │
│  │          ▼            ▼            ▼            ▼            ▼                  │ │
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   │ │
│  │  │  Processors: batch, memory_limiter, k8sattributes, resourcedetection    │   │ │
│  │  └─────────────────────────────────────────────────────────────────────────┘   │ │
│  │                                      │                                          │ │
│  │                                      ▼                                          │ │
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   │ │
│  │  │  Exporters:                                                           ──┐  │   │ │
│  │  │    • vmwrite (VictoriaMetrics)                                         │  │   │ │
│  │  │    • loki (Grafana Loki)                                               │  │   │ │
│  │  │    • otlp/tempo (Grafana Tempo)                                        │  │   │ │
│  │  │    • prometheusremotewrite (远程写入)                                   │  │   │ │
│  │  │                                                                    ▼    │   │ │
│  │  └─────────────────────────────────────────────────────────────────────────┘   │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌─────────────────────────────┬─────────────────────────────┬─────────────────────┐ │
│  │                             │                             │                     │ │
│  ▼                             ▼                             ▼                     │ │
│ ┌────────────────┐    ┌───────────────────┐    ┌──────────────────────┐           │ │
│ │   Victoria     │    │      Loki         │    │       Tempo          │           │ │
│ │   Metrics      │    │   (S3/MinIO)      │    │    (S3/MinIO)        │           │ │
│ │   (时序数据库)  │    │    (日志存储)     │    │    (链路追踪)         │           │ │
│ └───────┬────────┘    └─────────┬─────────┘    └──────────┬───────────┘           │ │
│         │                       │                        │                         │ │
│         └───────────────────────┼────────────────────────┘                         │ │
│                                 │                                                  │ │
│                                 ▼                                                  │ │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │                              Grafana                                             │ │
│ │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                  │ │
│ │  │   Dashboards    │  │     Explore     │  │    Alerting     │                  │ │
│ │  │   (仪表盘)      │  │  (三合一查询)    │  │   (统一告警)    │                  │ │
│ │  └─────────────────┘  └─────────────────┘  └─────────────────┘                  │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌─────────────────────────────┬─────────────────────────────┬─────────────────────┐ │
│  │                             │                             │                     │ │
│  ▼                             ▼                             ▼                     │ │
│ ┌────────────────┐    ┌───────────────────┐    ┌──────────────────────┐           │ │
│ │  OneMonitor    │    │     PostgreSQL    │    │        Redis         │           │ │
│ │  Backend API   │    │  (业务数据存储)    │    │    (缓存/会话)       │           │ │
│ │  (FastAPI)     │    │                   │    │                      │           │ │
│ └───────┬────────┘    └───────────────────┘    └──────────────────────┘           │ │
│         │                                                                         │ │
│         ▼                                                                         │ │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │                              OneMonitor Frontend (Vue 3)                         │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 数据流设计

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   数据流设计                                         │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  1. Metrics 数据流:                                                                 │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐    │
│  │ Exporter │───►│ OTel Agent   │───►│ Victoria     │───►│     Grafana      │    │
│  │(Prometheus│    │ Prometheus   │    │   Metrics    │    │  Dashboards/     │    │
│  │ 格式)    │    │ Receiver     │    │   :8428      │    │    Alerting      │    │
│  └──────────┘    └──────────────┘    └──────────────┘    └──────────────────┘    │
│                                                                                     │
│  2. Logs 数据流:                                                                    │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐    │
│  │  App/    │───►│ OTel Agent   │───►│     Loki     │───►│   Grafana        │    │
│  │ Container│    │ Log Receiver │    │   :3100      │    │    Explore       │    │
│  └──────────┘    └──────────────┘    └──────────────┘    └──────────────────┘    │
│                                                                                     │
│  3. Traces 数据流:                                                                  │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐    │
│  │ App(OTel │───►│ OTel Agent   │───►│    Tempo     │───►│   Grafana        │    │
│  │   SDK)   │    │ OTLP Receiver│    │   :4317      │    │    Explore       │    │
│  └──────────┘    └──────────────┘    └──────────────┘    └──────────────────┘    │
│                                                                                     │
│  4. 业务管理数据流:                                                                 │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────────────┐                   │
│  │ Frontend │───►│ OneMonitor   │───►│ PostgreSQL + Redis   │                   │
│  │          │    │ Backend API  │    │ (用户/配置/规则)      │                   │
│  └──────────┘    └──────────────┘    └──────────────────────┘                   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 组件说明

#### 2.3.1 OpenTelemetry Collector

**部署模式**: Agent (DaemonSet)

**配置结构**:
```yaml
receivers:
  prometheus:
    config:
      scrape_configs:
        - job_name: 'node-exporter'
          static_configs:
            - targets: ['node-exporter:9100']
  
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:
    timeout: 1s
    send_batch_size: 1024
  
  memory_limiter:
    limit_mib: 1000
  
  k8sattributes:
  
  resourcedetection:
    detectors: [env, gcp, ec2, azure, system]

exporters:
  prometheusremotewrite:
    endpoint: http://victoriametrics:8428/api/v1/write
  
  loki:
    endpoint: http://loki:3100/loki/api/v1/push
  
  otlp:
    endpoint: tempo:4317
    tls:
      insecure: true

service:
  pipelines:
    metrics:
      receivers: [prometheus, otlp]
      processors: [memory_limiter, batch, k8sattributes, resourcedetection]
      exporters: [prometheusremotewrite]
    
    logs:
      receivers: [otlp]
      processors: [memory_limiter, batch, k8sattributes, resourcedetection]
      exporters: [loki]
    
    traces:
      receivers: [otlp]
      processors: [memory_limiter, batch, k8sattributes, resourcedetection]
      exporters: [otlp]
```

#### 2.3.2 VictoriaMetrics

**部署模式**: 单节点 (开发) / 集群 (生产)

**配置**:
```yaml
# 单节点配置
command: /victoria-metrics-prod
args:
  - -storageDataPath=/vm-data
  - -retentionPeriod=30d
  - -httpListenAddr=:8428
  - -prometheusListenAddr=:8429

# 集群模式配置 (VMCluster CRD)
apiVersion: operator.victoriametrics.com/v1beta1
kind: VMCluster
metadata:
  name: onemonitor-vmcluster
spec:
  replicationFactor: 2
  vmstorage:
    replicaCount: 2
    storageDataPath: /vm-data
  vmselect:
    replicaCount: 2
  vminsert:
    replicaCount: 2
```

#### 2.3.3 Grafana Loki

**部署模式**: 单二进制 (开发) / 分布式 (生产)

**配置**:
```yaml
# Helm values
loki:
  commonConfig:
    replication_factor: 1
  schemaConfig:
    configs:
      - from: "2024-04-01"
        store: tsdb
        object_store: s3
        schema: v13
        index:
          prefix: loki_index_
          period: 24h
  
  storage_config:
    aws:
      region: minio
      endpoint: minio:9000
      insecure: true
      s3forcepathstyle: true
      bucketnames: loki-chunks
  
  ingester:
    chunk_encoding: snappy
    wal:
      enabled: true
  
  querier:
    max_concurrent: 4

deploymentMode: SingleBinary
```

#### 2.3.4 Grafana Tempo

**部署模式**: 单实例 (开发) / 分布式 (生产)

**配置**:
```yaml
# Helm values
tempo:
  enabled: true
  
  config: |
    distributor:
      receivers:
        otlp:
          protocols:
            grpc:
              endpoint: 0.0.0.0:4317
            http:
              endpoint: 0.0.0.0:4318
    
    ingester:
      trace_idle_period: 10s
      max_block_duration: 5m
      complete_block_timeout: 30m
    
    querier:
      frontend_worker:
        frontend_address: tempo-query-frontend:9095
    
    compactor:
      compaction:
        block_retention: 168h
    
    storage:
      trace:
        backend: s3
        s3:
          endpoint: minio:9000
          insecure: true
          bucket: tempo-traces

storage:
  s3:
    bucket: tempo-traces
```

---

## 3. 功能模块设计

### 3.1 功能模块列表

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              OneMonitor 功能模块                                     │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                              仪表盘 (Dashboard)                                 │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │ │
│  │  │  系统总览     │  │  业务视图    │  │  基础设施    │  │  自定义仪表盘 │       │ │
│  │  │  (Overview)  │  │ (Business)  │  │ (Infra)     │  │  (Custom)    │       │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘       │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                            监控管理 (Monitoring)                                │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │ │
│  │  │  监控对象     │  │  采集配置    │  │  指标浏览    │  │  服务发现    │       │ │
│  │  │  (Targets)   │  │  (OTel Config│  │  (Metrics)  │  │  (Discovery) │       │ │
│  │  │  - 列表      │  │  - 编辑)     │  │  - Explore │  │  - K8s/Consul│       │ │
│  │  │  - 状态      │  │  - 版本管理  │  │  - PromQL  │  │  - 动态注册  │       │ │
│  │  │  - 标签      │  │  -下发推送   │  │  - 函数     │  │             │       │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘       │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                              告警中心 (Alerts)                                  │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │ │
│  │  │  告警规则     │  │  告警历史    │  │  通知渠道    │  │  告警抑制    │       │ │
│  │  │  (Rules)     │  │  (History)   │  │  (Channels) │  │  (Silence)  │       │ │
│  │  │  - 创建      │  │  - 列表      │  │  - Email    │  │  - 静默规则  │       │ │
│  │  │  - 编辑      │  │  - 详情      │  │  - DingTalk │  │  - 抑制规则  │       │ │
│  │  │  - 启用/禁用 │  │  - 趋势      │  │  - Webhook  │  │             │       │ │
│  │  │  - 复制      │  │  - 统计      │  │  - Slack    │  │             │       │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘       │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                              日志分析 (Logs)                                    │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                        │ │
│  │  │  日志查询     │  │  日志仪表盘  │  │  日志告警    │                        │ │
│  │  │  (Search)    │  │  (Dashboards)│  │  (Alerts)   │                        │ │
│  │  │  - LogQL     │  │  - 预置模板  │  │  - 模式匹配  │                        │ │
│  │  │  - 过滤器    │  │  - 自定义    │  │  - 阈值告警  │                        │ │
│  │  │  - 实时流    │  │             │  │             │                        │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘                        │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                            链路追踪 (Traces)                                    │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                        │ │
│  │  │  追踪查询     │  │  服务依赖    │  │  性能分析    │                        │ │
│  │  │  (Explorer)  │  │  (Service    │  │  (Perform-   │                        │ │
│  │  │  - TraceID   │  │   Graph)     │  │   ance)     │                        │ │
│  │  │  - 时间范围   │  │  - 拓扑图    │  │  - 延迟分布  │                        │ │
│  │  │  - 服务过滤   │  │  - 调用关系  │  │  - 错误率    │                        │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘                        │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                            配置管理 (CMDB)                                      │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │ │
│  │  │  资源管理     │  │  关系拓扑    │  │  变更追踪    │  │  导入导出    │       │ │
│  │  │  (Resources) │  │  (Relations) │  │  (Changes)  │  │  (Import/    │       │ │
│  │  │  - CI类型    │  │  - 可视化    │  │  - 历史记录  │  │   Export)   │       │ │
│  │  │  - CI实例    │  │  - 依赖分析  │  │  - 审批流程  │  │             │       │ │
│  │  │  - 属性定义  │  │             │  │             │  │             │       │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘       │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                            报表中心 (Reports)                                   │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                        │ │
│  │  │  定时报表     │  │  趋势分析    │  │  容量规划    │                        │ │
│  │  │  (Scheduled) │  │  (Trends)    │  │  (Capacity)  │                        │ │
│  │  │  - 日报      │  │  - 对比分析  │  │  - 预测模型  │                        │ │
│  │  │  - 周报      │  │  - 异常检测  │  │  - 扩容建议  │                        │ │
│  │  │  - 月报      │  │  - 容量趋势  │  │             │                        │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘                        │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                            系统设置 (Settings)                                  │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │ │
│  │  │  用户管理     │  │  角色权限    │  │  通知渠道    │  │  系统配置    │       │ │
│  │  │  (Users)     │  │  (RBAC)      │  │  (Channels) │  │  (Config)   │       │ │
│  │  │  - 列表      │  │  - 角色定义  │  │  - 邮箱配置  │  │  - 基础设置  │       │ │
│  │  │  - 创建      │  │  - 权限分配  │  │  - 钉钉配置  │  │  - 数据保留  │       │ │
│  │  │  - 编辑      │  │  - 资源权限  │  │  - Webhook  │  │  - 高可用    │       │ │
│  │  │  - 密码重置  │  │             │  │             │  │             │       │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘       │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 模块详细说明

#### 3.2.1 仪表盘模块

| 功能 | 描述 | 数据源 |
|------|------|--------|
| 系统总览 | 全局状态、关键指标聚合 | VM/Loki |
| 业务视图 | 按业务线/部门组织监控视图 | VM |
| 基础设施 | K8s/VM/网络设备分类视图 | VM/Loki |
| 自定义仪表盘 | 用户创建、保存、分享仪表盘 | VM/Loki/Tempo |

#### 3.2.2 监控管理模块

| 功能 | 描述 | 数据源 |
|------|------|--------|
| 监控对象 | Target 管理、标签管理、状态监控 | VM |
| 采集配置 | OTel Collector 配置管理、版本控制 | PostgreSQL |
| 指标浏览 | Metrics Explorer、PromQL 查询 | VM |
| 服务发现 | K8s 服务发现、Consul 注册 | OTel Agent |

#### 3.2.3 告警中心模块

| 功能 | 描述 | 数据源 |
|------|------|--------|
| 告警规则 | Grafana Alert Rules 管理 | Grafana API |
| 告警历史 | 告警事件列表、详情、统计 | Grafana API |
| 通知渠道 | 邮箱、钉钉、企微、Webhook 配置 | PostgreSQL |
| 告警抑制 | Silence、Inhibit 规则配置 | Grafana API |

#### 3.2.4 日志分析模块

| 功能 | 描述 | 数据源 |
|------|------|--------|
| 日志查询 | LogQL 查询、实时流、日志下载 | Loki |
| 日志仪表盘 | 预置/自定义日志分析面板 | Loki |
| 日志告警 | 基于日志模式的告警规则 | Grafana |

#### 3.2.5 链路追踪模块

| 功能 | 描述 | 数据源 |
|------|------|--------|
| 追踪查询 | TraceID 查询、服务/操作过滤 | Tempo |
| 服务依赖 | Service Graph 拓扑图 | Tempo |
| 性能分析 | 延迟分布、错误率、吞吐量 | Tempo |

#### 3.2.6 CMDB 模块

| 功能 | 描述 | 数据源 |
|------|------|--------|
| 资源管理 | CI 类型定义、CI 实例管理 | PostgreSQL |
| 关系拓扑 | CI 关系可视化、依赖分析 | PostgreSQL/Neo4j |
| 变更追踪 | 变更历史记录、审批流程 | PostgreSQL |

---

## 4. API 接口设计

### 4.1 API 规范

**基础信息**:
- 框架: FastAPI
- 版本: v1
- 认证: JWT Bearer Token
- 文档: OpenAPI 3.0 (/docs, /redoc)

### 4.2 API 路由结构

```
/api/v1
├── /auth
│   ├── POST   /login          # 用户登录
│   ├── POST   /logout         # 用户登出
│   ├── POST   /refresh        # 刷新Token
│   └── GET    /me             # 获取当前用户信息
│
├── /users
│   ├── GET    /               # 用户列表
│   ├── POST   /               # 创建用户
│   ├── GET    /{id}           # 用户详情
│   ├── PUT    /{id}           # 更新用户
│   ├── DELETE /{id}           # 删除用户
│   └── PUT    /{id}/password  # 修改密码
│
├── /targets          # 监控对象管理
│   ├── GET    /               # 列表
│   ├── POST   /               # 创建
│   ├── GET    /{id}           # 详情
│   ├── PUT    /{id}           # 更新
│   ├── DELETE /{id}           # 删除
│   ├── GET    /{id}/metrics   # 指标数据
│   └── PUT    /{id}/status    # 更新状态
│
├── /alert-rules      # 告警规则
│   ├── GET    /               # 列表
│   ├── POST   /               # 创建
│   ├── GET    /{id}           # 详情
│   ├── PUT    /{id}           # 更新
│   ├── DELETE /{id}           # 删除
│   ├── PUT    /{id}/enable    # 启用
│   └── PUT    /{id}/disable   # 禁用
│
├── /alerts           # 告警事件
│   ├── GET    /               # 列表
│   ├── GET    /{id}           # 详情
│   ├── PUT    /{id}/acknowledge # 确认
│   └── PUT    /{id}/resolve   # 解决
│
├── /channels         # 通知渠道
│   ├── GET    /               # 列表
│   ├── POST   /               # 创建
│   ├── GET    /{id}           # 详情
│   ├── PUT    /{id}           # 更新
│   └── DELETE /{id}           # 删除
│
├── /dashboards       # 仪表盘
│   ├── GET    /               # 列表
│   ├── POST   /               # 创建
│   ├── GET    /{id}           # 详情
│   ├── PUT    /{id}           # 更新
│   ├── DELETE /{id}           # 删除
│   └── GET    /{id}/export    # 导出
│
├── /cmdb
│   ├── /ci-types    # CI类型
│   ├── /cis         # CI实例
│   └── /relations   # CI关系
│
├── /reports         # 报表
│   ├── GET    /               # 列表
│   ├── POST   /               # 创建
│   ├── GET    /{id}           # 详情
│   ├── POST   /{id}/generate  # 生成报表
│   └── GET    /{id}/download  # 下载报表
│
├── /settings        # 系统设置
│   ├── GET    /               # 获取设置
│   ├── PUT    /               # 更新设置
│   └── /notification          # 通知设置
│
└── /metrics         # 内部指标
    └── GET    /               # Prometheus指标
```

### 4.3 API 响应格式

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 响应数据
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**分页响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [],
    "total": 100,
    "page": 1,
    "page_size": 20,
    "total_pages": 5
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "Invalid parameters",
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 5. 数据模型设计

### 5.1 数据库选型

| 数据库 | 用途 | 说明 |
|--------|------|------|
| PostgreSQL | 业务数据 | 用户、配置、CMDB、告警规则 |
| Redis | 缓存 | 会话、配置缓存、指标缓存 |
| VictoriaMetrics | 指标数据 | 监控指标存储与查询 |
| Loki | 日志数据 | 日志聚合与查询 |
| Tempo | 追踪数据 | 分布式追踪存储 |

### 5.2 PostgreSQL 数据模型

#### 5.2.1 用户认证模块

```sql
-- 用户表
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    is_superuser BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 角色表
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    permissions JSONB DEFAULT '[]',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 用户角色关联表
CREATE TABLE user_roles (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

-- 会话表
CREATE TABLE sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires TIME ZONE NOT_at TIMESTAMP WITH NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- 操作日志表
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

#### 5.2.2 监控管理模块

```sql
-- 监控对象表
CREATE TABLE monitor_targets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- host, k8s, network, database, middleware, application
    status VARCHAR(20) DEFAULT 'unknown',  -- unknown, healthy, warning, critical, offline
    endpoint VARCHAR(500),
    labels JSONB DEFAULT '{}',
    annotations JSONB DEFAULT '{}',
    region VARCHAR(50),
    zone VARCHAR(50),
    description TEXT,
    metadata JSONB DEFAULT '{}',
    last_check_at TIMESTAMP WITH TIME ZONE,
    last_status_at TIMESTAMP WITH TIME ZONE,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_monitor_targets_type ON monitor_targets(type);
CREATE INDEX idx_monitor_targets_status ON monitor_targets(status);
CREATE INDEX idx_monitor_targets_labels ON monitor_targets USING GIN(labels);

-- 监控组表
CREATE TABLE monitor_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id INTEGER REFERENCES monitor_groups(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 监控对象与组关联表
CREATE TABLE target_groups (
    target_id INTEGER REFERENCES monitor_targets(id) ON DELETE CASCADE,
    group_id INTEGER REFERENCES monitor_groups(id) ON DELETE CASCADE,
    PRIMARY KEY (target_id, group_id)
);

-- OTel采集配置表
CREATE TABLE otel_configs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    config YAML,  -- 使用TEXT存储YAML格式配置
    version INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    target_selector JSONB,  -- 用于选择应用该配置的targets
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

#### 5.2.3 告警管理模块

```sql
-- 通知渠道表
CREATE TABLE notification_channels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- email, dingtalk, webhook, slack, sms
    config JSONB NOT NULL,  -- 根据type存储不同配置
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 告警规则表
CREATE TABLE alert_rules (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    expr TEXT NOT NULL,  -- PromQL/LogQL表达式
    for_duration VARCHAR(20) DEFAULT '5m',  -- 触发持续时间
    severity VARCHAR(20) NOT NULL,  -- critical, error, warning, info
    labels JSONB DEFAULT '{}',
    annotations JSONB DEFAULT '{}',
    datasource VARCHAR(50) NOT NULL,  -- prometheus, loki, tempo
    is_enabled BOOLEAN DEFAULT TRUE,
    notify_channels INTEGER[],
    group_id INTEGER REFERENCES alert_rule_groups(id),
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 告警规则组表
CREATE TABLE alert_rule_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    interval VARCHAR(20) DEFAULT '1m',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 告警事件表
CREATE TABLE alert_events (
    id SERIAL PRIMARY KEY,
    fingerprint VARCHAR(256) NOT NULL,
    rule_id INTEGER REFERENCES alert_rules(id),
    status VARCHAR(20) NOT NULL,  -- firing, pending, resolved
    severity VARCHAR(20) NOT NULL,
    labels JSONB NOT NULL,
    annotations JSONB NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE,
    generator_url TEXT,
    first_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_by INTEGER REFERENCES users(id),
    acknowledged_by INTEGER REFERENCES users(id),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_alert_events_rule_id ON alert_events(rule_id);
CREATE INDEX idx_alert_events_status ON alert_events(status);
CREATE INDEX idx_alert_events_fingerprint ON alert_events(fingerprint);
CREATE INDEX idx_alert_events_last_seen_at ON alert_events(last_seen_at DESC);

-- 静默规则表
CREATE TABLE silence_rules (
    id SERIAL PRIMARY KEY,
    matchers JSONB NOT NULL,  -- 匹配条件
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by INTEGER REFERENCES users(id),
    comment TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

#### 5.2.4 CMDB 模块

```sql
-- CI类型表
CREATE TABLE ci_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    icon VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    parent_type_id INTEGER REFERENCES ci_types(id),
    attribute_schema JSONB,  -- 属性定义
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- CI实例表
CREATE TABLE cis (
    id SERIAL PRIMARY KEY,
    ci_type_id INTEGER REFERENCES ci_types(id) ON DELETE RESTRICT,
    name VARCHAR(200) NOT NULL,
    display_name VARCHAR(200),
    status VARCHAR(50) DEFAULT 'active',
    attributes JSONB DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    location VARCHAR(200),
    owner VARCHAR(100),
    parent_id INTEGER REFERENCES cis(id),
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_cis_ci_type_id ON cis(ci_type_id);
CREATE INDEX idx_cis_status ON cis(status);
CREATE INDEX idx_cis_name ON cis(name);

-- CI关系表
CREATE TABLE ci_relations (
    id SERIAL PRIMARY KEY,
    source_ci_id INTEGER REFERENCES cis(id) ON DELETE CASCADE,
    target_ci_id INTEGER REFERENCES cis(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) NOT NULL,  -- contains, depends_on, runs_on, connects_to
    description TEXT,
    attributes JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_ci_relations_source ON ci_relations(source_ci_id);
CREATE INDEX idx_ci_relations_target ON ci_relations(target_ci_id);

-- CI变更历史表
CREATE TABLE ci_change_history (
    id SERIAL PRIMARY KEY,
    ci_id INTEGER REFERENCES cis(id) ON DELETE CASCADE,
    change_type VARCHAR(20) NOT NULL,  -- create, update, delete, status_change
    changed_by INTEGER REFERENCES users(id),
    old_values JSONB,
    new_values JSONB,
    change_description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

#### 5.2.5 报表模块

```sql
-- 报表模板表
CREATE TABLE report_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,  -- daily, weekly, monthly, custom
    config JSONB NOT NULL,  -- 报表配置
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 报表任务表
CREATE TABLE report_tasks (
    id SERIAL PRIMARY KEY,
    template_id INTEGER REFERENCES report_templates(id),
    name VARCHAR(100) NOT NULL,
    cron_expr VARCHAR(100) NOT NULL,
    params JSONB,
    recipients JSONB,  -- 邮件列表
    is_enabled BOOLEAN DEFAULT TRUE,
    last_run_at TIMESTAMP WITH TIME ZONE,
    next_run_at TIMESTAMP WITH TIME ZONE,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 报表生成记录表
CREATE TABLE report_records (
    id SERIAL PRIMARY KEY,
    task_id INTEGER REFERENCES report_tasks(id),
    template_id INTEGER REFERENCES report_templates(id),
    status VARCHAR(20) NOT NULL,  -- pending, running, completed, failed
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    report_url TEXT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

#### 5.2.6 系统配置模块

```sql
-- 系统配置表
CREATE TABLE system_settings (
    id SERIAL PRIMARY KEY,
    key VARCHAR(100) UNIQUE NOT NULL,
    value TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'string',  -- string, number, boolean, json
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 数据保留策略表
CREATE TABLE retention_policies (
    id SERIAL PRIMARY KEY,
    datasource_type VARCHAR(50) NOT NULL,  -- vm, loki, tempo
    name VARCHAR(100) NOT NULL,
    retention_period VARCHAR(20) NOT NULL,  -- 7d, 30d, 90d, 1y
    downsampling JSONB,  -- 降采样配置
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 集成配置表
CREATE TABLE integrations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- prometheus, grafana, jira, ldap, sso
    config JSONB NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    health_status VARCHAR(20) DEFAULT 'unknown',  -- unknown, healthy, unhealthy
    last_health_check_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## 6. 前端架构设计

### 6.1 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | 3.4+ |
| 构建工具 | Vite | 5.0+ |
| UI 组件库 | Element Plus | 2.5+ |
| 状态管理 | Pinia | 2.1+ |
| 路由 | Vue Router | 4.2+ |
| HTTP 客户端 | Axios | 1.6+ |
| 图表库 | ECharts | 5.4+ |
| 表格 | Element Plus Table | - |
| 图标 | Element Plus Icons | - |

### 6.2 前端目录结构

```
frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/                    # API 接口封装
│   │   ├── auth.js             # 认证相关
│   │   ├── users.js            # 用户管理
│   │   ├── targets.js          # 监控对象
│   │   ├── alerts.js           # 告警管理
│   │   ├── dashboards.js       # 仪表盘
│   │   ├── cmdb.js             # CMDB
│   │   ├── reports.js          # 报表
│   │   └── settings.js         # 系统设置
│   │
│   ├── assets/                 # 静态资源
│   │   ├── images/
│   │   └── styles/
│   │       ├── variables.scss
│   │       ├── mixins.scss
│   │       └── global.scss
│   │
│   ├── components/             # 公共组件
│   │   ├── Layout/             # 布局组件
│   │   │   ├── Layout.vue
│   │   │   ├── Header.vue
│   │   │   ├── Sidebar.vue
│   │   │   └── Breadcrumb.vue
│   │   ├── Charts/             # 图表组件
│   │   │   ├── LineChart.vue
│   │   │   ├── BarChart.vue
│   │   │   ├── PieChart.vue
│   │   │   ├── GaugeChart.vue
│   │   │   └── Heatmap.vue
│   │   ├── Common/             # 通用组件
│   │   │   ├── PageHeader.vue
│   │   │   ├── SearchForm.vue
│   │   │   ├── DataTable.vue
│   │   │   ├── StatusTag.vue
│   │   │   └── EmptyData.vue
│   │   └── Grafana/            # Grafana 集成组件
│   │       ├── GrafanaPanel.vue
│   │       └── GrafanaExplore.vue
│   │
│   ├── composables/            # 组合式函数
│   │   ├── useTable.js
│   │   ├── useForm.js
│   │   ├── usePermission.js
│   │   └── useMetrics.js
│   │
│   ├── constants/              # 常量定义
│   │   ├── index.js
│   │   ├── targetTypes.js
│   │   ├── alertSeverity.js
│   │   └── status.js
│   │
│   ├── directives/             # 自定义指令
│   │   ├── permission.js
│   │   └── debounce.js
│   │
│   ├── hooks/                  # 自定义 Hooks
│   │   ├── useChart.js
│   │   ├── useQuery.js
│   │   └── useForm.js
│   │
│   ├── router/                 # 路由配置
│   │   ├── index.js
│   │   └── routes.js
│   │
│   ├── stores/                 # Pinia 状态管理
│   │   ├── index.js
│   │   ├── user.js             # 用户状态
│   │   ├── app.js              # 应用状态
│   │   ├── permission.js       # 权限状态
│   │   └── settings.js         # 设置状态
│   │
│   ├── utils/                  # 工具函数
│   │   ├── index.js
│   │   ├── request.js          # Axios 封装
│   │   ├── auth.js             # 认证工具
│   │   ├── format.js           # 格式化工具
│   │   ├── validate.js         # 校验工具
│   │   └── constants.js        # 常量
│   │
│   ├── views/                  # 页面组件
│   │   ├── login/
│   │   │   └── index.vue
│   │   ├── dashboard/
│   │   │   ├── index.vue
│   │   │   ├── overview/
│   │   │   ├── business/
│   │   │   └── custom/
│   │   ├── monitoring/
│   │   │   ├── targets/
│   │   │   ├── config/
│   │   │   ├── metrics/
│   │   │   └── discovery/
│   │   ├── alerts/
│   │   │   ├── rules/
│   │   │   ├── history/
│   │   │   └── channels/
│   │   ├── logs/
│   │   │   ├── search.vue
│   │   │   └── dashboards/
│   │   ├── traces/
│   │   │   ├── explorer.vue
│   │   │   └── service-graph/
│   │   ├── cmdb/
│   │   │   ├── resources/
│   │   │   ├── relations/
│   │   │   └── changes/
│   │   ├── reports/
│   │   │   ├── templates/
│   │   │   └── tasks/
│   │   ├── settings/
│   │   │   ├── users/
│   │   │   ├── roles/
│   │   │   ├── channels/
│   │   │   └── system/
│   │   └── error/
│   │       ├── 403.vue
│   │       ├── 404.vue
│   │       └── 500.vue
│   │
│   ├── App.vue
│   └── main.js
│
├── .env
├── .env.development
├── .env.production
├── .eslintrc.js
├── .prettierrc
├── index.html
├── package.json
└── vite.config.js
```

### 6.3 路由结构

```javascript
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  
  {
    path: '/',
    component: () => import('@/components/Layout/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      },
      
      {
        path: 'monitoring',
        name: 'Monitoring',
        redirect: '/monitoring/targets',
        meta: { title: '监控管理' },
        children: [
          {
            path: 'targets',
            name: 'Targets',
            component: () => import('@/views/monitoring/targets/index.vue'),
            meta: { title: '监控对象' }
          },
          {
            path: 'targets/:id',
            name: 'TargetDetail',
            component: () => import('@/views/monitoring/targets/Detail.vue'),
            meta: { title: '对象详情' }
          },
          {
            path: 'config',
            name: 'OTelConfig',
            component: () => import('@/views/monitoring/config/index.vue'),
            meta: { title: '采集配置' }
          },
          {
            path: 'metrics',
            name: 'MetricsExplorer',
            component: () => import('@/views/monitoring/metrics/index.vue'),
            meta: { title: '指标浏览' }
          }
        ]
      },
      
      {
        path: 'alerts',
        name: 'Alerts',
        redirect: '/alerts/rules',
        meta: { title: '告警中心' },
        children: [
          {
            path: 'rules',
            name: 'AlertRules',
            component: () => import('@/views/alerts/rules/index.vue'),
            meta: { title: '告警规则' }
          },
          {
            path: 'history',
            name: 'AlertHistory',
            component: () => import('@/views/alerts/history/index.vue'),
            meta: { title: '告警历史' }
          },
          {
            path: 'channels',
            name: 'NotificationChannels',
            component: () => import('@/views/alerts/channels/index.vue'),
            meta: { title: '通知渠道' }
          }
        ]
      },
      
      {
        path: 'logs',
        name: 'Logs',
        redirect: '/logs/search',
        meta: { title: '日志分析' },
        children: [
          {
            path: 'search',
            name: 'LogSearch',
            component: () => import('@/views/logs/search.vue'),
            meta: { title: '日志查询' }
          }
        ]
      },
      
      {
        path: 'traces',
        name: 'Traces',
        redirect: '/traces/explorer',
        meta: { title: '链路追踪' },
        children: [
          {
            path: 'explorer',
            name: 'TraceExplorer',
            component: () => import('@/views/traces/explorer.vue'),
            meta: { title: '追踪查询' }
          },
          {
            path: 'service-graph',
            name: 'ServiceGraph',
            component: () => import('@/views/traces/service-graph.vue'),
            meta: { title: '服务依赖' }
          }
        ]
      },
      
      {
        path: 'cmdb',
        name: 'CMDB',
        redirect: '/cmdb/resources',
        meta: { title: '配置管理' },
        children: [
          {
            path: 'resources',
            name: 'CIResources',
            component: () => import('@/views/cmdb/resources/index.vue'),
            meta: { title: '资源管理' }
          },
          {
            path: 'relations',
            name: 'CIRelations',
            component: () => import('@/views/cmdb/relations/index.vue'),
            meta: { title: '关系拓扑' }
          }
        ]
      },
      
      {
        path: 'reports',
        name: 'Reports',
        redirect: '/reports/tasks',
        meta: { title: '报表中心' },
        children: [
          {
            path: 'tasks',
            name: 'ReportTasks',
            component: () => import('@/views/reports/tasks/index.vue'),
            meta: { title: '报表任务' }
          },
          {
            path: 'templates',
            name: 'ReportTemplates',
            component: () => import('@/views/reports/templates/index.vue'),
            meta: { title: '报表模板' }
          }
        ]
      },
      
      {
        path: 'settings',
        name: 'Settings',
        redirect: '/settings/users',
        meta: { title: '系统设置' },
        children: [
          {
            path: 'users',
            name: 'UserManagement',
            component: () => import('@/views/settings/users/index.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'roles',
            name: 'RoleManagement',
            component: () => import('@/views/settings/roles/index.vue'),
            meta: { title: '角色权限' }
          },
          {
            path: 'system',
            name: 'SystemSettings',
            component: () => import('@/views/settings/system/index.vue'),
            meta: { title: '系统配置' }
          }
        ]
      }
    ]
  },
  
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
];
```

### 6.4 状态管理结构

```javascript
// stores/user.js - 用户状态
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login, logout, getUserInfo } from '@/api/auth';
import { setToken, removeToken } from '@/utils/auth';

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(null);
  const token = ref(localStorage.getItem('token') || '');
  const roles = ref([]);
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => roles.value.includes('admin'));
  const displayName = computed(() => user.value?.display_name || user.value?.username);
  
  // 方法
  async function loginAction(credentials) {
    const response = await login(credentials);
    token.value = response.data.token;
    setToken(response.data.token);
    await fetchUserInfo();
    return response;
  }
  
  async function fetchUserInfo() {
    const response = await getUserInfo();
    user.value = response.data;
    roles.value = response.data.roles || [];
  }
  
  async function logoutAction() {
    try {
      await logout();
    } finally {
      resetState();
    }
  }
  
  function resetState() {
    user.value = null;
    token.value = '';
    roles.value = [];
    removeToken();
  }
  
  return {
    user,
    token,
    roles,
    isLoggedIn,
    isAdmin,
    displayName,
    loginAction,
    fetchUserInfo,
    logoutAction,
    resetState
  };
});

// stores/permission.js - 权限状态
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { constantRoutes, asyncRoutes } from '@/router/routes';
import { useUserStore } from './user';

const hasPermission = (roles, route) => {
  if (route.meta?.roles) {
    return roles.some(role => route.meta.roles.includes(role));
  }
  return true;
};

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([]);
  const addRoutes = ref([]);
  
  function filterAsyncRoutes(routes, roles) {
    return routes.filter(route => {
      if (hasPermission(roles, route)) {
        if (route.children) {
          route.children = filterAsyncRoutes(route.children, roles);
        }
        return true;
      }
      return false;
    });
  }
  
  function generateRoutes(roles) {
    const accessedRoutes = filterAsyncRoutes(asyncRoutes, roles);
    routes.value = [...constantRoutes, ...accessedRoutes];
    addRoutes.value = accessedRoutes;
    return accessedRoutes;
  }
  
  return {
    routes,
    addRoutes,
    generateRoutes
  };
});
```

---

## 7. 部署架构

### 7.1 Docker Compose 配置

```yaml
version: '3.8'

services:
  # ==================== 基础组件 ====================
  
  postgres:
    image: postgres:15-alpine
    container_name: onemonitor-postgres
    restart: unless-stopped
    environment:
      - POSTGRES_DB=onemonitor
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=admin123
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d onemonitor"]
      interval: 5s
      timeout: 5s
      retries: 5
  
  redis:
    image: redis:7-alpine
    container_name: onemonitor-redis
    restart: unless-stopped
    command: redis-server --requirepass onemonitor123
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5
  
  minio:
    image: minio/minio:latest
    container_name: onemonitor-minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=admin
      - MINIO_ROOT_PASSWORD=admin123
    volumes:
      - minio_data:/data
    ports:
      - "9000:9000"
      - "9001:9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3
  
  # ==================== 可观测性组件 ====================
  
  victoriametrics:
    image: victoriametrics/victoria-metrics:latest
    container_name: onemonitor-victoriametrics
    restart: unless-stopped
    command: >
      -storageDataPath=/vm-data
      -retentionPeriod=30d
      -httpListenAddr=:8428
      -prometheusListenAddr=:8429
    volumes:
      - vm_data:/vm-data
    ports:
      - "8428:8428"
      - "8429:8429"
    depends_on:
      - minio
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8428/health"]
      interval: 10s
      timeout: 5s
      retries: 3
  
  loki:
    image: grafana/loki:2.9.0
    container_name: onemonitor-loki
    restart: unless-stopped
    command: -config.file=/etc/loki/local-config.yaml
    volumes:
      - ./config/loki-config.yaml:/etc/loki/local-config.yaml:ro
    ports:
      - "3100:3100"
    environment:
      - TZ=Asia/Shanghai
    depends_on:
      - minio
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3100/loki/api/v1/status/buildinfo"]
      interval: 10s
      timeout: 5s
      retries: 3
  
  tempo:
    image: grafana/tempo:2.3.0
    container_name: onemonitor-tempo
    restart: unless-stopped
    command: -config.file=/etc/tempo/tempo.yaml
    volumes:
      - ./config/tempo-config.yaml:/etc/tempo/tempo.yaml:ro
      - tempo_data:/tmp/tempo
    ports:
      - "4317:4317"  # OTLP gRPC
      - "4318:4318"  # OTLP HTTP
      - "16687:16687"  # Query frontend
    depends_on:
      - minio
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:16687"]
      interval: 10s
      timeout: 5s
      retries: 3
  
  grafana:
    image: grafana/grafana:10.2.0
    container_name: onemonitor-grafana
    restart: unless-stopped
    volumes:
      - ./config/grafana/provisioning:/etc/grafana/provisioning:ro
      - ./config/grafana/dashboards:/etc/grafana/dashboards:ro
      - grafana_data:/var/lib/grafana
      - ./config/grafana.ini:/etc/grafana/grafana.ini:ro
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
    ports:
      - "3000:3000"
    depends_on:
      - victoriametrics
      - loki
      - tempo
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/api/health"]
      interval: 10s
      timeout: 5s
      retries: 3
  
  # ==================== 业务组件 ====================
  
  otel-collector:
    image: otel/opentelemetry-collector-contrib:0.91.0
    container_name: onemonitor-otel-collector
    restart: unless-stopped
    command: --config=/etc/otel-collector-config.yaml
    volumes:
      - ./config/otel-collector.yaml:/etc/otel-collector-config.yaml:ro
      - /var/log:/var/log:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    ports:
      - "4317:4317"  # OTLP gRPC
      - "4318:4318"  # OTLP HTTP
      - "8889:8889"  # Prometheus metrics
    depends_on:
      - victoriametrics
      - loki
      - tempo
  
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: onemonitor-backend
    restart: unless-stopped
    environment:
      - DATABASE_URL=postgresql://admin:admin123@postgres:5432/onemonitor
      - REDIS_URL=redis://:onemonitor123@redis:6379/0
      - SECRET_KEY=your-secret-key-change-in-production
      - API_V1_STR=/api/v1
      - GRAFANA_URL=http://grafana:3000
      - GRAFANA_USER=admin
      - GRAFANA_PASSWORD=admin123
      - VICTORIAMETRICS_URL=http://victoriametrics:8428
      - LOKI_URL=http://loki:3100
      - TEMPO_URL=http://tempo:4317
    ports:
      - "8000:8000"
    depends_on:
      - postgres
      - redis
      - grafana
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 10s
      timeout: 5s
      retries: 3
  
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: onemonitor-frontend
    restart: unless-stopped
    environment:
      - VITE_API_BASE_URL=http://backend:8000/api/v1
      - VITE_GRAFANA_URL=http://grafana:3000
    ports:
      - "80:80"
    depends_on:
      - backend

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

### 7.2 OTel Collector 配置

```yaml
# config/otel-collector.yaml
receivers:
  # Prometheus 格式指标
  prometheus:
    config:
      scrape_configs:
        # Node Exporter
        - job_name: node
          static_configs:
            - targets: ['node-exporter:9100']
          relabel_configs:
            - source_labels: [__address__]
              target_label: instance
              replacement: '${1}'
        
        # 自定义指标
        - job_name: custom
          static_configs:
            - targets: ['custom-exporter:8080']
  
  # OpenTelemetry 协议
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318
  
  # 日志文件采集
  filelog:
    include:
      - /var/log/containers/*.log
    start_at: beginning
    include_file_path: true
    operators:
      - type: json_parser
        parse_from: body
        timestamp:
          parse_from: time_iso8601
          layout: '%Y-%m-%dT%H:%M:%S.%LZ'
  
  # JMX 指标
  jmx:
    jar_path: /usr/share/opentelemetry-collector-contrib/extensions/jmxreceiver/lib/jmxreceiver.jar
    endpoint: "0.0.0.0:8080"
    target_system: java.lang

processors:
  # 批量处理
  batch:
    timeout: 1s
    send_batch_size: 1024
  
  # 内存限制
  memory_limiter:
    limit_mib: 1000
    spike_limit_mib: 200
    check_interval: 5s
  
  # Kubernetes 属性
  k8sattributes:
    passthrough: false
    filter:
      node_from_env_var: KUBE_NODE_NAME
    extract:
      metadata:
        - k8s.pod.name
        - k8s.namespace.name
        - k8s.deployment.name
        - k8s.statefulset.name
        - k8s.daemonset.name
        - k8s.cronjob.name
        - k8s.job.name
        - k8s.node.name
        - k8s.pod.uid
        - k8s.pod.start_time
  
  # 资源检测
  resourcedetection:
    detectors: [env, gcp, ec2, azure, system]
    timeout: 2s
  
  # 属性过滤
  filter:
    metrics:
      exclude:
        match_type: strict
        metric_names:
          - telemetry.sdk.*
          - process.*

exporters:
  # VictoriaMetrics 指标
  prometheusremotewrite:
    endpoint: http://victoriametrics:8428/api/v1/write
    timeout: 10s
    sending_queue:
      enabled: true
      queue_size: 10000
      num_consumers: 10
  
  # Loki 日志
  loki:
    endpoint: http://loki:3100/loki/api/v1/push
    timeout: 10s
    labels:
      attributes:
        - k8s.namespace.name
        - k8s.pod.name
        - k8s.container.name
        - level
  
  # Tempo 追踪
  otlp:
    endpoint: tempo:4317
    timeout: 10s
    tls:
      insecure: true
  
  # 开发调试
  debug:
    verbosity: detailed
    sampling_initial: 5
    sampling_thereafter: 200

extensions:
  health_check:
    endpoint: 0.0.0.0:13133
  zpages:
    endpoint: 0.0.0.0:55679

service:
  extensions: [health_check, zpages]
  
  pipelines:
    metrics:
      receivers: [prometheus, otlp]
      processors: [memory_limiter, batch, k8sattributes, resourcedetection, filter]
      exporters: [prometheusremotewrite, debug]
    
    logs:
      receivers: [otlp, filelog]
      processors: [memory_limiter, batch, k8sattributes, resourcedetection]
      exporters: [loki, debug]
    
    traces:
      receivers: [otlp, jmx]
      processors: [memory_limiter, batch, k8sattributes, resourcedetection]
      exporters: [otlp, debug]
  
  telemetry:
    logs:
      level: info
      initial_fields:
        service_name: otel-collector
    metrics:
      address: 0.0.0.0:8889
```

### 7.3 Loki 配置

```yaml
# config/loki-config.yaml
auth_enabled: false

server:
  http_listen_port: 3100
  grpc_listen_port: 9095

common:
  ring:
    instance_addr: 127.0.0.1
    kvstore:
      store: memberlist

memberlist:
  join_members:
    - loki

ingester:
  wal:
    enabled: true
    dir: /tmp/loki/wal
  lifecycler:
    ring:
      kvstore:
        store: memberlist
      replication_factor: 1
  chunk_encoding: snappy
  chunk_id_validation: disabled

limits_config:
  max_query_length: 720h
  max_query_parallelism: 32
  ingestion_rate_mb: 10
  ingestion_burst_size_mb: 20
  allow_structured_metadata: true
  volume_enabled: true

schema_config:
  configs:
    - from: "2024-04-01"
      store: tsdb
      object_store: s3
      schema: v13
      index:
        prefix: loki_index_
        period: 24h

storage_config:
  tsdb_shipper:
    active_index_directory: /tmp/loki/index
    cache_location: /tmp/loki/cache
  
  s3:
    endpoint: minio:9000
    insecure: true
    s3forcepathstyle: true
    bucketnames: loki-chunks

ruler:
  enable_api: true
  storage:
    s3:
      bucketnames: loki-ruler
    local:
      directory: /tmp/loki/rules
  rule_path: /tmp/loki/rules-temp

compactor:
  working_directory: /tmp/loki/compactor
  compaction_interval: 10m
  retention_enabled: true
  retention_delete_delay: 2h
  retention_delete_worker_count: 150

query_scheduler:
  max_outstanding_requests_per_tenant: 4096

querier:
  max_concurrent: 4
  query_ingesters_within: 15m

frontend:
  encoding: gzip
  max_outstanding_requests_per_tenant: 2000
  log_level: info
```

### 7.4 Tempo 配置

```yaml
# config/tempo-config.yaml
server:
  http_listen_port: 16687
  grpc_listen_port: 4317

distributor:
  receivers:                           # 配置接收器
    otlp:
      protocols:
        grpc:
        http:
    jaeger:
      protocols:
        thrift_http:
        grpc:
    zipkin:

ingester:
  trace_idle_period: 10s               # 空闲超时后刷新
  max_block_duration: 5m               # 最大块大小
  complete_block_timeout: 30m          # 块完成超时

querier:
  frontend_worker:
    frontend_address: tempo-query-frontend:9095

compactor:
  compaction:
    block_retention: 168h              # 保留7天
    max-objects-per-block: 10000000

storage:
  trace:
    backend: s3                        # 使用S3存储
    s3:
      endpoint: minio:9000
      insecure: true
      s3forcepathstyle: true
      bucket: tempo-traces
    cache:                             # 块缓存配置
      memcached:
        host: tempo-memcached
        service: memcached

overrides:
  defaults:
    max_traces_per_span_set: 10000
    max_span_set_size: 100000
```

### 7.5 Grafana 配置

```ini
# config/grafana.ini
[paths]
data = /var/lib/grafana/data
logs = /var/lib/grafana/logs
plugins = /var/lib/grafana/plugins
provisioning = /etc/grafana/provisioning

[server]
http_port = 3000
root_url = %(protocol)s://%(domain)s:%(http_port)s/
serve_from_sub_path = false

[security]
admin_user = admin
admin_password = admin123
disable_initial_admin_creation = false
secret_key = your-secret-key-change-in-production

[auth]
disable_login_form = false
disable_signout_menu = false

[users]
allow_sign_up = false
auto_assign_org = true
auto_assign_org_role = Viewer

[organizations]
enabled = true

[paths]
provisioning = /etc/grafana/provisioning

[metrics]
enabled = true
graphite_port = 3000

[external_image_storage]
provider = local

[snapshots]
enabled = true
external_enabled = true

[alerting]
enabled = true
execute_alerts = true

[log]
mode = console
level = info
format = json
```

---

## 8. 监控指标保留策略

### 8.1 默认保留策略

| 数据类型 | 存储组件 | 短期保留 | 长期保留 | 降采样 |
|---------|---------|---------|---------|-------|
| 指标 | VictoriaMetrics | 30天 | 365天 (可选) | 1h/24h |
| 日志 | Loki | 7天 | 90天 (S3) | - |
| 追踪 | Tempo | 7天 | 30天 (S3) | - |

### 8.2 存储容量估算

**参考公式**:
- 指标: 约 1.5KB/样本, 100万指标×10秒间隔×30天 ≈ 450GB
- 日志: 约 500字节/行, 10GB/天 × 7天 ≈ 70GB
- 追踪: 约 10KB/trace, 1000 traces/秒 × 7天 ≈ 6TB

---

## 9. 性能优化建议

### 9.1 VictoriaMetrics 优化

```yaml
# 推荐配置
- -storageDataPath=/vm-data
- -retentionPeriod=30d
- -httpListenAddr=:8428
- -prometheusListenAddr=:8429
- -search.maxUniqueTimeseries=1000000  # 根据实际调整
- -logNewSeries=1000  # 日志输出频率
- -memory.allowedPercent=80  # 内存使用限制
```

### 9.2 Loki 优化

```yaml
# 推荐配置
ingester:
  chunk_encoding: snappy
  max_chunk_age: 1h
  chunk_block_size: 1048576

querier:
  max_concurrent: 4
  query_ingesters_within: 15m

limits_config:
  max_query_length: 720h
  max_query_parallelism: 32
```

### 9.3 OTel Collector 优化

```yaml
processors:
  batch:
    timeout: 1s
    send_batch_size: 2048  # 批量大小
    send_batch_max_size: 4096
  
  memory_limiter:
    limit_mib: 2000  # 内存限制
    spike_limit_mib: 500
    check_interval: 1s
```

---

## 10. 安全考虑

### 10.1 网络安全

- 所有内部通信使用内网 IP
- 对外暴露端口使用 HTTPS
- 使用网络策略隔离组件

### 10.2 认证授权

- Grafana 使用 OAuth2/OIDC 认证
- API 使用 JWT Token 认证
- 基于角色的访问控制 (RBAC)

### 10.3 数据安全

- 敏感配置加密存储
- 审计日志记录所有操作
- 定期备份数据库

---

## 11. 运维手册

### 11.1 日常运维命令

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 重启服务
docker-compose restart victoriametrics

# 扩缩容 (Kubernetes)
kubectl scale deployment onemonitor-backend --replicas=3

# 数据备份
pg_dump -U admin onemonitor > backup.sql
```

### 11.2 故障排查

```bash
# 检查服务健康
curl http://localhost:8428/health
curl http://localhost:3100/loki/api/v1/status/buildinfo
curl http://localhost:3000/api/health

# 检查存储使用
docker exec -it onemonitor-victoriametrics df -h /vm-data
docker exec -it onemonitor-loki df -h /tmp/loki

# 检查 OTel Collector
curl http://localhost:8889/metrics
```

### 11.3 升级流程

1. 备份数据库
2. 更新 Docker Compose 文件
3. 拉取新镜像
4. 执行数据库迁移 (如有)
5. 滚动更新服务
6. 验证功能

---

## 12. 附录

### 12.1 数据源连接配置

| 数据源 | URL | 认证 |
|--------|-----|------|
| VictoriaMetrics | http://victoriametrics:8428 | 无 |
| Loki | http://loki:3100 | 无 |
| Tempo | http://tempo:4317 | 无 |
| Grafana | http://grafana:3000 | admin/admin123 |
| PostgreSQL | postgres:5432 | admin/admin123 |
| Redis | redis:6379 | onemonitor123 |

### 12.2 端口映射

| 组件 | 端口 | 协议 | 用途 |
|------|------|------|------|
| Backend | 8000 | HTTP | 业务 API |
| Frontend | 80 | HTTP | Web 界面 |
| Grafana | 3000 | HTTP | 可视化 |
| VictoriaMetrics | 8428 | HTTP | 指标查询 |
| Loki | 3100 | HTTP | 日志查询 |
| Tempo | 4317/4318 | gRPC/HTTP | 追踪收集 |
| OTel Collector | 4317/4318 | gRPC/HTTP | 数据采集 |
| PostgreSQL | 5432 | TCP | 数据库 |
| Redis | 6379 | TCP | 缓存 |
| MinIO | 9000/9001 | HTTP | 对象存储 |

### 12.3 默认账号

| 服务 | 用户名 | 密码 |
|------|--------|------|
| Grafana | admin | admin123 |
| PostgreSQL | admin | admin123 |
| Redis | - | onemonitor123 |
| MinIO | admin | admin123 |
| OneMonitor | admin | admin123 |

---

*文档版本: 1.0*  
*更新日期: 2024-01-21*
*作者: OneMonitor Team*
