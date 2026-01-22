<template>
  <div class="ci-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">配置项管理</h1>
        <p class="page-subtitle">管理企业IT基础设施配置项 (CMDB)</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建配置项
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section">
      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="活跃" value="ACTIVE" />
              <el-option label="维护中" value="MAINTENANCE" />
              <el-option label="已退役" value="DECOMMISSIONED" />
              <el-option label="已处置" value="DISPOSED" />
            </el-select>
          </el-form-item>
          <el-form-item label="环境">
            <el-select
              v-model="filterForm.environment"
              placeholder="全部环境"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="生产环境" value="production" />
              <el-option label="测试环境" value="testing" />
              <el-option label="开发环境" value="development" />
              <el-option label="预发布环境" value="staging" />
            </el-select>
          </el-form-item>
          <el-form-item label="负责人">
            <el-input
              v-model="filterForm.owner"
              placeholder="请输入负责人"
              clearable
              @change="handleFilterChange"
            />
          </el-form-item>
          <el-form-item label="搜索">
            <el-input
              v-model="filterForm.search"
              placeholder="搜索名称/标识"
              clearable
              @change="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <div class="data-section">
      <el-card shadow="never">
        <el-table
          v-loading="cmdbStore.loading"
          :data="cmdbStore.ciList"
          stripe
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="name" label="名称" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="handleViewDetail(row.id)">
                {{ row.displayName || row.name }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ row.statusDisplayName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="environment" label="环境" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.environment" type="info" size="small">
                {{ row.environment }}
              </el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="owner" label="负责人" width="120">
            <template #default="{ row }">
              <span>{{ row.owner || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" width="150">
            <template #default="{ row }">
              <span>{{ row.location || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button-group>
                <el-tooltip content="查看详情">
                  <el-button size="small" @click="handleViewDetail(row.id)">
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="编辑">
                  <el-button size="small" @click="handleEdit(row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="状态变更">
                  <el-button size="small" @click="handleStatusChange(row)">
                    <el-icon><Switch /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除">
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="handleDelete(row)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="cmdbStore.pagination.totalElements"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 创建/编辑对话框 -->
    <CICreateDialog
      v-model:visible="dialogVisible"
      :ci="selectedCI"
      @success="handleDialogSuccess"
    />

    <!-- 状态变更对话框 -->
    <CIStatusDialog
      v-model:visible="statusDialogVisible"
      :ci="selectedCI"
      @success="handleStatusDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, View, Edit, Switch, Delete } from '@element-plus/icons-vue';
import { useCmdbStore } from '@/stores/cmdb';
import type { CIResponse, CILifecycleStatus } from '@/types/cmdb';
import CICreateDialog from '@/components/cmdb/CICreateDialog.vue';
import CIStatusDialog from '@/components/cmdb/CIStatusDialog.vue';
import { formatDate } from '@/utils/date';

const router = useRouter();
const cmdbStore = useCmdbStore();

// 筛选表单
const filterForm = reactive({
  status: '' as CILifecycleStatus | '',
  environment: '',
  owner: '',
  search: '',
});

// 对话框状态
const dialogVisible = ref(false);
const statusDialogVisible = ref(false);
const selectedCI = ref<CIResponse | null>(null);

// 分页
const currentPage = ref(1);
const pageSize = ref(20);

// 选中的行
const selectedRows = ref<CIResponse[]>([]);

// 状态类型映射
const getStatusType = (status: CILifecycleStatus): string => {
  const statusMap: Record<CILifecycleStatus, string> = {
    ACTIVE: 'success',
    MAINTENANCE: 'warning',
    DECOMMISSIONED: 'info',
    DISPOSED: 'danger',
  };
  return statusMap[status] || 'info';
};

// 加载数据
const loadData = () => {
  cmdbStore.fetchCIList({
    status: filterForm.status || undefined,
    environment: filterForm.environment || undefined,
    owner: filterForm.owner || undefined,
    search: filterForm.search || undefined,
    page: currentPage.value - 1,
    size: pageSize.value,
  });
};

// 筛选变更
const handleFilterChange = () => {
  currentPage.value = 1;
  loadData();
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  loadData();
};

// 重置
const handleReset = () => {
  filterForm.status = '';
  filterForm.environment = '';
  filterForm.owner = '';
  filterForm.search = '';
  currentPage.value = 1;
  loadData();
};

// 分页
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadData();
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadData();
};

// 选择行
const handleSelectionChange = (rows: CIResponse[]) => {
  selectedRows.value = rows;
};

// 创建
const handleCreate = () => {
  selectedCI.value = null;
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (ci: CIResponse) => {
  selectedCI.value = ci;
  dialogVisible.value = true;
};

// 查看详情
const handleViewDetail = (id: string) => {
  router.push(`/cmdb/ci/${id}`);
};

// 状态变更
const handleStatusChange = (ci: CIResponse) => {
  selectedCI.value = ci;
  statusDialogVisible.value = true;
};

// 删除
const handleDelete = (ci: CIResponse) => {
  ElMessageBox.confirm(
    `确定要删除配置项 "${ci.displayName || ci.name}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await cmdbStore.deleteCI(ci.id);
      ElMessage.success('删除成功');
      loadData();
    } catch {
      ElMessage.error('删除失败');
    }
  }).catch(() => {
    // 用户取消
  });
};

// 对话框成功
const handleDialogSuccess = () => {
  ElMessage.success('操作成功');
  loadData();
};

// 状态变更成功
const handleStatusDialogSuccess = () => {
  ElMessage.success('状态变更成功');
  loadData();
};

// 初始化
onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.ci-list {
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

.filter-section {
  margin-bottom: 24px;

  .filter-card {
    :deep(.el-card__body) {
      padding: 16px 20px;
    }
  }

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
  }
}

.data-section {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.text-muted {
  color: #c0c4cc;
}

:deep(.el-table) {
  .el-link {
    font-weight: 500;
  }
}
</style>
