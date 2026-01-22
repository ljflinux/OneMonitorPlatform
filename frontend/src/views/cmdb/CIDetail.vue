<template>
  <div class="ci-detail">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="header-info">
          <h1 class="page-title">{{ ci?.displayName || ci?.name || '配置项详情' }}</h1>
          <div class="ci-meta">
            <el-tag :type="getStatusType(ci?.status || 'ACTIVE')">
              {{ ci?.statusDisplayName }}
            </el-tag>
            <span class="meta-item">类型: {{ ci?.typeId }}</span>
            <span class="meta-item">ID: {{ ci?.id }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button @click="handleStatusChange">
            <el-icon><Switch /></el-icon>
            状态变更
          </el-button>
          <el-button type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 内容区域 -->
    <div v-else-if="ci" class="content-area">
      <el-row :gutter="24">
        <!-- 左侧基本信息 -->
        <el-col :span="16">
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
              </div>
            </template>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="名称">
                {{ ci.name }}
              </el-descriptions-item>
              <el-descriptions-item label="显示名称">
                {{ ci.displayName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(ci.status)">
                  {{ ci.statusDisplayName }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="环境">
                {{ ci.environment || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="负责人">
                {{ ci.owner || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="部门">
                {{ ci.department || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="位置">
                {{ ci.location || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="描述">
                {{ ci.description || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(ci.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ formatDate(ci.updatedAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 扩展属性 -->
          <el-card v-if="Object.keys(ci.attributes).length > 0" shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>扩展属性</span>
              </div>
            </template>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item
                v-for="(value, key) in ci.attributes"
                :key="key"
                :label="formatAttrKey(key)"
              >
                {{ formatAttrValue(value) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 关联关系 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>关联关系</span>
                <el-button text type="primary" @click="viewRelations">
                  查看拓扑
                </el-button>
              </div>
            </template>
            
            <el-table :data="relations" stripe>
              <el-table-column prop="type" label="关系类型" width="150">
                <template #default="{ row }">
                  {{ getRelationLabel(row.type) }}
                </template>
              </el-table-column>
              <el-table-column prop="targetCiName" label="关联配置项" min-width="200">
                <template #default="{ row }">
                  <el-link type="primary">
                    {{ row.targetCiName || row.targetCiId }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="validFrom" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.validFrom) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button size="small" type="danger" text>
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- 右侧监控数据 -->
        <el-col :span="8">
          <!-- 监控标签 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>监控标签</span>
                <el-button text type="primary" @click="copyLabels">
                  复制
                </el-button>
              </div>
            </template>
            
            <div class="labels-section">
              <el-tag
                v-for="(value, key) in ci.labels"
                :key="key"
                class="label-tag"
                type="info"
              >
                {{ key }}: {{ value }}
              </el-tag>
              <el-tag
                class="label-tag"
                type="success"
              >
                ci_id: {{ ci.id }}
              </el-tag>
              <el-tag
                class="label-tag"
                type="success"
              >
                ci_name: {{ ci.name }}
              </el-tag>
            </div>
          </el-card>

          <!-- 操作历史 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>最近操作</span>
              </div>
            </template>
            
            <el-timeline>
              <el-timeline-item
                v-for="(event, index) in recentEvents"
                :key="index"
                :timestamp="formatDate(event.timestamp)"
                :type="getEventType(event.type)"
              >
                {{ getEventLabel(event.type) }}
                <div class="event-detail">{{ event.detail }}</div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty description="配置项不存在或已被删除" />
    </div>

    <!-- 编辑对话框 -->
    <CICreateDialog
      v-model:visible="dialogVisible"
      :ci="ci"
      @success="handleDialogSuccess"
    />

    <!-- 状态变更对话框 -->
    <CIStatusDialog
      v-model:visible="statusDialogVisible"
      :ci="ci"
      @success="handleStatusDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Edit, Switch, Delete } from '@element-plus/icons-vue';
import { useCmdbStore } from '@/stores/cmdb';
import type { CIResponse } from '@/types/cmdb';
import CICreateDialog from '@/components/cmdb/CICreateDialog.vue';
import CIStatusDialog from '@/components/cmdb/CIStatusDialog.vue';
import { formatDate } from '@/utils/date';

const route = useRoute();
const router = useRouter();
const cmdbStore = useCmdbStore();

const ci = ref<CIResponse | null>(null);
const loading = ref(true);
const dialogVisible = ref(false);
const statusDialogVisible = ref(false);

// 模拟数据
const relations = ref<Array<{
  type: string;
  targetCiId: string;
  targetCiName: string;
  validFrom: string;
}>>([]);

const recentEvents = ref<Array<{
  type: string;
  timestamp: string;
  detail: string;
}>>([]);

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

// 格式化属性键
const formatAttrKey = (key: string): string => {
  return key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
};

// 格式化属性值
const formatAttrValue = (value: unknown): string => {
  if (value === null || value === undefined) return '-';
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
};

// 关系类型标签
const getRelationLabel = (type: string): string => {
  const labels: Record<string, string> = {
    CONTAINS: '包含',
    DEPENDS_ON: '依赖',
    RUNS_ON: '部署在',
    CONNECTED_TO: '连接',
    MANAGES: '管理',
    PART_OF: '属于',
    REPLICATES_TO: '复制到',
  };
  return labels[type] || type;
};

// 事件类型
const getEventType = (type: string): string => {
  const typeMap: Record<string, string> = {
    CICreated: 'success',
    CIUpdated: 'primary',
    CIStatusChanged: 'warning',
    CIDecommissioned: 'danger',
  };
  return typeMap[type] || 'info';
};

// 事件标签
const getEventLabel = (type: string): string => {
  const labels: Record<string, string> = {
    CICreated: '创建配置项',
    CIUpdated: '更新配置项',
    CIStatusChanged: '状态变更',
    CIDecommissioned: '退役配置项',
  };
  return labels[type] || type;
};

// 返回
const goBack = () => {
  router.push('/cmdb/ci');
};

// 编辑
const handleEdit = () => {
  dialogVisible.value = true;
};

// 状态变更
const handleStatusChange = () => {
  statusDialogVisible.value = true;
};

// 删除
const handleDelete = () => {
  if (!ci.value) return;

  ElMessageBox.confirm(
    `确定要删除配置项 "${ci.value.displayName || ci.value.name}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await cmdbStore.deleteCI(ci.value!.id);
      ElMessage.success('删除成功');
      router.push('/cmdb/ci');
    } catch {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

// 查看关系
const viewRelations = () => {
  router.push(`/cmdb/relation?ci=${ci.value?.id}`);
};

// 复制标签
const copyLabels = async () => {
  if (!ci.value) return;

  const labels = {
    ...ci.value.labels,
    ci_id: ci.value.id,
    ci_name: ci.value.name,
  };

  await navigator.clipboard.writeText(JSON.stringify(labels, null, 2));
  ElMessage.success('标签已复制到剪贴板');
};

// 对话框成功
const handleDialogSuccess = async () => {
  await loadData();
};

const handleStatusDialogSuccess = async () => {
  await loadData();
};

// 加载数据
const loadData = async () => {
  const id = route.params.id as string;
  loading.value = true;

  try {
    const data = await cmdbStore.fetchCIById(id);
    ci.value = data;
    
    // 加载关联关系（模拟）
    relations.value = [
      {
        type: 'RUNS_ON',
        targetCiId: 'server-001',
        targetCiName: 'web-server-01',
        validFrom: new Date().toISOString(),
      },
    ];

    // 加载事件（模拟）
    recentEvents.value = [
      {
        type: 'CICreated',
        timestamp: ci.value.createdAt,
        detail: `创建配置项 ${ci.value.name}`,
      },
    ];
  } catch {
    ElMessage.error('加载配置项失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.ci-detail {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: flex-start;
    gap: 16px;

    .header-info {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px;
      }

      .ci-meta {
        display: flex;
        align-items: center;
        gap: 12px;

        .meta-item {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
}

.loading-container,
.empty-container {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

.content-area {
  .info-card {
    margin-bottom: 24px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 500;
    }
  }
}

.labels-section {
  .label-tag {
    margin: 4px;
    font-family: monospace;
  }
}

.event-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
