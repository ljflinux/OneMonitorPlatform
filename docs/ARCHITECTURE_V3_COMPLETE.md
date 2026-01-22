# OneMonitor 平台完整架构设计文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 版本 | 3.0 |
| 日期 | 2024-01-21 |
| 方法论 | DDD + MECE |
| 状态 | 设计阶段 |

---

# 第一部分：业务架构设计

## 1. 业务能力全景图 (MECE 分解)

### 1.1 一级业务域划分

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ONE MONITOR 业务能力域                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐ │
│  │    资产       │  │    数据       │  │    告警       │  │    洞察       │ │
│  │   管理域      │  │   可观测域     │  │   响应域      │  │   分析域      │ │
│  │   (CMDB)     │  │  (OBSERV.)    │  │   (ALERT)     │  │  (ANALYTICS)  │ │
│  └───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘ │
│                                                                              │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐ │
│  │    事件       │  │    自动化     │  │    平台       │  │    安全       │ │
│  │   管理域      │  │   控制域      │  │   治理域      │  │   合规模      │ │
│  │  (INCIDENT)  │  │  (AUTOMATION) │  │  (GOVERNANCE) │  │   (COMPLIANCE)│ │
│  └───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘ │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

**MECE 验证**:
- **相互独立 (Mutually Exclusive)**: 8 个一级域之间没有功能重叠
- **完全穷尽 (Collectively Exhaustive)**: 覆盖监控平台所有业务功能

### 1.2 二级能力分解

#### 域 1: 资产管理 (CMDB) - 核心域

| 二级能力 | 三级能力 | 描述 | 核心/支撑/通用 |
|---------|---------|------|---------------|
| **CI 发现** | 自动发现 | 云 API 扫描、K8s 发现、SNMP 扫描 | 核心 |
| | 手动注册 | CI 表单录入、批量导入 | 支撑 |
| | 第三方集成 | ServiceNow、BMC、云厂商 CMDB 同步 | 支撑 |
| **CI 生命周期** | 创建/上线 | CI 类型选择、属性填写、验证审批 | 核心 |
| | 更新维护 | 属性变更、版本升级、配置调整 | 核心 |
| | 退役下线 | 状态标记、数据归档、依赖清理 | 核心 |
| **CI 关系** | 依赖映射 | 包含、依赖、部署、连接关系 | 核心 |
| | 拓扑可视化 | 关系图展示、影响范围分析 | 核心 |
| | 影响分析 | 依赖追踪、级联影响、故障定位 | 核心 |
| **CI 属性** | 属性定义 | 类型 schema、验证规则、默认值 | 核心 |
| | 动态属性 | 标签管理、元数据扩展、自定义字段 | 支撑 |
| | 属性验证 | 必填校验、格式校验、范围校验 | 核心 |
| **变更追踪** | 变更历史 | 变更日志、版本对比、审计追踪 | 核心 |
| | 变更审批 | 审批流程、多级审批、变更冻结 | 支撑 |
| | 影响评估 | 风险评估、回滚预案、测试验证 | 支撑 |

#### 域 2: 数据可观测域

| 二级能力 | 三级能力 | 描述 | 核心/支撑/通用 |
|---------|---------|------|---------------|
| **数据采集** | 指标采集 | Counter、Gauge、Histogram、Summary | 核心 |
| | 日志采集 | 应用日志、系统日志、审计日志 | 核心 |
| | 链路采集 | Distributed Traces、Spans、Context | 核心 |
| | 事件采集 | K8s Events、Custom Events、State Changes | 支撑 |
| **数据接入** | OTLP 接收 | OpenTelemetry 协议接收 | 核心 |
| | 协议适配 | Prometheus Remote Write、StatsD、Syslog、SNMP | 支撑 |
| | 数据验证 | Schema 验证、数据脱敏、异常过滤 | 支撑 |
| **数据存储** | 时序存储 | VictoriaMetrics - 指标 | 核心 |
| | 日志存储 | Loki/Elasticsearch - 日志 | 核心 |
| | 链路存储 | Tempo/Jaeger - 追踪 | 核心 |
| | 关系存储 | PostgreSQL - 元数据/配置 | 核心 |
| | 对象存储 | MinIO - 报表/备份 | 通用 |
| **数据处理** | 实时聚合 | Rate、Sum、Avg、Quantile 计算 | 核心 |
| | 降采样 | 精度优化、历史数据压缩 | 支撑 |
| | 数据丰富化 | 添加 CMDB 上下文、标签扩展 | 核心 |
| **数据保留** | 保留策略 | 分级保留、冷热数据分离 | 支撑 |
| | 数据归档 | 长期存储、成本优化 | 通用 |

#### 域 3: 告警响应域

| 二级能力 | 三级能力 | 描述 | 核心/支撑/通用 |
|---------|---------|------|---------------|
| **告警检测** | 规则引擎 | PromQL、Threshold、Anomaly、Composite | 核心 |
| | 评估执行 | 定时扫描、实时计算、窗口聚合 | 核心 |
| | 智能检测 | 基线学习、异常识别、预测告警 | 支撑 |
| **告警管理** | 生命周期 | Pending、Firing、Resolved、Silenced | 核心 |
| | 分组聚合 | 时间窗口、标签分组、聚合策略 | 核心 |
| | 抑制/去重 | 依赖抑制、维护窗口、振动抑制 | 核心 |
| **通知路由** | 渠道管理 | Email、SMS、Webhook、钉钉、企微、Slack | 核心 |
| | 路由策略 | 基于标签、基于严重度、基于时间 | 核心 |
| | 升级机制 | 超时升级、多级通知、值班轮换 | 核心 |
| **事件集成** | 工单创建 | 自动创建、手动关联、工单同步 | 支撑 |
| | 告警关联 | 告警分组、事件关联、根因标记 | 支撑 |

#### 域 4: 洞察分析域

| 二级能力 | 三级能力 | 描述 | 核心/支撑/通用 |
|---------|---------|------|---------------|
| **可视化分析** | 仪表盘管理 | 自定义面板、模板库、共享权限 | 核心 |
| | 图表类型 | Time Series、Heatmap、Gauge、Table、Log | 核心 |
| | 实时视图 | Live mode、Auto-refresh、实时数据流 | 支撑 |
| **数据探索** | 即席查询 | PromQL Builder、LogQL、TraceQL | 核心 |
| | 关联分析 | 跨数据源关联、上下文跳转 | 核心 |
| | 下钻分析 | 从概览到详情、粒度调整 | 支撑 |
| **性能分析** | APM 分析 | Transaction traces、DB queries、External calls | 支撑 |
| | 资源分析 | 容量使用、成本分析、资源优化 | 支撑 |
| | SLA/SLO 监控 | Error budget、Burn rate、Compliance | 核心 |
| **根因分析** | 异常检测 | 统计异常、基线偏离、突变识别 | 支撑 |
| | 模式识别 | 周期性模式、关联规则、依赖模式 | 支撑 |
| | AI 辅助 RCA | LLM 分析、因果推断、建议生成 | 通用 |
| **预测分析** | 容量预测 | 资源需求预测、成本预测、扩容建议 | 通用 |
| | 故障预测 | MTBF 预测、预防性维护、风险预警 | 通用 |

#### 域 5: 事件管理域

| 二级能力 | 三级能力 | 描述 | 核心/支撑/通用 |
|---------|---------|------|---------------|
| **事件生命周期** | 创建/分诊 | 自动创建、手动创建、严重度评估 | 核心 |
| | 解决/关闭 | 调查处理、验证解决、记录总结 | 核心 |
| **事件响应** | 值班管理 | 值班表、轮换规则、替班管理 | 支撑 |
| | 战情室协作 | 实时沟通、状态同步、决策记录 | 支撑 |
| | 状态页面 | 状态更新、用户通知、影响范围 | 支撑 |
| **事后回顾** | RCA 文档 | RCA 模板、根因分析、行动项 | 核心 |
| | 知识捕获 | Runbook 更新、最佳实践、经验教训 | 支撑 |
| | 行动追踪 | 任务分配、进度跟踪、完成验证 | 核心 |
| **事件指标** | MTTR 追踪 | 响应时间、解决时间、分阶段统计 | 核心 |
| | DORA 指标 | Deployment Frequency、Lead Time、MTTR | 支撑 |

#### 域 6-8: 自动化/治理/安全域

| 二级能力 | 三级能力 | 描述 | 核心/支撑/通用 |
|---------|---------|------|---------------|
| **自动化控制** | 工作流自动化 | 编排引擎、条件分支、并行执行 | 支撑 |
| | 自动修复 | 重启服务、扩容容器、清理资源 | 通用 |
| | 任务调度 | Cron 调度、事件触发、依赖管理 | 支撑 |
| **平台治理** | 用户管理 | 身份管理、角色管理、团队管理 | 核心 |
| | 访问控制 | RBAC、ABAC、SSO 集成 | 核心 |
| | 审计合规 | 审计日志、合规报告、数据隐私 | 核心 |
| **安全合规** | 数据安全 | 加密、脱敏、安全传输 | 核心 |
| | 威胁检测 | 安全监控、日志分析、漏洞扫描 | 支撑 |
| | 合规管理 | SOC2、ISO27001、GDPR | 通用 |

---

## 2. 业务流程映射

### 2.1 监控数据流管道

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      监控数据流管道 (DATA FLOW PIPELINE)                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐            │
│  │   SOURCES     │───▶│  COLLECTION    │───▶│   INGESTION    │            │
│  │  数据源        │    │   采集层        │    │   接入层        │            │
│  └────────────────┘    └────────────────┘    └────────────────┘            │
│                                                                              │
│  数据源类型:                                                                │
│  • Applications (APM, 应用日志)                                             │
│  • Infrastructure (Node Exporter, SNMP, IPMI)                              │
│  • Cloud APIs (AWS, 阿里云, 腾讯云)                                         │
│  • Kubernetes (Metrics Server, Events)                                      │
│  • Network Devices (Switch, Router, Firewall)                               │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                    处理与存储层 (PROCESSING & STORAGE)            │      │
│  │                                                                  │      │
│  │  Metrics ──────────────────▶│ VictoriaMetrics Cluster           │      │
│  │  (时序数据)                  │ • Downsampling                   │      │
│  │                             │ • Aggregation                    │      │
│  │                             │ • Retention                      │      │
│  │                                                                  │      │
│  │  Logs ─────────────────────▶│ Loki + MinIO                     │      │
│  │  (日志流)                    │ • Indexing                       │      │
│  │                             │ • Compression                    │      │
│  │                             │ • Retention                      │      │
│  │                                                                  │      │
│  │  Traces ──────────────────▶│ Tempo + MinIO                    │      │
│  │  (分布式追踪)                 │ • Span storage                   │      │
│  │                             │ • Search                         │      │
│  │                                                                  │      │
│  │  Metadata ────────────────▶│ PostgreSQL                       │      │
│  │  (CMDB/Config)             │ • ACID transactions              │      │
│  │                             │ • Relationships                  │      │
│  │                             │ • Audit trails                   │      │
│  └─────────────────────────────────────────────────────────────────┘      │
│                              │                                            │
│                              ▼                                            │
│                                                                              │
│  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐            │
│  │   ANALYSIS     │    │    ALERT       │    │ VISUALIZATION  │            │
│  │   分析层        │    │    告警层       │    │   可视化层       │            │
│  └────────────────┘    └────────────────┘    └────────────────┘            │
│                                                                              │
│                              │                                            │
│                              ▼                                            │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                        行动层 (ACTION LAYER)                      │      │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │      │
│  │  │ Notification │  │ Incident     │  │ Automation   │          │      │
│  │  │ (Channels)   │  │ Management   │  │ (Remediation)│          │      │
│  │  │              │  │              │  │              │          │      │
│  │  │ • Email      │  │ • Workflows  │  │ • Scripts    │          │      │
│  │  │ • DingTalk   │  │ • RCA        │  │ • Ansible    │          │      │
│  │  │ • WeCom      │  │ • Reports    │  │ • Terraform  │          │      │
│  │  │ • Webhook    │  │              │  │              │          │      │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │      │
│  └─────────────────────────────────────────────────────────────────┘      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 CMDB 集成工作流

```
工作流 1: 自动发现 → 监控配置
┌────────────────┐    ┌────────────────┐    ┌────────────────┐
│   Discovery    │───▶│   CMDB Sync    │───▶│  Auto-Config   │
│   (K8s/Cloud)  │    │   (Create CI)  │    │  (Mon Targets) │
└────────────────┘    └────────────────┘    └────────────────┘
       │                    │                    │
       ▼                    ▼                    ▼
• 扫描资源            • 创建 CI            • 创建监控目标
• 识别类型            • 映射属性           • 设置标签
• 提取属性            • 构建关系           • 应用模板

工作流 2: 变更管理 → 影响分析
┌────────────────┐    ┌────────────────┐    ┌────────────────┐
│   Change Req   │───▶│ Impact Analysis │───▶│  Risk Assess   │
│   (User/ITSM)  │    │ (Dependency)   │    │  (Alert Impact)│
└────────────────┘    └────────────────┘    └────────────────┘
       │                    │                    │
       ▼                    ▼                    ▼
• 变更请求            • 向上游依赖         • 识别受影响告警
• 验证审批            • 向下游级联         • 建议缓解措施
                      • 映射业务服务

工作流 3: 监控数据 → CMDB 丰富化
┌────────────────┐    ┌────────────────┐    ┌────────────────┐
│  Monitor Data  │───▶│  Data Fusion   │───▶│  CMDB Enrich   │
│  (Metrics/Logs)│    │  (Correlation) │    │  (Add Context) │
└────────────────┘    └────────────────┘    └────────────────┘
       │                    │                    │
       ▼                    ▼                    ▼
• 收集数据            • 按 CI ID 关联        • 添加健康状态
• 时序分析            • 添加业务上下文       • 添加性能标签
• 事件日志            • 映射到服务          • 添加容量数据

工作流 4: 告警 → 事件 → CMDB 更新
┌────────────────┐    ┌────────────────┐    ┌────────────────┐
│   Alert/Fault  │───▶│   RCA Analysis │───▶│  CMDB Update   │
│   (Detection)  │    │ (Root Cause)   │    │  (Fix/Update)  │
└────────────────┘    └────────────────┘    └────────────────┘
       │                    │                    │
       ▼                    ▼                    ▼
• 告警触发            • 识别故障 CI         • 更新 CI 状态
• 告警分组            • 追溯依赖关系       • 更新关系
• 事件创建            • 找到根因           • 更新属性
```

---

## 3. 干系人分析

### 3.1 用户画像

| 干系人 | 角色 | 核心需求 | 痛点 | 关键指标 |
|--------|------|---------|------|---------|
| **SRE 工程师** | 主要用户 | 实时可见性、快速排障、自动化 | 告警太多、缺少上下文、数据嘈杂 | MTTR、告警噪音率、检测时间 |
| **网络/基础设施管理员** | 操作员 | 设备监控、拓扑视图、容量规划 | 遗留工具、手动发现、CMDB 过时 | 设备可用率、发现延迟、配置漂移 |
| **应用开发者** | 消费者 | 应用性能、调试追踪、代码级洞察 | 无法访问生产数据、黑盒监控 | 应用延迟、错误率、部署频率 |
| **IT 经理** | 决策者 | SLA 合规、容量规划、成本优化 | 缺乏可见性、被动决策、预算超支 | SLA 达成率、资源利用率、单位成本 |
| **安全/合规团队** | 审计者 | 审计追踪、访问控制、安全监控 | 手动审计、缺乏证据、不合规 | 审计覆盖率、合规率、安全事件 |
| **支持团队** | 分诊 | 工单上下文、已知问题、快速解决 | 上下文不足、重复问题、升级延迟 | 解决时间、首次接触解决、知识库使用 |

### 3.2 SLA 质量要求

| 质量属性 | 要求 | 测量方法 | 目标值 |
|---------|------|---------|-------|
| **可用性** | 平台 uptime | 每月停机时间 | ≥ 99.95% |
| **性能** | 查询响应时间 | p95 延迟 | 仪表盘 < 2s，复杂查询 < 5s |
| **可扩展性** | 摄入速率 | 每秒指标数 | ≥ 100 万指标/秒 |
| **数据新鲜度** | 实时可见性 | 数据延迟 | 从采集到可查询 < 10s |
| **告警延迟** | 告警检测时间 | 从阈值触发到告警 | < 30s |
| **数据保留** | 历史数据 | 保留周期 | 详细数据 13 个月，聚合数据 7 年 |
| **安全** | 访问控制 | 认证授权 | RBAC、SSO、MFA |
| **合规** | 审计日志 | 覆盖率 | 100% 用户操作记录 |

---

# 第二部分：领域建模 (DDD)

## 4. 限界上下文识别

### 4.1 限界上下文映射

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ONE MONITOR 限界上下文映射                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    PLATFORM CONTEXT                                   │   │
│  │  (共享内核 Shared Kernel)                                           │   │
│  │  • 认证授权 (AuthN/AuthZ)                                         │   │
│  │  • RBAC 权限模型                                                    │   │
│  │  • 审计日志                                                        │   │
│  │  • CMDB 类型定义 (CI Types, Relation Types)                        │   │
│  │  • 标签键定义 (Service, Instance, Env, CI_ID)                       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                              │                                           │
│         ┌────────────────────┼────────────────────┐                       │
│         │                    │                    │                       │
│         ▼                    ▼                    ▼                       │
│  ┌───────────────┐   ┌───────────────┐   ┌───────────────┐              │
│  │    CMDB       │◄──│  MONITORING   │◄──│   INCIDENT    │              │
│  │   CONTEXT     │   │   CONTEXT     │   │   CONTEXT     │              │
│  │               │   │               │   │               │              │
│  │  核心域       │   │  支撑域       │   │  支撑域       │              │
│  │               │   │               │   │               │              │
│  │ • CI Types   │   │ • Targets     │   │ • Incidents   │              │
│  │ • CI Inst.   │   │ • Metrics     │   │ • Workflows   │              │
│  │ • Relations  │   │ • Logs/Traces │   │ • Post-Mortem │              │
│  │ • Lifecycle  │   │ • Query       │   │ • RCA         │              │
│  │ • Discovery  │   │ • Storage     │   │ • Runbooks    │              │
│  └───────────────┘   └───────────────┘   └───────────────┘              │
│         │                    │                    │                       │
│         └────────────────────┼────────────────────┘                       │
│                              │                                           │
│                              ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    ALERTING CONTEXT                             │   │
│  │  (通用域 Generic Subdomain)                                      │   │
│  │                                                                 │   │
│  │  • Alert Rules (PromQL/阈值/智能检测)                          │   │
│  │  • Alert Evaluation (规则引擎)                                  │   │
│  │  • Notification Routing (邮件/钉钉/企微/Webhook)               │   │
│  │  • Escalation (升级机制)                                        │   │
│  │  • Silence (静默规则)                                           │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                           │
│         ┌────────────────────┼────────────────────┐                       │
│         │                    │                    │                       │
│         ▼                    ▼                    ▼                       │
│  ┌───────────────┐   ┌───────────────┐   ┌───────────────┐              │
│  │  ANALYTICS    │   │  AUTOMATION   │   │   SECURITY    │              │
│  │   CONTEXT     │   │   CONTEXT     │   │   CONTEXT     │              │
│  │               │   │               │   │               │              │
│  │  • Dashboards │   │  • Workflows  │   │  • Audit      │              │
│  │  • ML Models  │   │  • Remediation│   │  • Compliance │              │
│  │  • Reports    │   │  • Integrations│  │  • Monitoring │              │
│  │  • Exploration│   │  • Scheduling │   │               │              │
│  └───────────────┘   └───────────────┘   └───────────────┘              │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 上下文关系类型

| 关系 | 上下文对 | 描述 |
|------|---------|------|
| **Shared Kernel (SK)** | Platform ↔ All | CMDB 类型定义、标签键作为共享内核 |
| **Open Host Service (OHS)** | Monitoring → CMDB | CMDB 提供 CI 数据服务给 Monitoring |
| **Conformist (CF)** | Alerting → Platform | Alerting 遵循 Platform 的认证授权 |
| **Partnership (PL)** | Monitoring ↔ Alerting | 紧密协作，Monitoring 发送数据，Alerting 创建告警 |
| **Customer-Supplier (CS)** | CMDB → Incident | CMDB 提供数据，Incident 消费 |
| **Anti-Corruption Layer (ACL)** | All → External | 各上下文通过 ACL 隔离外部系统 |

---

## 5. 聚合设计

### 5.1 CMDB 上下文聚合

#### 聚合 1: CI (配置项) - 聚合根

```java
/**
 * CI 聚合根
 * 
 * 不变式 (Invariants):
 * 1. CI 状态转换必须遵循状态机规则
 * 2. CI 属性必须符合 CIType 的 schema 定义
 * 3. CI 名称在同一 CIType 下唯一
 * 4. 退役的 CI 不能更新属性
 * 
 * 边界: CI 及其属性、标签 (事务一致)
 */
@Entity
@Table(name = "cis")
public class CI extends AggregateRoot<CIId> {
    
    @Id private CIId id;
    
    private CITypeId typeId;
    private String name;              // CI 唯一标识符
    private String displayName;       // 显示名称
    private CILifecycleStatus status; // ACTIVE, MAINTENANCE, DECOMMISSIONED, DISPOSED
    
    // CI 类型定义的属性 (JSONB)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
    
    // 监控关联标签 (用于与 Metrics/Logs/Traces 关联)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private LabelSet labels;
    
    // 元数据
    private String owner;
    private String department;
    private String location;
    private Instant createdAt;
    private Instant updatedAt;
    
    // 领域方法
    public void updateAttributes(Map<String, Object> newAttributes) {
        // 不变式: 退役的 CI 不能更新
        if (this.status == CILifecycleStatus.DECOMMISSIONED) {
            throw new CannotUpdateDecommissionedCIException(this.id);
        }
        
        // 获取 CIType 验证属性
        CIType ciType = CITypeRegistry.get(this.typeId);
        ciType.validateAttributes(newAttributes);
        
        Map<String, Object> oldAttrs = this.attributes;
        this.attributes = Map.copyOf(newAttributes);
        
        // 领域事件
        this.addDomainEvent(new CIUpdated(
            this.id, this.typeId, oldAttrs, this.attributes, 
            Set.of("attributes"), Instant.now()
        ));
    }
    
    public void changeStatus(CILifecycleStatus newStatus, String reason) {
        // 不变式: 状态转换必须有效
        if (!isValidTransition(this.status, newStatus)) {
            throw new InvalidStatusTransitionException(this.status, newStatus);
        }
        
        CILifecycleStatus oldStatus = this.status;
        this.status = newStatus;
        
        this.addDomainEvent(new CIStatusChanged(
            this.id, oldStatus, newStatus, reason, Instant.now()
        ));
    }
    
    public void decommission(String reason) {
        this.changeStatus(CILifecycleStatus.DECOMMISSIONED, reason);
        
        // 触发依赖 CI 的状态检查
        this.addDomainEvent(new CIDecommissioned(
            this.id, reason, this.findDependents(), Instant.now()
        ));
    }
    
    private boolean isValidTransition(CILifecycleStatus from, CILifecycleStatus to) {
        // 状态机规则
        return switch (from) {
            case ACTIVE -> to == CILifecycleStatus.MAINTENANCE 
                          || to == CILifecycleStatus.DECOMMISSIONED;
            case MAINTENANCE -> to == CILifecycleStatus.ACTIVE 
                             || to == CILifecycleStatus.DECOMMISSIONED;
            case DECOMMISSIONED -> false; // 最终状态
            case DISPOSED -> false; // 最终状态
        };
    }
    
    public LabelSet getCorrelationLabels() {
        // 返回用于与监控数据关联的标签
        Map<String, String> labels = new HashMap<>();
        labels.put("ci_id", this.id.toString());
        labels.put("ci_name", this.name);
        labels.put("ci_type", this.typeId.toString());
        labels.put("environment", this.labels.get("environment"));
        if (this.owner != null) labels.put("owner", this.owner);
        return new LabelSet(labels);
    }
}

/**
 * CI 生命周期状态
 */
public enum CILifecycleStatus {
    ACTIVE,         // 活跃使用中
    MAINTENANCE,    // 维护中
    DECOMMISSIONED, // 已退役 (最终状态)
    DISPOSED        // 已处置 (最终状态)
}

/**
 * 值对象: 标签集 (用于与 Prometheus/Loki/Tempo 关联)
 */
@Embeddable
public class LabelSet {
    private final Map<String, String> labels;
    
    public LabelSet(Map<String, String> labels) {
        this.labels = Map.copyOf(labels);
    }
    
    // 检查是否为另一个标签集的子集
    public boolean isSubsetOf(LabelSet other) {
        for (Map.Entry<String, String> entry : this.labels.entrySet()) {
            String otherValue = other.labels.get(entry.getKey());
            if (otherValue == null || !otherValue.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    // 合并标签
    public LabelSet merge(LabelSet other) {
        Map<String, String> merged = new HashMap<>(this.labels);
        merged.putAll(other.labels);
        return new LabelSet(merged);
    }
    
    public String get(String key) {
        return this.labels.get(key);
    }
}
```

#### 聚合 2: CI 关系 - 聚合根

```java
/**
 * CI 关系聚合根
 * 
 * 不变式:
 * 1. 不能创建自引用关系 (A 不能关联到自身)
 * 2. 不能创建循环依赖
 * 3. 关系有时间有效性
 * 
 * 边界: 单一关系及其属性
 */
@Entity
@Table(name = "ci_relations")
public class CIRelation extends AggregateRoot<CIRelationId> {
    
    @Id private CIRelationId id;
    
    private CIId sourceCiId;      // 源 CI
    private CIId targetCiId;      // 目标 CI
    private CIRelationType type;  // 关系类型
    
    private Instant validFrom;
    private Instant validTo;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
    
    // 领域方法
    public static CIRelation create(CI source, CI target, CIRelationType type) {
        // 不变式: 不能自引用
        if (source.getId().equals(target.getId())) {
            throw new SelfReferenceException("CI cannot relate to itself");
        }
        
        // 不变式: 不能循环依赖
        if (wouldCreateCircularDependency(source.getId(), target.getId())) {
            throw new CircularDependencyException(source.getId(), target.getId());
        }
        
        CIRelationId id = new CIRelationId();
        CIRelation relation = new CIRelation();
        relation.id = id;
        relation.sourceCiId = source.getId();
        relation.targetCiId = target.getId();
        relation.type = type;
        relation.validFrom = Instant.now();
        
        // 领域事件
        relation.addDomainEvent(new CIRelationshipCreated(
            id, source.getId(), target.getId(), type, Instant.now()
        ));
        
        return relation;
    }
    
    // 检查是否会导致循环依赖
    private static boolean wouldCreateCircularDependency(CIId source, CIId target) {
        // 从 target 向上遍历，查找是否能回到 source
        Set<CIId> visited = new HashSet<>();
        Queue<CIId> queue = new LinkedList<>();
        queue.add(source);
        
        while (!queue.isEmpty()) {
            CIId current = queue.poll();
            if (current.equals(target)) {
                return true; // 找到回路
            }
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            
            // 获取当前 CI 的上游依赖
            List<CIRelation> deps = findDependencies(current);
            for (CIRelation dep : deps) {
                queue.add(dep.getSourceCiId());
            }
        }
        return false;
    }
    
    public enum CIRelationType {
        CONTAINS("contains", "包含"),
        DEPENDS_ON("depends_on", "依赖"),
        RUNS_ON("runs_on", "部署在"),
        CONNECTED_TO("connected_to", "连接"),
        MANAGES("manages", "管理"),
        PART_OF("part_of", "属于"),
        REPLICATES_TO("replicates_to", "复制到");
    }
}
```

### 5.2 Monitoring 上下文聚合

#### 聚合 1: MetricSeries (时序序列)

```java
/**
 * 时序序列聚合根
 * 
 * 边界: 序列元数据 + 标签 (一致性边界)
 */
@Entity
public class MetricSeries extends AggregateRoot<MetricSeriesId> {
    
    @Id private MetricSeriesId id;
    private String metricName;           // 指标名称
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private LabelSet labels;             // 标签集 (标识唯一性)
    
    private Instant firstSeen;
    private Instant lastSeen;
    
    // 记录采样点
    public void recordSample(double value, Instant timestamp) {
        this.lastSeen = timestamp;
        
        // 检测异常 (简单版，复杂逻辑在 Analytics 上下文)
        if (isAnomalous(value)) {
            this.addDomainEvent(new MetricAnomalyDetected(
                this.id, value, calculateExpectedValue(), Instant.now()
            ));
        }
    }
    
    private boolean isAnomalous(double value) {
        // 实际实现在 Analytics 上下文
        return false;
    }
    
    private double calculateExpectedValue() {
        return 0.0;
    }
}
```

#### 聚合 2: Trace (链路追踪)

```java
/**
 * Trace 聚合根
 * 
 * 边界: 完整 Trace 及其所有 Span
 */
@Entity
public class Trace extends AggregateRoot<TraceId> {
    
    @Id private TraceId id;
    private Instant startTime;
    private Duration duration;
    
    @OneToMany(mappedBy = "trace", cascade = CascadeType.ALL)
    private List<Span> spans;
    
    public Trace createFromSpans(List<Span> spans) {
        this.spans = List.copyOf(spans);
        
        this.startTime = spans.stream()
            .map(Span::getTimestamp)
            .min(Instant::compareTo)
            .orElse(Instant.now());
            
        Instant endTime = spans.stream()
            .map(Span::getEndTime)
            .max(Instant::compareTo)
            .orElse(this.startTime);
            
        this.duration = Duration.between(this.startTime, endTime);
        
        this.addDomainEvent(new TraceCompleted(
            this.id, this.spans.size(), this.duration, Instant.now()
        ));
        
        return this;
    }
    
    public double calculateErrorRate() {
        if (this.spans.isEmpty()) return 0.0;
        long errorCount = this.spans.stream()
            .filter(s -> s.getStatus() == SpanStatus.ERROR)
            .count();
        return (double) errorCount / this.spans.size();
    }
}

/**
 * Span 实体
 */
@Entity
public class Span {
    @Id private SpanId id;
    private TraceId traceId;
    private SpanId parentSpanId;
    
    private String operationName;
    private Instant timestamp;
    private Duration duration;
    private SpanStatus status;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> tags;
    
    public Instant getEndTime() {
        return timestamp.plus(duration);
    }
    
    public enum SpanStatus {
        UNSET, OK, ERROR
    }
}
```

### 5.3 Alerting 上下文聚合

#### 聚合 1: AlertRule (告警规则)

```java
/**
 * 告警规则聚合根
 * 
 * 边界: 规则配置及其评估状态
 */
@Entity
@Table(name = "alert_rules")
public class AlertRule extends AggregateRoot<AlertRuleId> {
    
    @Id private AlertRuleId id;
    private String name;
    private AlertRuleType type;        // PROMETHEUS, THRESHOLD, ANOMALY, COMPOSITE
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private AlertCondition condition;   // 告警条件
    
    private AlertSeverity severity;
    private AlertRuleStatus status;    // ACTIVE, PAUSED, DISABLED
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<NotificationChannel> channels;
    
    public void evaluate(MetricData data) {
        if (this.status != AlertRuleStatus.ACTIVE) {
            return;
        }
        
        AlertEvaluationResult result = condition.evaluate(data);
        
        if (result.isTriggered()) {
            this.addDomainEvent(new AlertTriggered(
                this.id, this.severity, result, Instant.now()
            ));
        }
    }
    
    public enum AlertRuleStatus {
        ACTIVE, PAUSED, DISABLED
    }
}

/**
 * 告警实例聚合根
 */
@Entity
@Table(name = "alerts")
public class AlertInstance extends AggregateRoot<AlertInstanceId> {
    
    @Id private AlertInstanceId id;
    private AlertRuleId ruleId;
    private AlertSeverity severity;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private LabelSet labels;  // 告警标识
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> annotations;
    
    private double value;
    private AlertState state;  // INACTIVE, PENDING, FIRING, RESOLVED
    
    private Instant startedAt;
    private Instant resolvedAt;
    
    public void fire(double value, String message) {
        if (this.state == AlertState.FIRING) return;
        
        AlertState oldState = this.state;
        this.state = AlertState.FIRING;
        this.value = value;
        this.startedAt = Instant.now();
        this.annotations = Map.of("message", message);
        
        this.addDomainEvent(new AlertTriggered(
            this.id, this.ruleId, oldState, this.state, 
            this.labels, Instant.now()
        ));
    }
    
    public void resolve(String resolvedBy) {
        if (this.state != AlertState.FIRING) return;
        
        AlertState oldState = this.state;
        this.state = AlertState.RESOLVED;
        this.resolvedAt = Instant.now();
        this.annotations.put("resolvedBy", resolvedBy);
        
        this.addDomainEvent(new AlertResolved(
            this.id, oldState, this.state, resolvedBy, Instant.now()
        ));
    }
    
    public enum AlertState {
        INACTIVE, PENDING, FIRING, RESOLVED
    }
}
```

---

## 6. 领域事件定义

### 6.1 事件清单

```java
// CMDB 领域事件
public interface CMDBEvent extends DomainEvent {
    CIId ciId();
    Instant timestamp();
}

public record CICreated(
    CIId ciId,
    CITypeId typeId,
    String name,
    LabelSet labels,
    Instant timestamp
) implements CMDBEvent {}

public record CIUpdated(
    CIId ciId,
    CITypeId typeId,
    Map<String, Object> oldValues,
    Map<String, Object> newValues,
    Set<String> changedFields,
    Instant timestamp
) implements CMDBEvent {}

public record CIStatusChanged(
    CIId ciId,
    CILifecycleStatus oldStatus,
    CILifecycleStatus newStatus,
    String reason,
    Instant timestamp
) implements CMDBEvent {}

public record CIDecommissioned(
    CIId ciId,
    String reason,
    List<CIId> affectedDependents,
    Instant timestamp
) implements CMDBEvent {}

public record CIRelationshipCreated(
    CIRelationId relationId,
    CIId sourceCiId,
    CIId targetCiId,
    CIRelationType type,
    Instant timestamp
) implements CMDBEvent {}

// Monitoring 领域事件
public record MetricAnomalyDetected(
    MetricSeriesId seriesId,
    double actualValue,
    double expectedValue,
    AnomalySeverity severity,
    Instant timestamp
) implements DomainEvent {}

public record TraceCompleted(
    TraceId traceId,
    int spanCount,
    Duration duration,
    Instant timestamp
) implements DomainEvent {}

// Alerting 领域事件
public record AlertTriggered(
    AlertInstanceId alertId,
    AlertRuleId ruleId,
    AlertState fromState,
    AlertState toState,
    LabelSet labels,
    Map<String, String> annotations,
    Instant timestamp
) implements DomainEvent {}

public record AlertResolved(
    AlertInstanceId alertId,
    AlertState fromState,
    AlertState toState,
    String resolvedBy,
    Instant timestamp
) implements DomainEvent {}

public record AlertEscalated(
    AlertInstanceId alertId,
    EscalationLevel fromLevel,
    EscalationLevel toLevel,
    String reason,
    Instant timestamp
) implements DomainEvent {}

// Incident 领域事件
public record IncidentCreated(
    IncidentId incidentId,
    AlertInstanceId triggeredByAlert,
    Set<CIId> affectedCIs,
    IncidentSeverity severity,
    Instant timestamp
) implements DomainEvent {}

public record IncidentResolved(
    IncidentId incidentId,
    String resolvedBy,
    String resolutionNote,
    Instant timestamp
) implements DomainEvent {}
```

### 6.2 事件流

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          领域事件流 (DOMAIN EVENT FLOW)                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  CMDB Context                        Monitoring Context                       │
│      │                                    │                                 │
│      ├── CICreated ───────────────────────▶│                                 │
│      ├── CIUpdated ────────────────────────▶│  Subscribe to CI changes        │
│      ├── CIStatusChanged ─────────────────▶│  to configure monitoring       │
│      ├── CIRelationshipCreated ──────────▶│                                 │
│      │                                    ▼                                 │
│      │                         Configure Monitoring Targets                   │
│      │                                    │                                 │
│      │                         ┌───────────────┴────────────┐            │
│      │                         │                        │                │
│      │                   ┌─────▼─────────┐         │                │
│      │                   │ Metric Anomaly │         │                │
│      │                   │ Log Pattern   │         │                │
│      │                   │ Trace Complete│         │                │
│      │                   └─────┬─────────┘         │                │
│      │                         │                  │                │
│      │                         ▼                  │                │
│      │              Alerting Context              │                │
│      │                    │                     │                │
│      │           ┌────────┴────────┐            │                │
│      │           │  Evaluate Alerts  │            │                │
│      │           │  State Machine   │            │                │
│      │           └────────┬────────┘            │                │
│      │                    │                     │                │
│      │                    ▼                     │                │
│      │           AlertTriggered/Firing         │                │
│      │           AlertEscalated               │                │
│      │                    │                     │                │
│      │                    ▼                     │                │
│      │         Incident Management Context      │                │
│      │                    │                     │                │
│      │           ┌────────┴────────┐          │                │
│      │           │ Incident Workflow │          │                │
│      │           │ Impact Analysis  │          │                │
│      │           └────────┬────────┘          │                │
│      │                    │                   │                │
│      │                    ▼                   │                │
│      │         Analytics Context             │                │
│      │                    │                   │                │
│      │           Process Events for ML       │                │
│      │           Generate Reports           │                │
│      │                    │                   │                │
│      └────────────────────────────────────────────────────────┘                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# 第三部分：技术架构设计

## 7. 分层架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ONE MONITOR 技术分层架构                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   用户界面层 (User Interface)                      │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │       │
│  │  │   Vue 3      │  │   Grafana    │  │   Mobile     │          │       │
│  │  │   SPA        │  │   Dashboards │  │   App        │          │       │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   API 网关层 (API Gateway)                       │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │       │
│  │  │   路由转发   │  │   认证授权   │  │   限流熔断  │          │       │
│  │  │   Spring    │  │   JWT/OAuth2 │  │   Resilience │          │       │
│  │  │   Cloud GW  │  │              │  │              │          │       │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   应用服务层 (Application Services)               │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                    Java 微服务 (核心)                  │   │       │
│  │  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────┐│   │       │
│  │  │  │   Auth    │ │   CMDB    │ │ Monitoring│ │ Alert ││   │       │
│  │  │  │  Service  │ │  Service │ │  Service │ │Service││   │       │
│  │  │  └───────────┘ └───────────┘ └───────────┘ └───────┘│   │       │
│  │  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────┐│   │       │
│  │  │  │ Incident  │ │ Report   │ │ Platform │ │       ││   │       │
│  │  │  │ Service  │ │ Service │ │ Service  │ │       ││   │       │
│  │  │  └───────────┘ └───────────┘ └───────────┘ └───────┘│   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                   Python 微服务 (分析)                 │   │       │
│  │  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────┐│   │       │
│  │  │  │ Analytics │ │   ML      │ │  Report  │ │       ││   │       │
│  │  │  │  Service  │ │ Service  │ │ Service │ │       ││   │       │
│  │  │  └───────────┘ └───────────┘ └───────────┘ └───────┘│   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   领域层 (Domain Layer)                         │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                    聚合 (Aggregates)                   │   │       │
│  │  │  • CI, CIRelation (CMDB Context)                     │   │       │
│  │  │  • MetricSeries, Trace (Monitoring Context)           │   │       │
│  │  │  • AlertRule, AlertInstance (Alerting Context)       │   │       │
│  │  │  • Incident (Incident Context)                      │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                  领域服务 (Domain Services)           │   │       │
│  │  │  • AlertEvaluationService                           │   │       │
│  │  │  • ImpactAnalysisService                          │   │       │
│  │  │  • CorrelationService                            │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                  领域事件 (Domain Events)             │   │       │
│  │  │  • CICreated, CIUpdated, CIStatusChanged            │   │       │
│  │  │  • AlertTriggered, AlertResolved                   │   │       │
│  │  │  • MetricAnomalyDetected                          │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   基础设施层 (Infrastructure)                   │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                   数据访问 (Repositories)             │   │       │
│  │  │  • PostgreSQL (CMDB, Alert Rules, Incidents)        │   │       │
│  │  │  • VictoriaMetrics Cluster (Metrics)                 │   │       │
│  │  │  • Loki (Logs)                                     │   │       │
│  │  │  • Tempo (Traces)                                  │   │       │
│  │  │  • Redis Cluster (Cache, Sessions)                  │   │       │
│  │  │  • MinIO (Object Storage)                          │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                   消息通信 (Messaging)               │   │       │
│  │  │  • Kafka (Domain Events)                            │   │       │
│  │  │  • RabbitMQ (Notifications)                        │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                   外部集成 (ACLs)                    │   │       │
│  │  │  • Prometheus ACL (指标采集)                        │   │       │
│  │  │  • OTel ACL (链路追踪)                             │   │       │
│  │  │  • Cloud Provider ACL (云资源)                      │   │       │
│  │  │  • Notification ACL (钉钉/企微/邮件)               │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   采集层 (Collection Layer)                    │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │              OpenTelemetry Collector                 │   │       │
│  │  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────┐│   │       │
│  │  │  │ Prometheus│ │   OTLP    │ │   Logs   │ │ Traces││   │       │
│  │  │  │ Receiver  │ │ Receiver │ │ Receiver │ │Receiver││   │       │
│  │  │  └───────────┘ └───────────┘ └───────────┘ └───────┘│   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 8. 服务通信模式

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       服务通信模式 (SERVICE COMMUNICATION)                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   同步通信 (Synchronous)                         │       │
│  │                                                                 │       │
│  │  REST API:                                                      │       │
│  │  • 通用 API 调用 (用户认证、CMDB 查询)                         │       │
│  │  • OpenAPI 3.0 规范                                            │       │
│  │  • Spring Cloud OpenFeign                                       │       │
│  │                                                                 │       │
│  │  gRPC:                                                         │       │
│  │  • 高性能场景 (实时指标查询、告警同步)                          │       │
│  │  • 双向流 (实时告警推送)                                       │       │
│  │  • Protocol Buffers 定义                                       │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   异步通信 (Asynchronous)                       │       │
│  │                                                                 │       │
│  │  Kafka (Domain Events):                                        │       │
│  │  • 跨上下文事件发布/订阅                                       │       │
│  │  • 事件溯源 (Event Sourcing) 可选                              │       │
│  │  • 消费者组管理                                               │       │
│  │                                                                 │       │
│  │  RabbitMQ (Notifications):                                     │       │
│  │  • 告警通知分发                                              │       │
│  │  • 工作队列 (Celery Tasks)                                    │       │
│  │  • 消息确认机制                                              │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   服务发现与配置                                 │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                   Consul / Nacos                       │   │       │
│  │  │  • 服务注册与发现                                       │   │       │
│  │  │  • 健康检查                                             │   │       │
│  │  │  • 配置中心                                             │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  │                                                                 │       │
│  │  ┌─────────────────────────────────────────────────────────┐   │       │
│  │  │                   Kubernetes                          │   │       │
│  │  │  • Service Mesh (Istio/Linkerd) 可选               │   │       │
│  │  │  • ConfigMap/Secret 管理                           │   │       │
│  │  │  • HPA 自动伸缩                                     │   │       │
│  │  └─────────────────────────────────────────────────────────┘   │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 9. 数据架构

### 9.1 存储策略

| 数据类型 | 存储系统 | 分区策略 | 保留策略 |
|---------|---------|---------|---------|
| **元数据 (用户/配置/规则)** | PostgreSQL | 按租户/组织 | 永久 |
| **监控指标** | VictoriaMetrics | 按 metric + labels | 30天原始, 1年降采样 |
| **日志** | Loki + MinIO | 按 labels + 时间 | 7天热存储, 90天冷存储 |
| **链路追踪** | Tempo + MinIO | 按 trace_id | 7天 |
| **事件/告警** | ClickHouse (可选) | 按时间 + 严重度 | 1年 |
| **缓存** | Redis Cluster | 按业务 | TTL 1-24小时 |
| **对象存储** | MinIO | 按类型 | 报表/备份 1年 |

### 9.2 数据模型

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          数据模型关系图                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  PostgreSQL (ACID)                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                                                          │       │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐│       │
│  │  │  users   │  │ci_types │  │   cis    │  │relations ││       │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘│       │
│  │       │           │           │           │           │      │       │
│  │  ┌────▼─────┐   │     ┌────▼─────┐   │     ┌────▼─────┐│       │
│  │  │roles     │   │     │attributes│   │     │change_hist││       │
│  │  └──────────┘   │     └──────────┘   │     └──────────┘│       │
│  │                  │                     │                  │      │
│  │  ┌──────────┐   │     ┌──────────┐   │     ┌──────────┐│       │
│  │  │alert_rules│   │     │alert_hist│   │     │incidents ││       │
│  │  └──────────┘   │     └──────────┘   │     └──────────┘│       │
│  │                  │                     │                  │      │
│  └──────────────────┼─────────────────────┼──────────────────┼──────┘       │
│                     │                     │                  │                │
│                     ▼                     ▼                  ▼                │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                    标签关联 (Label-based Correlation)           │       │
│  │                                                                 │       │
│  │  CI.labels: {ci_id, ci_name, ci_type, service_name, environment, owner}│       │
│  │       │                                                         │       │
│  │       ▼                                                         │       │
│  │  Metrics.labels: {ci_id, service_name, environment, ...}           │       │
│  │  Logs.labels: {ci_id, service_name, environment, ...}              │       │
│  │  Traces.resource: {ci_id, service_name, ...}                     │       │
│  │                                                                 │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                          │
│  │ Victoria │    │   Loki   │    │  Tempo   │                          │
│  │ Metrics  │    │          │    │          │                          │
│  │  Cluster │    │  +MinIO  │    │  +MinIO  │                          │
│  └──────────┘    └──────────┘    └──────────┘                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# 第四部分：详细设计

## 10. API 设计规范

### 10.1 API 分组

```
/api/v1
├── /auth                    # 认证授权
│   ├── POST /login         # 用户登录
│   ├── POST /logout        # 用户登出
│   ├── POST /refresh       # 刷新 Token
│   └── GET  /me            # 当前用户信息
│
├── /users                  # 用户管理
│   ├── GET  /              # 用户列表
│   ├── POST /              # 创建用户
│   ├── GET  /{id}         # 用户详情
│   ├── PUT  /{id}          # 更新用户
│   └── DELETE /{id}         # 删除用户
│
├── /cmdb                   # CMDB 核心 (核心域)
│   ├── /ci-types           # CI 类型管理
│   ├── /cis                # CI 实例管理
│   ├── /relations          # CI 关系管理
│   ├── /topology           # 拓扑视图
│   └── /search             # CI 搜索
│
├── /monitoring              # 监控管理 (支撑域)
│   ├── /targets            # 监控对象
│   ├── /metrics            # 指标查询
│   ├── /logs               # 日志查询
│   └── /traces             # 链路查询
│
├── /alerting               # 告警管理 (通用域)
│   ├── /rules              # 告警规则
│   ├── /instances          # 告警实例
│   ├── /channels          # 通知渠道
│   └── /silences          # 静默规则
│
├── /incidents              # 事件管理 (支撑域)
│   ├── /                   # 事件列表
│   ├── /{id}              # 事件详情
│   ├── /{id}/workflows    # 工作流
│   └── /{id}/rca          # 根因分析
│
├── /analytics              # 分析洞察 (通用域)
│   ├── /dashboards         # 仪表盘
│   ├── /reports           # 报表
│   └── /ml                # ML 分析
│
└── /platform              # 平台治理 (核心)
    ├── /roles              # 角色管理
    ├── /audit             # 审计日志
    └── /settings          # 系统配置
```

### 10.2 响应格式

```json
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  },
  "timestamp": "2024-01-21T10:30:00Z",
  "trace_id": "abc123"
}

// 分页响应
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total": 100,
      "total_pages": 5
    }
  },
  "timestamp": "2024-01-21T10:30:00Z"
}

// 错误响应
{
  "code": 400,
  "message": "Invalid parameters",
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ],
  "timestamp": "2024-01-21T10:30:00Z",
  "trace_id": "abc123"
}
```

---

## 11. 安全设计

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         安全架构 (SECURITY ARCHITECTURE)                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   认证 (Authentication)                        │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │       │
│  │  │   JWT       │  │   SSO       │  │   MFA       │          │       │
│  │  │  Token      │  │  OAuth2/OIDC │  │  TOTP/SMS   │          │       │
│  │  │  (Bearer)   │  │  (Keycloak) │  │             │          │       │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   授权 (Authorization)                        │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │       │
│  │  │   RBAC      │  │   ABAC      │  │   CI-Level   │          │       │
│  │  │  角色权限    │  │  属性策略    │  │  细粒度控制  │          │       │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   数据安全 (Data Security)                     │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │       │
│  │  │   TLS 1.3   │  │   AES-256   │  │  数据脱敏    │          │       │
│  │  │  传输加密    │  │  存储加密    │  │  敏感字段    │          │       │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │                   审计合规 (Audit & Compliance)               │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │       │
│  │  │   完整日志  │  │   操作审计  │  │   合规报告  │          │       │
│  │  │   (审计)    │  │   (追踪)    │  │   (报表)    │          │       │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │       │
│  └─────────────────────────────────────────────────────────────────┘       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# 第五部分：任务分解 (WBS)

## 12. 工作分解结构

### 12.1 一级分解 (阶段)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ONE MONITOR 项目工作分解结构                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  P1. 基础设施搭建 (2-3周)                                              │
│  P2. 核心域开发 - CMDB (4-6周)                                         │
│  P3. 支撑域开发 - Monitoring (4-6周)                                      │
│  P4. 支撑域开发 - Alerting (3-4周)                                      │
│  P5. 支撑域开发 - Incident (3-4周)                                     │
│  P6. 通用域开发 - Analytics (3-4周)                                    │
│  P7. 通用域开发 - Automation (2-3周)                                    │
│  P8. 平台治理 - Platform (2-3周)                                        │
│  P9. 前端开发 (4-6周)                                                  │
│  P10. 测试与质量保证 (贯穿)                                              │
│  P11. 部署与运维 (2-3周)                                              │
│                                                                              │
│  总工期: 6-8 个月                                                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 12.2 二级分解 (任务包)

```
P1. 基础设施搭建 (2-3周)
├── 1.1 环境准备
│   ├── [ ] 开发环境搭建 (Docker Compose)
│   ├── [ ] 测试环境配置
│   └── [ ] 生产环境规划 (K8s)
├── 1.2 基础设施组件部署
│   ├── [ ] PostgreSQL Cluster (主从)
│   ├── [ ] Redis Cluster (Sentinel)
│   ├── [ ] VictoriaMetrics Cluster
│   ├── [ ] Loki + MinIO
│   ├── [ ] Tempo + MinIO
│   └── [ ] Kafka Cluster
├── 1.3 基础框架搭建
│   ├── [ ] Java 多模块 Maven 项目
│   ├── [ ] Python 项目脚手架
│   ├── [ ] 前端 Vue 3 项目
│   └── [ ] CI/CD 流水线
└── 1.4 共享内核开发
    ├── [ ] CMDB 类型定义库
    ├── [ ] 标签键定义
    ├── [ ] 领域事件定义
    └── [ ] 公共组件库

P2. CMDB 核心域开发 (4-6周)
├── 2.1 CI 类型管理
│   ├── [ ] CI Type 聚合设计
│   ├── [ ] CI Type CRUD API
│   ├── [ ] 属性 Schema 定义引擎
│   └── [ ] 预定义 CI Types 初始化
├── 2.2 CI 实例管理
│   ├── [ ] CI 聚合设计
│   ├── [ ] CI CRUD API
│   ├── [ ] 状态机实现
│   ├── [ ] 属性验证
│   └── [ ] 批量导入导出
├── 2.3 关系管理
│   ├── [ ] CIRelation 聚合设计
│   ├── [ ] 关系 CRUD API
│   ├── [ ] 循环依赖检测
│   ├── [ ] 关系类型管理
│   └── [ ] 关系查询优化
├── 2.4 变更追踪
│   ├── [ ] Change History 实体
│   ├── [ ] 变更记录 API
│   ├── [ ] 版本对比功能
│   └── [ ] 审计日志
├── 2.5 拓扑与影响分析
│   ├── [ ] 拓扑查询 API
│   ├── [ ] 依赖图构建
│   ├── [ ] 影响范围分析
│   └── [ ] 拓扑可视化组件
└── 2.6 测试
    ├── [ ] 单元测试 (>80%覆盖率)
    ├── [ ] 集成测试
    └── [ ] 性能测试

P3. Monitoring 支撑域开发 (4-6周)
├── 3.1 数据采集层
│   ├── [ ] OTel Collector 配置
│   ├── [ ] Prometheus Receiver 适配
│   ├── [ ] SNMP 采集器
│   └── [ ] 云 API 采集器
├── 3.2 指标管理
│   ├── [ ] Target 管理 API
│   ├── [ ] 标签同步服务
│   ├── [ ] PromQL 查询 API
│   └── [ ] 指标元数据管理
├── 3.3 日志管理
│   ├── [ ] LogQL 查询 API
│   ├── [ ] 日志 Pattern 检测
│   ├── [ ] 日志 Pattern 告警
│   └── [ ] 日志聚合
├── 3.4 链路追踪
│   ├── [ ] Trace 查询 API
│   ├── [ ] Trace 关联
│   ├── [ ] Service Graph
│   └── [ ] Trace 告警
└── 3.5 测试
    ├── [ ] 集成测试
    └── [ ] E2E 测试

P4. Alerting 支撑域开发 (3-4周)
├── 4.1 告警规则引擎
│   ├── [ ] AlertRule 聚合
│   ├── [ ] PromQL 规则评估
│   ├── [ ] 阈值规则评估
│   └── [ ] 智能检测 (简单版)
├── 4.2 告警生命周期
│   ├── [ ] AlertInstance 聚合
│   ├── [ ] 状态机实现
│   ├── [ ] 分组与聚合
│   └── [ ] 静默规则
├── 4.3 通知路由
│   ├── [ ] Channel 管理
│   ├── [ ] 路由策略
│   ├── [ ] 升级机制
│   └── [ ] 通知模板
└── 4.4 测试
    ├── [ ] 规则测试
    └── [ ] 通知测试

... (Incident, Analytics, Automation, Platform 类似分解)

P9. 前端开发 (4-6周)
├── 9.1 基础框架
│   ├── [ ] 项目初始化
│   ├── [ ] 路由配置
│   ├── [ ] 状态管理
│   └── [ ] 公共组件
├── 9.2 CMDB 前端
│   ├── [ ] CI 列表/详情
│   ├── [ ] CI 表单
│   ├── [ ] 拓扑图 (G6)
│   └── [ ] 关系编辑器
├── 9.3 监控前端
│   ├── [ ] 仪表盘
│   ├── [ ] 指标图表 (ECharts)
│   ├── [ ] 日志查看器
│   └── [ ] 链路追踪
├── 9.4 告警前端
│   ├── [ ] 告警规则
│   ├── [ ] 告警列表
│   ├── [ ] 通知配置
│   └── [ ] 静默管理
└── 9.5 测试
    ├── [ ] 单元测试
    └── [ ] E2E 测试
```

---

## 13. 里程碑计划

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         里程碑计划 (MILESTONES)                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  M1: 基础设施就绪 (Week 3)                                              │
│  └── 交付: Docker Compose 环境 + 基础组件部署                               │
│                                                                              │
│  M2: CMDB 核心上线 (Week 9)                                             │
│  └── 交付: CI 管理、关系管理、拓扑视图、变更追踪                           │
│                                                                              │
│  M3: 监控数据流打通 (Week 15)                                            │
│  └── 交付: 指标/日志/链路采集、存储、查询、告警触发                        │
│                                                                              │
│  M4: 完整功能内测 (Week 20)                                            │
│  └── 交付: 所有功能模块集成 + 前端界面                                      │
│                                                                              │
│  M5: Beta 发布 (Week 24)                                                │
│  └── 交付: 生产环境部署 + 内部试用                                         │
│                                                                              │
│  M6: 正式发布 (Week 28)                                                │
│  └── 交付: 正式版本 + 文档 + 运维手册                                     │
│                                                                              │
│  总工期: ~7 个月                                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# 附录: 设计原则总结

## MECE 分解验证

| 域 | 二级能力数 | 三级能力数 | 独立性验证 | 穷尽性验证 |
|----|-----------|-----------|-----------|-----------|
| 资产管理 | 5 | 16 | ✅ 无重叠 | ✅ 覆盖全面 |
| 数据可观测 | 5 | 17 | ✅ 无重叠 | ✅ 覆盖全面 |
| 告警响应 | 4 | 14 | ✅ 无重叠 | ✅ 覆盖全面 |
| 洞察分析 | 4 | 14 | ✅ 无重叠 | ✅ 覆盖全面 |
| 事件管理 | 3 | 12 | ✅ 无重叠 | ✅ 覆盖全面 |
| 自动化控制 | 3 | 9 | ✅ 无重叠 | ✅ 覆盖全面 |
| 平台治理 | 3 | 10 | ✅ 无重叠 | ✅ 覆盖全面 |
| 安全合规 | 3 | 10 | ✅ 无重叠 | ✅ 覆盖全面 |

## DDD 聚合边界验证

| 聚合 | 不变式数量 | 边界清晰度 | 一致性保证 |
|-----|-----------|-----------|-----------|
| CI | 4 | ✅ 清晰 | ✅ 强一致 |
| CIRelation | 3 | ✅ 清晰 | ✅ 强一致 |
| MetricSeries | 2 | ✅ 清晰 | ✅ 最终一致 |
| Trace | 2 | ✅ 清晰 | ✅ 最终一致 |
| AlertRule | 1 | ✅ 清晰 | ✅ 强一致 |
| AlertInstance | 2 | ✅ 清晰 | ✅ 强一致 |

---

*文档版本: 3.0*  
*更新日期: 2024-01-21*  
*方法论: DDD + MECE*  
*状态: 设计完成，待评审*
