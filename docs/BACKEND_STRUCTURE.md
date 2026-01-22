# OneMonitor Backend 目录结构

```
backend/
├── app/
│   ├── api/
│   │   ├── v1/
│   │   │   ├── api.py              # 路由汇总
│   │   │   ├── deps.py             # 依赖注入
│   │   │   └── endpoints/
│   │   │       ├── __init__.py
│   │   │       ├── auth.py         # 认证接口
│   │   │       ├── users.py        # 用户管理
│   │   │       ├── targets.py      # 监控对象
│   │   │       ├── alerts.py       # 告警管理
│   │   │       ├── dashboards.py   # 仪表盘
│   │   │       ├── cmdb.py         # CMDB
│   │   │       ├── reports.py      # 报表
│   │   │       └── settings.py     # 系统设置
│   │   └── __init__.py
│   │
│   ├── core/
│   │   ├── __init__.py
│   │   ├── config.py              # 配置管理
│   │   ├── security.py            # 安全工具
│   │   ├── logging.py             # 日志配置
│   │   └── prometheus.py          # 监控指标
│   │
│   ├── crud/
│   │   ├── __init__.py
│   │   ├── base.py                # 基础CRUD
│   │   ├── user.py                # 用户CRUD
│   │   ├── target.py              # 监控对象CRUD
│   │   ├── alert.py               # 告警CRUD
│   │   ├── dashboard.py           # 仪表盘CRUD
│   │   ├── cmdb.py                # CMDB CRUD
│   │   └── report.py              # 报表CRUD
│   │
│   ├── db/
│   │   ├── __init__.py
│   │   ├── session.py             # 数据库会话
│   │   ├── base.py                # Base 类
│   │   └── init_db.py             # 初始化脚本
│   │
│   ├── models/
│   │   ├── __init__.py
│   │   ├── user.py                # 用户模型
│   │   ├── target.py              # 监控对象模型
│   │   ├── alert.py               # 告警模型
│   │   ├── dashboard.py           # 仪表盘模型
│   │   ├── cmdb.py                # CMDB 模型
│   │   ├── report.py              # 报表模型
│   │   └── system.py              # 系统配置模型
│   │
│   ├── schemas/
│   │   ├── __init__.py
│   │   ├── token.py               # Token Schema
│   │   ├── user.py                # 用户 Schema
│   │   ├── target.py              # 监控对象 Schema
│   │   ├── alert.py               # 告警 Schema
│   │   ├── dashboard.py           # 仪表盘 Schema
│   │   ├── cmdb.py                # CMDB Schema
│   │   ├── report.py              # 报表 Schema
│   │   └── system.py              # 系统配置 Schema
│   │
│   ├── services/
│   │   ├── __init__.py
│   │   ├── auth_service.py        # 认证服务
│   │   ├── alert_service.py       # 告警服务
│   │   ├── notification_service.py # 通知服务
│   │   ├── report_service.py      # 报表服务
│   │   ├── grafana_service.py     # Grafana 集成
│   │   └── vm_service.py          # VictoriaMetrics 集成
│   │
│   ├── tasks/
│   │   ├── __init__.py
│   │   ├── celery.py              # Celery 配置
│   │   ├── alert_tasks.py         # 告警任务
│   │   └── report_tasks.py        # 报表任务
│   │
│   ├── utils/
│   │   ├── __init__.py
│   │   ├── constants.py           # 常量定义
│   │   ├── helpers.py             # 辅助函数
│   │   └── validators.py          # 校验器
│   │
│   ├── __init__.py
│   └── main.py                    # 应用入口
│
├── alembic/
│   ├── versions/                  # 数据库迁移版本
│   └── env.py
│
├── scripts/
│   ├── init_db.py                 # 数据库初始化
│   └── seed_data.py               # 种子数据
│
├── tests/
│   ├── __init__.py
│   ├── conftest.py
│   ├── test_api/
│   └── test_services/
│
├── .env.example
├── .env
├── requirements.txt
├── requirements-dev.txt
├── Dockerfile
├── alembic.ini
└── README.md
```

## 文件说明

### 核心模块

| 文件 | 职责 |
|------|------|
| `main.py` | FastAPI 应用入口，配置 CORS、中间件、路由 |
| `api/v1/api.py` | 路由汇总，导入所有端点路由 |
| `core/config.py` | Pydantic Settings 配置管理 |
| `core/security.py` | JWT 认证、密码加密、权限验证 |
| `db/session.py` | SQLAlchemy 数据库会话管理 |
| `crud/base.py` | 基础 CRUD 操作类 |

### 依赖版本

```
fastapi>=0.109.0
uvicorn[standard]>=0.27.0
sqlalchemy>=2.0.25
pydantic>=2.5.0
pydantic-settings>=2.1.0
python-jose[cryptography]>=3.3.0
passlib[bcrypt]>=1.7.4
celery>=5.3.4
redis>=5.0.1
httpx>=0.26.0
python-multipart>=0.0.6
alembic>=1.13.0
```

### 快速启动

```bash
# 创建虚拟环境
python -m venv venv
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt

# 配置环境变量
cp .env.example .env
# 编辑 .env 文件

# 初始化数据库
alembic upgrade head

# 启动服务
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
