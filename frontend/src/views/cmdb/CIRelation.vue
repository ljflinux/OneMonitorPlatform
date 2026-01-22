<template>
  <div class="ci-relation">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">关系拓扑</h1>
        <p class="page-subtitle">配置项依赖关系可视化</p>
      </div>
      <div class="header-actions">
        <el-select v-model="selectedType" placeholder="关系类型" clearable style="width: 200px">
          <el-option label="全部类型" value="" />
          <el-option label="包含" value="CONTAINS" />
          <el-option label="依赖" value="DEPENDS_ON" />
          <el-option label="部署在" value="RUNS_ON" />
          <el-option label="连接" value="CONNECTED_TO" />
          <el-option label="管理" value="MANAGES" />
        </el-select>
      </div>
    </div>

    <!-- 拓扑图 -->
    <div class="topology-section">
      <el-card shadow="never" class="topology-card">
        <div ref="graphContainer" class="graph-container">
          <!-- 拓扑图将通过 ECharts 实现 -->
          <v-chart :option="graphOption" style="height: 600px" />
        </div>
      </el-card>
    </div>

    <!-- 节点列表 -->
    <div class="nodes-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>关联配置项</span>
            <span class="node-count">({{ nodes.length }} 个节点)</span>
          </div>
        </template>
        
        <el-table :data="nodes" stripe max-height="300">
          <el-table-column prop="name" label="名称" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="handleNodeClick(row)">
                {{ row.label }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="relationCount" label="关联数" width="80" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { GraphChart } from 'echarts/charts';
import { TooltipComponent, LegendComponent } from 'echarts/components';

use([CanvasRenderer, GraphChart, TooltipComponent, LegendComponent]);

const router = useRouter();
const graphContainer = ref<HTMLElement | null>(null);
const selectedType = ref('');
const nodes = ref<Array<{
  id: string;
  label: string;
  type: string;
  status: string;
  relationCount: number;
  x?: number;
  y?: number;
}>>([]);
const links = ref<Array<{
  source: string;
  target: string;
  label: string;
}>>([]);

// 模拟数据
const mockNodes = [
  { id: 'app-001', label: '订单服务', type: 'application', status: 'ACTIVE', relationCount: 3 },
  { id: 'db-001', label: '订单数据库', type: 'database', status: 'ACTIVE', relationCount: 2 },
  { id: 'cache-001', label: 'Redis缓存', type: 'database', status: 'ACTIVE', relationCount: 1 },
  { id: 'server-001', label: 'Web服务器1', type: 'server', status: 'ACTIVE', relationCount: 2 },
  { id: 'server-002', label: 'Web服务器2', type: 'server', status: 'ACTIVE', relationCount: 2 },
  { id: 'lb-001', label: '负载均衡器', type: 'network', status: 'ACTIVE', relationCount: 3 },
];

const mockLinks = [
  { source: 'app-001', target: 'db-001', label: '依赖' },
  { source: 'app-001', target: 'cache-001', label: '依赖' },
  { source: 'app-001', target: 'server-001', label: '部署在' },
  { source: 'app-001', target: 'server-002', label: '部署在' },
  { source: 'server-001', target: 'lb-001', label: '连接' },
  { source: 'server-002', target: 'lb-001', label: '连接' },
];

// ECharts 配置
const graphOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: (params: { data: { name?: string; label?: string; category?: string } }) => {
      if (params.dataType === 'node') {
        return `<div>
          <strong>${params.data.name || params.data.label}</strong><br/>
          类型: ${params.data.category}<br/>
        </div>`;
      }
      return `${params.data.source} → ${params.data.target}`;
    },
  },
  legend: {
    data: ['应用', '数据库', '服务器', '网络设备'],
    bottom: 10,
  },
  series: [
    {
      type: 'graph',
      layout: 'force',
      data: nodes.value.map(node => ({
        id: node.id,
        name: node.label,
        category: node.type,
        symbolSize: 50,
        draggable: true,
        label: {
          show: true,
          position: 'bottom',
          formatter: node.label,
        },
      })),
      links: links.value.map(link => ({
        source: link.source,
        target: link.target,
        label: {
          show: true,
          formatter: link.label,
          fontSize: 10,
        },
        lineStyle: {
          width: 2,
          curveness: 0.1,
        },
      })),
      roam: true,
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 4,
        },
      },
      force: {
        repulsion: 800,
        edgeLength: 150,
        gravity: 0.1,
      },
      categories: [
        { name: '应用' },
        { name: '数据库' },
        { name: '服务器' },
        { name: '网络设备' },
      ],
    },
  ],
}));

// 状态类型
const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    ACTIVE: 'success',
    MAINTENANCE: 'warning',
    DECOMMISSIONED: 'info',
    DISPOSED: 'danger',
  };
  return typeMap[status] || 'info';
};

// 点击节点
const handleNodeClick = (node: { id: string }) => {
  router.push(`/cmdb/ci/${node.id}`);
};

// 初始化
onMounted(() => {
  nodes.value = mockNodes;
  links.value = mockLinks;
});
</script>

<style lang="scss" scoped>
.ci-relation {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }

  .page-subtitle {
    font-size: 14px;
    color: #909399;
    margin: 4px 0 0;
  }
}

.topology-section {
  margin-bottom: 24px;

  .topology-card {
    :deep(.el-card__body) {
      padding: 0;
    }

    .graph-container {
      padding: 16px;
    }
  }
}

.nodes-section {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;

    .node-count {
      font-size: 14px;
      color: #909399;
    }
  }
}
</style>
