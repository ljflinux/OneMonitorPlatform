<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>仪表盘</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
      </div>
    </div>

    <!-- 状态卡片 -->
    <div class="status-cards">
      <el-card class="status-card" shadow="hover">
        <div class="card-content">
          <div class="card-info">
            <div class="card-title">监控对象总数</div>
            <div class="card-value">{{ totalMonitorObjects }}</div>
          </div>
          <div class="card-icon server-icon">
            <el-icon><server /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="status-card" shadow="hover">
        <div class="card-content">
          <div class="card-info">
            <div class="card-title">活跃告警数</div>
            <div class="card-value danger">{{ activeAlerts }}</div>
          </div>
          <div class="card-icon alert-icon">
            <el-icon><warning /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="status-card" shadow="hover">
        <div class="card-content">
          <div class="card-info">
            <div class="card-title">正常运行率</div>
            <div class="card-value">{{ uptimeRate }}%</div>
          </div>
          <div class="card-icon success-icon">
            <el-icon><success /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card class="status-card" shadow="hover">
        <div class="card-content">
          <div class="card-info">
            <div class="card-title">平均响应时间</div>
            <div class="card-value">{{ avgResponseTime }}ms</div>
          </div>
          <div class="card-icon speed-icon">
            <el-icon><timer /></el-icon>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>监控对象状态分布</span>
          </div>
        </template>
        <div class="chart-content">
          <v-chart :option="statusDistributionOption" autoresize />
        </div>
      </el-card>

      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>告警趋势分析</span>
          </div>
        </template>
        <div class="chart-content">
          <v-chart :option="alertTrendOption" autoresize />
        </div>
      </el-card>

      <el-card class="chart-card full-width" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>系统资源使用率</span>
          </div>
        </template>
        <div class="chart-content">
          <v-chart :option="resourceUsageOption" autoresize />
        </div>
      </el-card>
    </div>

    <!-- 最近告警 -->
    <el-card class="recent-alerts" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>最近告警</span>
          <el-button type="text" @click="viewAllAlerts">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentAlertsData" stripe style="width: 100%">
        <el-table-column prop="severity" label="级别" width="100">
          <template #default="scope">
            <el-tag :type="getSeverityType(scope.row.severity)">
              {{ scope.row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="告警信息" min-width="300" />
        <el-table-column prop="monitor_object_name" label="监控对象" width="200" />
        <el-table-column prop="created_at" label="发生时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="text" size="small" @click="handleAcknowledge(scope.row)">
              确认
            </el-button>
            <el-button type="text" size="small" @click="handleResolve(scope.row)">
              解决
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import * as echarts from 'echarts'

// 状态
const router = useRouter()
const dateRange = ref([])

// 模拟数据
const totalMonitorObjects = ref(156)
const activeAlerts = ref(12)
const uptimeRate = ref(99.8)
const avgResponseTime = ref(125)

// 最近告警数据
const recentAlertsData = ref([
  {
    id: 1,
    severity: 'critical',
    message: '服务器 CPU 使用率超过 90%',
    monitor_object_name: 'Web服务器01',
    created_at: '2024-01-15 14:30:00'
  },
  {
    id: 2,
    severity: 'warning',
    message: '磁盘空间使用率超过 80%',
    monitor_object_name: '数据库服务器01',
    created_at: '2024-01-15 14:15:00'
  },
  {
    id: 3,
    severity: 'error',
    message: '网络延迟超过阈值',
    monitor_object_name: '核心交换机01',
    created_at: '2024-01-15 14:00:00'
  },
  {
    id: 4,
    severity: 'warning',
    message: '内存使用率超过 85%',
    monitor_object_name: '应用服务器02',
    created_at: '2024-01-15 13:45:00'
  },
  {
    id: 5,
    severity: 'critical',
    message: '服务不可用',
    monitor_object_name: 'API网关',
    created_at: '2024-01-15 13:30:00'
  }
])

// 监控对象状态分布图表配置
const statusDistributionOption = reactive({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '监控对象状态',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 132, name: '正常' },
        { value: 15, name: '警告' },
        { value: 9, name: '故障' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

// 告警趋势分析图表配置
const alertTrendOption = reactive({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['info', 'warning', 'error', 'critical']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: 'info',
      type: 'line',
      stack: 'Total',
      data: [120, 132, 101, 134, 90, 230]
    },
    {
      name: 'warning',
      type: 'line',
      stack: 'Total',
      data: [220, 182, 191, 234, 290, 330]
    },
    {
      name: 'error',
      type: 'line',
      stack: 'Total',
      data: [150, 232, 201, 154, 190, 330]
    },
    {
      name: 'critical',
      type: 'line',
      stack: 'Total',
      data: [320, 332, 301, 334, 390, 330]
    }
  ]
})

// 系统资源使用率图表配置
const resourceUsageOption = reactive({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['CPU使用率', '内存使用率', '磁盘使用率']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['Web服务器01', 'Web服务器02', '数据库服务器01', '应用服务器01', '应用服务器02', '文件服务器01']
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value}%'
    }
  },
  series: [
    {
      name: 'CPU使用率',
      type: 'bar',
      data: [58, 65, 72, 45, 52, 38]
    },
    {
      name: '内存使用率',
      type: 'bar',
      data: [72, 78, 85, 68, 75, 55]
    },
    {
      name: '磁盘使用率',
      type: 'bar',
      data: [65, 72, 80, 58, 65, 45]
    }
  ]
})

// 计算属性
const recentAlerts = computed(() => {
  return recentAlertsData.value.slice(0, 5)
})

// 方法
const handleDateChange = (val) => {
  console.log('Date range changed:', val)
  // TODO: 根据日期范围更新数据
}

const viewAllAlerts = () => {
  router.push('/alert/history')
}

const getSeverityType = (severity) => {
  const typeMap = {
    critical: 'danger',
    error: 'warning',
    warning: 'warning',
    info: 'info'
  }
  return typeMap[severity] || 'info'
}

const handleAcknowledge = (alert) => {
  console.log('Acknowledge alert:', alert)
  // TODO: 实现告警确认功能
}

const handleResolve = (alert) => {
  console.log('Resolve alert:', alert)
  // TODO: 实现告警解决功能
}

// 生命周期
onMounted(() => {
  // TODO: 从API获取真实数据
  console.log('Dashboard mounted')
})
</script>

<style scoped>
.dashboard {
  height: 100%;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.status-card {
  height: 120px;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.card-value.danger {
  color: #f56c6c;
}

.card-value.warning {
  color: #e6a23c;
}

.card-value.success {
  color: #67c23a;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 30px;
}

.server-icon {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.alert-icon {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.success-icon {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.speed-icon {
  background-color: rgba(144, 202, 249, 0.1);
  color: #409eff;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  height: 350px;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-content {
  height: calc(100% - 48px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recent-alerts {
  margin-top: 20px;
}

.recent-alerts .el-table {
  margin-top: 20px;
}
</style>