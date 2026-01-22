# OneMonitor 完整项目目录结构

```
OneMonitorPlatform/
│
├── 📄 文档目录 (docs/)
│   ├── ARCHITECTURE_V2.md          # 完整架构设计文档 (推荐)
│   ├── ARCHITECTURE.md             # 原架构设计文档 (参考)
│   ├── PROJECT_STRUCTURE.md        # 项目结构说明
│   ├── BACKEND_STRUCTURE.md        # 后端结构说明
│   └── FRONTEND_STRUCTURE.md       # 前端结构说明
│
├── 📦 Docker 编排 (docker/)
│   ├── docker-compose.yml          # 主编排文件 (混合架构)
│   ├── docker-compose.override.yml # 开发环境覆盖配置
│   │
│   ├── config/
│   │   ├── otel-collector.yaml     # OTel 采集配置
│   │   ├── loki-config.yaml        # Loki 配置
│   │   ├── tempo-config.yaml       # Tempo 配置
│   │   ├── grafana.ini             # Grafana 配置
│   │   ├── prometheus.yml          # Prometheus 配置
│   │   └── victoriametrics.yml     # VictoriaMetrics 配置
│   │
│   └── provisioning/
│       ├── dashboards/             # Grafana 仪表盘
│       │   ├── Overview.json
│       │   ├── Infrastructure.json
│       │   ├── Business.json
│       │   └── CMDB.json
│       └── datasources/
│           └── datasources.yaml    # Grafana 数据源
│
├── 🐍 Java 后端 (backend/)
│   │
│   ├── onemonitor-parent/          # 父 POM
│   │   ├── pom.xml
│   │   └── README.md
│   │
│   ├── onemonitor-common/          # 公共模块
│   │   ├── pom.xml
│   │   └── src/main/java/com/onemonitor/common/
│   │       ├── exception/          # 异常定义
│   │       ├── result/             # 响应封装
│   │       ├── utils/              # 工具类
│   │       └── constant/           # 常量
│   │
│   ├── onemonitor-api/             # API 模块 (DTO)
│   │   ├── pom.xml
│   │   └── src/main/java/com/onemonitor/api/
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   └── response/
│   │       └── enums/
│   │           └── ResultCode.java
│   │
│   ├── onemonitor-security/        # 安全模块
│   │   ├── pom.xml
│   │   └── src/main/java/com/onemonitor/security/
│   │       ├── config/
│   │       ├── filter/
│   │       ├── handler/
│   │       └── service/
│   │
│   ├── onemonitor-gateway/         # 网关服务
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/onemonitor/gateway/
│   │       ├── GatewayApplication.java
│   │       ├── config/
│   │       └── filter/
│   │
│   ├── onemonitor-auth/            # 认证服务
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/onemonitor/auth/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       └── entity/
│   │
│   ├── onemonitor-cmdb/            # CMDB 服务 (核心)
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/onemonitor/cmdb/
│   │       ├── controller/         # API 控制器
│   │       │   ├── CITypeController.java
│   │       │   ├── CIController.java
│   │       │   └── RelationController.java
│   │       ├── service/            # 业务逻辑
│   │       │   ├── CITypeService.java
│   │       │   ├── CIService.java
│   │       │   ├── CIMonitorIntegrationService.java
│   │       │   └── TopologyService.java
│   │       ├── repository/         # 数据访问
│   │       │   ├── CITypeRepository.java
│   │       │   ├── CIRepository.java
│   │       │   └── CIRelationRepository.java
│   │       ├── entity/             # JPA 实体
│   │       │   ├── CIType.java
│   │       │   ├── CI.java
│   │       │   ├── CIRelation.java
│   │       │   └── CIChangeHistory.java
│   │       ├── dto/                # 数据传输对象
│   │       │   ├── CICreateDTO.java
│   │       │   ├── CIUpdateDTO.java
│   │       │   ├── CIRelationDTO.java
│   │       │   └── CIMonitorConfigDTO.java
│   │       └── vo/                 # 视图对象
│   │           ├── CIDetailVO.java
│   │           ├── CIBasicVO.java
│   │           └── TopologyVO.java
│   │
│   ├── onemonitor-monitor/         # 监控服务
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/onemonitor/monitor/
│   │       ├── controller/
│   │       ├── service/
│   │       │   ├── TargetService.java
│   │       │   └── VictoriaMetricsService.java
│   │       ├── collector/
│   │       │   ├── PrometheusCollector.java
│   │       │   └── OTelCollector.java
│   │       ├── entity/
│   │       │   ├── MonitorTarget.java
│   │       │   └── MonitorMetric.java
│   │       └── repository/
│   │
│   ├── onemonitor-alert/           # 告警服务
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/onemonitor/alert/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── engine/             # 告警引擎
│   │       │   ├── RuleEngine.java
│   │       │   └── PromQLEvaluator.java
│   │       └── repository/
│   │
│   ├── onemonitor-report/          # 报表服务
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/onemonitor/report/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── generator/
│   │       │   ├── PdfReportGenerator.java
│   │       │   └── ExcelReportGenerator.java
│   │       └── scheduler/
│   │
│   ├── onemonitor-notification/    # 通知服务
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/onemonitor/notification/
│   │       ├── service/
│   │       │   ├── EmailService.java
│   │       │   ├── DingTalkService.java
│   │       │   └── WebhookService.java
│   │       └── channel/
│   │           └── NotificationChannel.java
│   │
│   └── scripts/
│       ├── init-db.sh              # 数据库初始化
│       └── generate-models.sh      # 代码生成脚本
│
├── 🐍 Python 分析服务 (analytics/)
│   ├── app/
│   │   ├── main.py                 # FastAPI 应用入口
│   │   ├── config.py               # 配置管理
│   │   │
│   │   ├── api/                    # API 层
│   │   │   ├── router.py           # 路由汇总
│   │   │   ├── deps.py             # 依赖注入
│   │   │   └── v1/
│   │   │       ├── analytics.py    # 分析 API
│   │   │       ├── ml.py           # ML API
│   │   │       └── reports.py      # 报表 API
│   │   │
│   │   ├── services/               # 服务层
│   │   │   ├── __init__.py
│   │   │   ├── analytics_service.py
│   │   │   ├── anomaly_detection.py
│   │   │   ├── capacity_prediction.py
│   │   │   ├── root_cause_analysis.py
│   │   │   └── report_service.py
│   │   │
│   │   ├── ml/                     # 机器学习模块
│   │   │   ├── __init__.py
│   │   │   ├── models/
│   │   │   │   ├── anomaly_detector.py
│   │   │   │   ├── trend_predictor.py
│   │   │   │   └── seasonality_detector.py
│   │   │   ├── training/
│   │   │   │   └── train_anomaly_model.py
│   │   │   └── inference/
│   │   │       └── predict.py
│   │   │
│   │   ├── tasks/                  # Celery 任务
│   │   │   ├── __init__.py
│   │   │   ├── celery_app.py
│   │   │   ├── analytics_tasks.py
│   │   │   └── report_tasks.py
│   │   │
│   │   ├── clients/                # 外部服务客户端
│   │   │   ├── __init__.py
│   │   │   ├── victoria_metrics_client.py
│   │   │   ├── clickhouse_client.py
│   │   │   └── grafana_client.py
│   │   │
│   │   ├── schemas/                # Pydantic 模型
│   │   │   ├── __init__.py
│   │   │   ├── analytics.py
│   │   │   ├── ml.py
│   │   │   └── report.py
│   │   │
│   │   └── utils/                  # 工具函数
│   │       ├── __init__.py
│   │       ├── date_utils.py
│   │       ├── promql_utils.py
│   │       └── logger.py
│   │
│   ├── tests/
│   │   ├── test_analytics/
│   │   │   └── test_anomaly_detection.py
│   │   └── test_ml/
│   │       └── test_models.py
│   │
│   ├── requirements.txt
│   ├── requirements-dev.txt
│   ├── Dockerfile
│   ├── celerybeat-schedule
│   └── README.md
│
├── 🌐 前端应用 (frontend/)
│   ├── public/
│   │   ├── favicon.ico
│   │   └── robots.txt
│   │
│   ├── src/
│   │   ├── api/                    # API 封装
│   │   │   ├── index.js
│   │   │   ├── auth.js
│   │   │   ├── cmdb.js             # CMDB API
│   │   │   ├── monitoring.js
│   │   │   ├── alerts.js
│   │   │   ├── reports.js
│   │   │   └── analytics.js        # Python 分析服务 API
│   │   │
│   │   ├── assets/                 # 静态资源
│   │   │   ├── images/
│   │   │   └── styles/
│   │   │       ├── _variables.scss
│   │   │       └── global.scss
│   │   │
│   │   ├── components/             # 公共组件
│   │   │   ├── Layout/
│   │   │   ├── Charts/
│   │   │   │   ├── LineChart.vue
│   │   │   │   ├── AreaChart.vue
│   │   │   │   ├── BarChart.vue
│   │   │   │   └── GaugeChart.vue
│   │   │   ├── Common/
│   │   │   └── Grafana/
│   │   │       └── GrafanaPanel.vue
│   │   │
│   │   ├── composables/            # 组合式函数
│   │   ├── constants/              # 常量定义
│   │   ├── directives/             # 自定义指令
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态
│   │   └── utils/                  # 工具函数
│   │
│   ├── views/                      # 页面组件
│   │   ├── login/
│   │   │   └── index.vue
│   │   │
│   │   ├── dashboard/
│   │   │   └── index.vue
│   │   │
│   │   ├── monitoring/
│   │   │   ├── targets/
│   │   │   │   ├── index.vue
│   │   │   │   ├── Detail.vue
│   │   │   │   └── Create.vue
│   │   │   └── metrics/
│   │   │       └── index.vue
│   │   │
│   │   ├── cmdb/                   # CMDB 核心模块
│   │   │   ├── index.vue
│   │   │   ├── components/
│   │   │   │   ├── CITree.vue              # CI 树形结构
│   │   │   │   ├── CIForm.vue              # CI 表单
│   │   │   │   ├── CIRelationGraph.vue     # 关系拓扑图
│   │   │   │   ├── CIAttributeEditor.vue   # 属性编辑器
│   │   │   │   └── CIMonitorStatus.vue     # 监控状态
│   │   │   └── views/
│   │   │       ├── resources/              # 资源管理
│   │   │       │   ├── index.vue
│   │   │       │   ├── Detail.vue
│   │   │       │   └── Create.vue
│   │   │       ├── topology/               # 拓扑视图
│   │   │       │   └── index.vue
│   │   │       ├── relations/              # 关系管理
│   │   │       │   └── index.vue
│   │   │       └── history/                # 变更历史
│   │   │           └── index.vue
│   │   │
│   │   ├── alerts/
│   │   │   ├── rules/
│   │   │   ├── history/
│   │   │   └── channels/
│   │   │
│   │   ├── reports/
│   │   │   └── index.vue
│   │   │
│   │   └── settings/
│   │       ├── users/
│   │       ├── roles/
│   │       └── system/
│   │
│   ├── App.vue
│   ├── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
│
├── 📊 可观测性组件配置 (monitoring/)
│   ├── victoriametrics/
│   │   ├── cluster/
│   │   │   ├── vminsert/
│   │   │   ├── vmselect/
│   │   │   └── vmstorage/
│   │   └── single/
│   └── loki/
│       ├── distributed/
│       └── single-binary/
│
├── 🗂️ SQL 初始化 (sql/)
│   ├── init/
│   │   ├── 001-init.sql            # 基础表结构
│   │   ├── 002-ci-types.sql        # CI 类型预置数据
│   │   ├── 003-relation-types.sql  # 关系类型预置数据
│   │   └── 004-sample-data.sql     # 示例数据
│   │
│   ├── migrations/                 # Flyway/PgQD 迁移
│   │   └── V1__Initial_schema.sql
│   │
│   └── README.md
│
├── 🧪 测试 (tests/)
│   ├── backend/                    # Java 后端测试
│   │   ├── test_api/
│   │   └── test_services/
│   │       └── test_cmdb.py
│   │
│   └── frontend/                   # 前端测试
│       ├── unit/
│       └── e2e/
│
├── 📋 其他文件
│   ├── .env.example                # 环境变量示例
│   ├── .gitignore
│   ├── .dockerignore
│   ├── Makefile                    # 常用命令
│   ├── README.md                   # 项目说明
│   └── LICENSE
│
└── 📦 外部依赖 (Docker 运行时)
    ├── PostgreSQL       # 关系数据库
    ├── Redis            # 缓存
    ├── MinIO            # 对象存储
    ├── VictoriaMetrics  # 时序数据库
    ├── Grafana Loki     # 日志系统
    ├── Grafana Tempo    # 链路追踪
    ├── Grafana          # 可视化
    └── OTel Collector   # 数据采集
```

## 文件统计

| 类型 | 数量 |
|------|------|
| Java 服务模块 | 8 个 (parent + 7 子服务) |
| Java 源文件 | ~200+ |
| Python 源文件 | ~100+ |
| Vue 组件 | ~50+ |
| 配置文件 | ~30+ |
| 文档文件 | ~10+ |

## 技术栈汇总

### Java 后端 (Spring Boot 3.x)

| 模块 | 技术 |
|------|------|
| 框架 | Spring Boot 3.2 + WebFlux |
| ORM | Spring Data JPA + R2DBC |
| 安全 | Spring Security 6.x + JWT |
| API 文档 | SpringDoc OpenAPI |
| 测试 | JUnit 5 + Testcontainers |

### Python 分析服务

| 模块 | 技术 |
|------|------|
| 框架 | FastAPI + Celery |
| 数据处理 | Pandas + NumPy |
| 机器学习 | Scikit-learn + PyTorch |
| 任务队列 | Celery + Redis |
| 测试 | pytest + coverage |

### 前端 (Vue 3)

| 模块 | 技术 |
|------|------|
| 框架 | Vue 3 + Composition API |
| UI 组件 | Element Plus |
| 图表 | Apache ECharts + AntV G6 |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |

## 快速启动

### 开发环境

```bash
# 1. 启动基础设施
docker-compose up -d postgres redis minio

# 2. 初始化数据库
cd sql/init
./init-db.sh

# 3. 启动 Java 服务 (Backend)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4. 启动 Python 服务 (Analytics)
cd analytics
python -m uvicorn app.main:app --reload

# 5. 启动前端 (Frontend)
cd frontend
npm install
npm run dev
```

### 生产环境

```bash
# 使用 Docker Compose
docker-compose -f docker-compose.yml up -d

# 或使用 Kubernetes
kubectl apply -f k8s/
```

## 服务端口映射

| 服务 | 端口 | 协议 | 说明 |
|------|------|------|------|
| Frontend | 80 | HTTP | Web 界面 |
| API Gateway | 8080 | HTTP | API 网关 |
| Auth Service | 8081 | HTTP | 认证服务 |
| CMDB Service | 8082 | HTTP | CMDB 服务 |
| Monitor Service | 8083 | HTTP | 监控服务 |
| Alert Service | 8084 | HTTP | 告警服务 |
| Analytics API | 8000 | HTTP | 分析服务 |
| Grafana | 3000 | HTTP | 可视化 |
| VictoriaMetrics | 8428 | HTTP | 指标查询 |
| Loki | 3100 | HTTP | 日志查询 |
| Tempo | 16687 | HTTP | 追踪查询 |
| PostgreSQL | 5432 | TCP | 数据库 |
| Redis | 6379 | TCP | 缓存 |
| MinIO | 9000/9001 | HTTP | 对象存储 |

---

*OneMonitor - 让运维更简单，让监控更智能！ 🚀*
