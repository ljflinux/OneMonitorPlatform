# OneMonitor 云原生监控平台 - 完整目录结构

```
OneMonitorPlatform/
│
├── 📄 文档目录
│   ├── ARCHITECTURE.md          # 完整架构设计文档 (本文档)
│   ├── BACKEND_STRUCTURE.md     # 后端代码结构说明
│   ├── FRONTEND_STRUCTURE.md    # 前端代码结构说明
│   └── OneMonitor_Architecture.png  # 架构图
│
├── 📦 Docker 编排
│   ├── docker-compose.yml       # 主编排文件
│   ├── Dockerfile.backend       # 后端 Dockerfile
│   ├── Dockerfile.frontend      # 前端 Dockerfile
│   └── config/                  # 配置文件目录
│       ├── otel-collector.yaml  # OTel 采集配置
│       ├── loki-config.yaml     # Loki 配置
│       ├── tempo-config.yaml    # Tempo 配置
│       └── grafana/
│           ├── grafana.ini      # Grafana 配置
│           ├── provisioning/
│           │   ├── dashboards/
│           │   │   └── dashboards.yaml
│           │   └── datasources/
│           │       └── datasources.yaml
│           └── dashboards/
│               ├── Overview.json
│               ├── Infrastructure.json
│               └── Business.json
│
├── 🐍 后端服务 (backend/)
│   ├── app/
│   │   ├── api/v1/endpoints/    # API 端点
│   │   │   ├── auth.py          # 认证接口
│   │   │   ├── users.py         # 用户管理
│   │   │   ├── targets.py       # 监控对象
│   │   │   ├── alerts.py        # 告警管理
│   │   │   ├── dashboards.py    # 仪表盘
│   │   │   ├── cmdb.py          # CMDB
│   │   │   ├── reports.py       # 报表
│   │   │   └── settings.py      # 系统设置
│   │   ├── core/                # 核心模块
│   │   │   ├── config.py        # 配置
│   │   │   ├── security.py      # 安全
│   │   │   └── prometheus.py    # 监控
│   │   ├── crud/                # 数据访问
│   │   ├── models/              # 数据模型
│   │   ├── schemas/             # Pydantic 模型
│   │   ├── services/            # 业务逻辑
│   │   ├── tasks/               # 定时任务
│   │   └── utils/               # 工具函数
│   ├── tests/                   # 测试用例
│   ├── requirements.txt         # Python 依赖
│   ├── Dockerfile               # 容器化
│   └── main.py                  # 应用入口
│
├── 🌐 前端应用 (frontend/)
│   ├── src/
│   │   ├── api/                 # API 封装
│   │   ├── assets/              # 静态资源
│   │   ├── components/          # 公共组件
│   │   │   ├── Layout/          # 布局组件
│   │   │   ├── Charts/          # 图表组件
│   │   │   ├── Common/          # 通用组件
│   │   │   ├── Form/            # 表单组件
│   │   │   └── Grafana/         # Grafana 集成
│   │   ├── composables/         # 组合式函数
│   │   ├── constants/           # 常量定义
│   │   ├── directives/          # 自定义指令
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # 状态管理
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面组件
│   │       ├── login/           # 登录页
│   │       ├── dashboard/       # 仪表盘
│   │       ├── monitoring/      # 监控管理
│   │       ├── alerts/          # 告警中心
│   │       ├── logs/            # 日志分析
│   │       ├── traces/          # 链路追踪
│   │       ├── cmdb/            # 配置管理
│   │       ├── reports/         # 报表中心
│   │       ├── settings/        # 系统设置
│   │       └── error/           # 错误页面
│   ├── package.json             # 项目配置
│   ├── vite.config.js           # Vite 配置
│   └── Dockerfile               # 容器化
│
├── 📊 可观测性组件配置
│   ├── victoriametrics/         # VM 配置 (可选)
│   ├── loki/                    # Loki 配置
│   ├── tempo/                   # Tempo 配置
│   └── grafana/                 # Grafana 配置
│
├── 📋 基础设施即代码
│   ├── k8s/                     # Kubernetes 部署
│   │   ├── namespace.yaml
│   │   ├── cmdb-deployment.yaml
│   │   ├── backend-deployment.yaml
│   │   ├── frontend-deployment.yaml
│   │   └── configmap.yaml
│   └── helm/                    # Helm Chart
│       ├── onemonitor/
│       │   ├── Chart.yaml
│       │   ├── values.yaml
│       │   └── templates/
│       └── readme.md
│
├── 🧪 测试
│   ├── tests/backend/           # 后端测试
│   │   ├── conftest.py
│   │   ├── test_api/
│   │   └── test_services/
│   └── tests/frontend/          # 前端测试
│       ├── unit/
│       └── e2e/
│
├── 📝 其他文件
│   ├── .env.example             # 环境变量示例
│   ├── .gitignore
│   ├── .dockerignore
│   ├── README.md                # 项目说明
│   └── LICENSE                  # 许可证
│
└── 📦 外部依赖 (Docker 运行)
    ├── PostgreSQL       # 关系数据库
    ├── Redis            # 缓存
    ├── MinIO            # 对象存储
    ├── VictoriaMetrics  # 时序数据库
    ├── Grafana Loki     # 日志系统
    ├── Grafana Tempo    # 链路追踪
    ├── Grafana          # 可视化
    └── OTel Collector   # 数据采集
```

## 技术栈总结

### 核心组件

| 组件 | 用途 | 端口 |
|------|------|------|
| **后端 (FastAPI)** | 业务 API 服务 | 8000 |
| **前端 (Vue 3)** | Web 管理界面 | 80 |
| **PostgreSQL** | 业务数据存储 | 5432 |
| **Redis** | 缓存、会话 | 6379 |
| **MinIO** | 对象存储 | 9000/9001 |
| **VictoriaMetrics** | 指标存储 | 8428/8429 |
| **Grafana Loki** | 日志聚合 | 3100 |
| **Grafana Tempo** | 链路追踪 | 4317/4318/16687 |
| **Grafana** | 可视化 | 3000 |
| **OTel Collector** | 统一采集 | 4317/4318 |

### 开发技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Python 3.11 + FastAPI |
| ORM | SQLAlchemy 2.0 |
| 任务队列 | Celery + Redis |
| 前端框架 | Vue 3 + Composition API |
| UI 组件 | Element Plus |
| 图表 | Apache ECharts |
| 状态管理 | Pinia |
| 构建工具 | Vite 5 |

## 快速启动

### 1. 环境准备

```bash
# 安装 Docker 和 Docker Compose
# 确保可用内存 >= 4GB
```

### 2. 启动所有服务

```bash
# 克隆项目
git clone <repo-url>
cd OneMonitorPlatform

# 启动所有服务
docker-compose up -d

# 查看启动状态
docker-compose ps
```

### 3. 访问服务

| 服务 | 地址 | 账号 |
|------|------|------|
| OneMonitor 前端 | http://localhost:80 | admin/admin123 |
| Grafana | http://localhost:3000 | admin/admin123 |
| API 文档 | http://localhost:8000/docs | - |

### 4. 开发模式

```bash
# 后端开发
cd backend
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python -m uvicorn app.main:app --reload

# 前端开发
cd frontend
npm install
npm run dev
```

## 文件统计

| 类型 | 数量 |
|------|------|
| 后端 Python 文件 | ~50+ |
| 前端 Vue 组件 | ~30+ |
| 配置文件 | ~20+ |
| 文档文件 | ~5+ |

## 目录用途说明

```
📄 docs/              - 项目文档和架构设计
🐍 backend/           - Python 后端服务
🌐 frontend/          - Vue 3 前端应用
📦 config/            - 基础设施配置文件
📊 k8s/               - Kubernetes 部署配置
📋 helm/              - Helm Chart 包
🧪 tests/             - 测试用例
```

## 注意事项

1. **首次启动**: 首次启动时会自动创建数据库表和初始数据
2. **资源占用**: 完整运行需要约 4GB 内存
3. **数据持久化**: 所有数据存储在 Docker volumes 中
4. **配置修改**: 修改配置后需要重启相关服务

---

*OneMonitor - 让运维更简单，让监控更智能！ 🚀*
