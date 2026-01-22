# OneMonitor Frontend 目录结构

```
frontend/
├── public/
│   ├── favicon.ico
│   └── robots.txt
│
├── src/
│   ├── api/                          # API 接口封装
│   │   ├── index.js                  # API 统一导出
│   │   ├── auth.js                   # 认证相关接口
│   │   ├── users.js                  # 用户管理
│   │   ├── targets.js                # 监控对象
│   │   ├── alert-rules.js            # 告警规则
│   │   ├── alerts.js                 # 告警事件
│   │   ├── channels.js               # 通知渠道
│   │   ├── dashboards.js             # 仪表盘
│   │   ├── cmdb.js                   # CMDB
│   │   ├── reports.js                # 报表
│   │   ├── settings.js               # 系统设置
│   │   ├── grafana.js                # Grafana API
│   │   └── prometheus.js             # Prometheus/VictoriaMetrics API
│   │
│   ├── assets/                       # 静态资源
│   │   ├── images/
│   │   │   ├── logo.svg
│   │   │   └── logo-dark.svg
│   │   ├── fonts/
│   │   └── styles/
│   │       ├── _variables.scss       # SCSS 变量
│   │       ├── _mixins.scss          # SCSS 混入
│   │       ├── _functions.scss       # SCSS 函数
│   │       ├── _transitions.scss     # 过渡动画
│   │       └── global.scss           # 全局样式
│   │
│   ├── components/                   # 公共组件
│   │   ├── Layout/
│   │   │   ├── Layout.vue            # 主布局
│   │   │   ├── Header.vue            # 顶部导航
│   │   │   ├── Sidebar.vue           # 侧边栏
│   │   │   ├── SidebarMenu.vue       # 侧边栏菜单
│   │   │   ├── Breadcrumb.vue        # 面包屑
│   │   │   ├── TagsView.vue          # 标签页
│   │   │   └── Settings.vue          # 设置抽屉
│   │   │
│   │   ├── Charts/                   # ECharts 图表组件
│   │   │   ├── index.js              # 统一导出
│   │   │   ├── LineChart.vue         # 折线图
│   │   │   ├── AreaChart.vue         # 面积图
│   │   │   ├── BarChart.vue          # 柱状图
│   │   │   ├── PieChart.vue          # 饼图
│   │   │   ├── GaugeChart.vue        # 仪表盘
│   │   │   ├── Heatmap.vue           # 热力图
│   │   │   ├── RadarChart.vue        # 雷达图
│   │   │   ├── TreeChart.vue         # 树图
│   │   │   ├── GraphChart.vue        # 关系图
│   │   │   └── useECharts.js         # ECharts Composable
│   │   │
│   │   ├── Common/                   # 通用组件
│   │   │   ├── PageHeader.vue        # 页面头部
│   │   │   ├── SearchForm.vue        # 搜索表单
│   │   │   ├── DataTable.vue         # 数据表格
│   │   │   ├── Pagination.vue        # 分页组件
│   │   │   ├── StatusTag.vue         # 状态标签
│   │   │   ├── EmptyData.vue         # 空数据
│   │   │   ├── Loading.vue           # 加载动画
│   │   │   ├── Dialog.vue            # 弹窗
│   │   │   └── Drawer.vue            # 抽屉
│   │   │
│   │   ├── Form/                     # 表单组件
│   │   │   ├── BaseForm.vue          # 基础表单
│   │   │   ├── InputGroup.vue        # 输入组合
│   │   │   ├── Select.vue            # 选择器
│   │   │   ├── DatePicker.vue        # 日期选择
│   │   │   └── Cascader.vue          # 级联选择
│   │   │
│   │   ├── Grafana/                  # Grafana 集成组件
│   │   │   ├── GrafanaPanel.vue      # Grafana 面板
│   │   │   ├── GrafanaDashboard.vue  # Grafana 仪表盘
│   │   │   ├── GrafanaExplore.vue    # Explore 集成
│   │   │   └── useGrafana.js         # Grafana Composable
│   │   │
│   │   └── Icons/                    # 图标组件
│   │       ├── index.js              # 图标导出
│   │       ├── SvgIcon.vue           # SVG 图标
│   │       └── svg/                  # SVG 文件
│   │
│   ├── composables/                  # 组合式函数
│   │   ├── useTable.js               # 表格 Composable
│   │   ├── useForm.js                # 表单 Composable
│   │   ├── usePermission.js          # 权限 Composable
│   │   ├── useCharts.js              # 图表 Composable
│   │   ├── useDate.js                # 日期 Composable
│   │   └── useClipboard.js           # 剪贴板 Composable
│   │
│   ├── constants/                    # 常量定义
│   │   ├── index.js                  # 导出
│   │   ├── targetTypes.js            # 监控对象类型
│   │   ├── alertSeverity.js          # 告警级别
│   │   ├── alertStatus.js            # 告警状态
│   │   ├── targetStatus.js           # 监控对象状态
│   │   ├── httpStatus.js             # HTTP 状态码
│   │   └── timeRanges.js             # 时间范围选项
│   │
│   ├── directives/                   # 自定义指令
│   │   ├── index.js                  # 指令注册
│   │   ├── permission.js             # 权限指令
│   │   ├── debounce.js               # 防抖指令
│   │   ├── throttle.js               # 节流指令
│   │   ├── clipboard.js              # 剪贴板指令
│   │   └── longpress.js              # 长按指令
│   │
│   ├── filters/                      # 过滤器 (Vue 2 兼容)
│   │   ├── index.js                  # 过滤器注册
│   │   ├── date.js                   # 日期格式化
│   │   ├── number.js                 # 数字格式化
│   │   └── string.js                 # 字符串处理
│   │
│   ├── hooks/                        # 自定义 Hooks (Vue 3)
│   │   ├── useQuery.js               # 查询参数 Hook
│   │   ├── useModal.js               # 模态框 Hook
│   │   ├── useMessage.js             # 消息提示 Hook
│   │   ├── useLoading.js             # 加载状态 Hook
│   │   └── usePagination.js          # 分页 Hook
│   │
│   ├── router/                       # 路由配置
│   │   ├── index.js                  # Router 实例
│   │   ├── routes.js                 # 路由定义
│   │   ├── permission.js             # 路由守卫
│   │   └── modules/                  # 路由模块化
│   │       ├── index.js
│   │       ├── dashboard.js
│   │       ├── monitoring.js
│   │       ├── alerts.js
│   │       ├── logs.js
│   │       ├── traces.js
│   │       ├── cmdb.js
│   │       ├── reports.js
│   │       └── settings.js
│   │
│   ├── stores/                       # Pinia 状态管理
│   │   ├── index.js                  # Store 实例
│   │   ├── app.js                    # 应用状态
│   │   ├── user.js                   # 用户状态
│   │   ├── permission.js             # 权限状态
│   │   ├── settings.js               # 设置状态
│   │   ├── tagsView.js               # 标签页状态
│   │   └── modules/                  # 模块化 Store
│   │       ├── user.js
│   │       └── permission.js
│   │
│   ├── utils/                        # 工具函数
│   │   ├── index.js                  # 工具导出
│   │   ├── request.js                # Axios 封装
│   │   ├── auth.js                   # 认证工具
│   │   ├── format.js                 # 格式化工具
│   │   ├── validate.js               # 表单校验
│   │   ├── constants.js              # 常量
│   │   ├── storage.js                # 存储封装
│   │   ├── browser.js                # 浏览器工具
│   │   └── date.js                   # 日期工具
│   │
│   ├── views/                        # 页面组件
│   │   ├── login/
│   │   │   └── index.vue             # 登录页
│   │   │
│   │   ├── dashboard/
│   │   │   ├── index.vue             # 仪表盘主页
│   │   │   ├── components/
│   │   │   │   ├── OverviewCard.vue      # 概览卡片
│   │   │   │   ├── StatusDistribution.vue # 状态分布
│   │   │   │   ├── AlertTrendChart.vue    # 告警趋势
│   │   │   │   └── ResourceUsageChart.vue # 资源使用
│   │   │   └── views/
│   │   │       ├── overview.vue       # 系统总览
│   │   │       ├── business.vue       # 业务视图
│   │   │       └── custom.vue         # 自定义视图
│   │   │
│   │   ├── monitoring/
│   │   │   ├── index.vue             # 监控管理主页
│   │   │   ├── components/
│   │   │   │   ├── TargetCard.vue        # 对象卡片
│   │   │   │   ├── StatusIndicator.vue   # 状态指示器
│   │   │   │   └── MetricChart.vue       # 指标图表
│   │   │   └── views/
│   │   │       ├── targets/
│   │   │       │   ├── index.vue      # 对象列表
│   │   │       │   ├── Detail.vue     # 对象详情
│   │   │       │   └── Create.vue     # 创建对象
│   │   │       ├── config/
│   │   │       │   └── index.vue      # 采集配置
│   │   │       ├── metrics/
│   │   │       │   └── index.vue      # 指标浏览
│   │   │       └── discovery/
│   │   │           └── index.vue      # 服务发现
│   │   │
│   │   ├── alerts/
│   │   │   ├── index.vue             # 告警管理主页
│   │   │   ├── components/
│   │   │   │   ├── AlertTable.vue        # 告警表格
│   │   │   │   ├── AlertFilter.vue       # 告警过滤
│   │   │   │   └── SeverityBadge.vue     # 级别徽章
│   │   │   └── views/
│   │   │       ├── rules/
│   │   │       │   ├── index.vue     # 规则列表
│   │   │       │   ├── Create.vue    # 创建规则
│   │   │       │   └── Edit.vue      # 编辑规则
│   │   │       ├── history/
│   │   │       │   └── index.vue     # 告警历史
│   │   │       │       └── Detail.vue # 历史详情
│   │   │       └── channels/
│   │   │           ├── index.vue     # 渠道列表
│   │   │           ├── Create.vue    # 创建渠道
│   │   │           └── Edit.vue      # 编辑渠道
│   │   │
│   │   ├── logs/
│   │   │   ├── index.vue             # 日志主页
│   │   │   ├── components/
│   │   │   │   ├── LogSearch.vue         # 日志搜索
│   │   │   │   ├── LogViewer.vue         # 日志查看器
│   │   │   │   ├── LogFilter.vue         # 日志过滤
│   │   │   │   └── LogChart.vue          # 日志图表
│   │   │   └── views/
│   │   │       ├── search.vue        # 日志查询
│   │   │       └── stream.vue        # 实时日志流
│   │   │
│   │   ├── traces/
│   │   │   ├── index.vue             # 追踪主页
│   │   │   ├── components/
│   │   │   │   ├── TraceList.vue         # 追踪列表
│   │   │   │   ├── TraceDetail.vue       # 追踪详情
│   │   │   │   ├── SpanTree.vue          # 跨度树
│   │   │   │   └── ServiceGraph.vue      # 服务图
│   │   │   └── views/
│   │   │       ├── explorer.vue      # 追踪浏览器
│   │   │       └── service-graph.vue # 服务依赖
│   │   │
│   │   ├── cmdb/
│   │   │   ├── index.vue             # CMDB 主页
│   │   │   ├── components/
│   │   │   │   ├── CITree.vue            # CI 树
│   │   │   │   ├── CIRelationGraph.vue   # 关系图
│   │   │   │   └── CIForm.vue            # CI 表单
│   │   │   └── views/
│   │   │       ├── resources/
│   │   │       │   ├── index.vue     # 资源列表
│   │   │       │   ├── Detail.vue    # 资源详情
│   │   │       │   └── Create.vue    # 创建资源
│   │   │       ├── relations/
│   │   │       │   └── index.vue     # 关系拓扑
│   │   │       └── changes/
│   │   │           └── index.vue     # 变更历史
│   │   │
│   │   ├── reports/
│   │   │   ├── index.vue             # 报表主页
│   │   │   ├── components/
│   │   │   │   ├── ReportCard.vue        # 报表卡片
│   │   │   │   └── ReportPreview.vue     # 报表预览
│   │   │   └── views/
│   │   │       ├── templates/
│   │   │       │   ├── index.vue    # 模板列表
│   │   │       │   └── Create.vue   # 创建模板
│   │   │       └── tasks/
│   │   │           ├── index.vue    # 任务列表
│   │   │           └── Create.vue   # 创建任务
│   │   │
│   │   ├── settings/
│   │   │   ├── index.vue             # 设置主页
│   │   │   └── views/
│   │   │       ├── users/
│   │   │       │   ├── index.vue    # 用户列表
│   │   │       │   ├── Create.vue   # 创建用户
│   │   │       │   └── Edit.vue     # 编辑用户
│   │   │       ├── roles/
│   │   │       │   ├── index.vue    # 角色列表
│   │   │       │   ├── Create.vue   # 创建角色
│   │   │       │   └── Edit.vue     # 编辑角色
│   │   │       ├── channels/
│   │   │       │   └── index.vue    # 通知渠道
│   │   │       └── system/
│   │   │           └── index.vue    # 系统配置
│   │   │
│   │   └── error/
│   │       ├── 403.vue
│   │       ├── 404.vue
│   │       └── 500.vue
│   │
│   ├── App.vue                        # 根组件
│   └── main.js                        # 应用入口
│
├── .env                                # 环境变量
├── .env.development                    # 开发环境
├── .env.production                     # 生产环境
├── .eslintrc.js                        # ESLint 配置
├── .prettierrc                         # Prettier 配置
├── .stylelintrc                        # Stylelint 配置
├── index.html                          # HTML 模板
├── package.json                        # 项目配置
├── vite.config.js                      # Vite 配置
└── README.md                           # 项目说明
```

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue.js | 3.4+ |
| 构建工具 | Vite | 5.0+ |
| UI 组件 | Element Plus | 2.5+ |
| 状态管理 | Pinia | 2.1+ |
| 路由 | Vue Router | 4.2+ |
| HTTP 客户端 | Axios | 1.6+ |
| 图表 | Apache ECharts | 5.4+ |
| CSS 预处理器 | Sass | 1.69+ |
| 代码规范 | ESLint + Prettier | - |

## 快速启动

```bash
# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build

# 代码检查
npm run lint

# 代码格式化
npm run format
```

## 关键组件说明

### 布局组件 (components/Layout)

```
Layout/
├── Layout.vue         # 主布局容器
├── Header.vue         # 顶部导航栏
├── Sidebar.vue        # 侧边栏菜单
├── SidebarMenu.vue    # 菜单项组件
├── Breadcrumb.vue     # 面包屑导航
├── TagsView.vue       # 多标签页
└── Settings.vue       # 设置抽屉
```

### 图表组件 (components/Charts)

```
Charts/
├── LineChart.vue      # 折线图 - 用于趋势展示
├── AreaChart.vue      # 面积图 - 用于指标占比
├── BarChart.vue       # 柱状图 - 用于对比分析
├── PieChart.vue       # 饼图 - 用于占比展示
├── GaugeChart.vue     # 仪表盘 - 用于状态展示
├── Heatmap.vue        # 热力图 - 用于时序数据
├── RadarChart.vue     # 雷达图 - 用于多维分析
├── TreeChart.vue      # 树图 - 用于层级数据
└── GraphChart.vue     # 关系图 - 用于依赖关系
```

### 页面模块 (views)

| 模块 | 路由 | 说明 |
|------|------|------|
| 登录 | /login | 用户认证 |
| 仪表盘 | /dashboard | 数据总览 |
| 监控管理 | /monitoring | 监控对象管理 |
| 告警中心 | /alerts | 告警规则和历史 |
| 日志分析 | /logs | 日志查询 |
| 链路追踪 | /traces | 分布式追踪 |
| 配置管理 | /cmdb | IT 资源管理 |
| 报表中心 | /reports | 报表生成 |
| 系统设置 | /settings | 用户、角色、配置 |
