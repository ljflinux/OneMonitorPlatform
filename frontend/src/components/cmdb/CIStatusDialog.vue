<template>
  <el-dialog
    v-model="visible"
    title="变更配置项状态"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="ci" class="status-change-dialog">
      <!-- 当前状态 -->
      <div class="current-status">
        <span class="label">当前状态：</span>
        <el-tag :type="getStatusType(ci.status)" size="large">
          {{ ci.statusDisplayName }}
        </el-tag>
      </div>

      <!-- 状态箭头 -->
      <div class="status-arrow">
        <el-icon><Right /></el-icon>
      </div>

      <!-- 目标状态选择 -->
      <div class="target-status">
        <span class="label">目标状态：</span>
        <el-select
          v-model="targetStatus"
          placeholder="请选择目标状态"
          style="width: 200px"
        >
          <el-option
            v-for="status in availableStatuses"
            :key="status.value"
            :label="status.label"
            :value="status.value"
          />
        </el-select>
      </div>

      <!-- 变更原因 -->
      <div class="reason-section">
        <span class="label">变更原因：</span>
        <el-input
          v-model="reason"
          type="textarea"
          :rows="3"
          placeholder="请输入状态变更原因（必填）"
        />
      </div>

      <!-- 状态说明 -->
      <div class="status-info">
        <el-alert
          v-if="statusDescription"
          :title="statusDescription"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        :loading="loading" 
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        确认变更
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Right } from '@element-plus/icons-vue';
import { useCmdbStore } from '@/stores/cmdb';
import type { CIResponse, CILifecycleStatus } from '@/types/cmdb';

const props = defineProps<{
  visible: boolean;
  ci: CIResponse | null;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  success: [];
}>();

const cmdbStore = useCmdbStore();
const loading = ref(false);
const targetStatus = ref<CILifecycleStatus | ''>('');
const reason = ref('');

// 状态配置
const statusConfig: Record<CILifecycleStatus, { label: string; description: string }> = {
  ACTIVE: { label: '活跃', description: '配置项正常运行中' },
  MAINTENANCE: { label: '维护中', description: '配置项正在进行维护，暂时不可用' },
  DECOMMISSIONED: { label: '已退役', description: '配置项已停止服务，将被替换或废弃' },
  DISPOSED: { label: '已处置', description: '配置项数据已归档或删除' },
};

// 可用的目标状态
const availableStatuses = computed(() => {
  if (!props.ci) return [];

  const currentStatus = props.ci.status;
  const available: { value: CILifecycleStatus; label: string }[] = [];

  // 根据当前状态确定可用的目标状态
  if (currentStatus === 'ACTIVE') {
    available.push({ value: 'MAINTENANCE', label: '维护中' });
    available.push({ value: 'DECOMMISSIONED', label: '已退役' });
  } else if (currentStatus === 'MAINTENANCE') {
    available.push({ value: 'ACTIVE', label: '活跃' });
    available.push({ value: 'DECOMMISSIONED', label: '已退役' });
  } else if (currentStatus === 'DECOMMISSIONED') {
    available.push({ value: 'DISPOSED', label: '已处置' });
  }

  return available;
});

// 状态说明
const statusDescription = computed(() => {
  if (!targetStatus.value) return '';
  return statusConfig[targetStatus.value]?.description || '';
});

// 是否可以提交
const canSubmit = computed(() => {
  return targetStatus.value !== '' && reason.value.trim().length > 0;
});

// 状态类型
const getStatusType = (status: CILifecycleStatus): string => {
  const typeMap: Record<CILifecycleStatus, string> = {
    ACTIVE: 'success',
    MAINTENANCE: 'warning',
    DECOMMISSIONED: 'info',
    DISPOSED: 'danger',
  };
  return typeMap[status] || 'info';
};

// 监听对话框显示
watch(() => props.visible, (val) => {
  if (val) {
    targetStatus.value = '';
    reason.value = '';
  }
});

// 提交
const handleSubmit = async () => {
  if (!props.ci || !targetStatus.value) return;
  if (!reason.value.trim()) {
    ElMessage.warning('请输入状态变更原因');
    return;
  }

  loading.value = true;
  try {
    await cmdbStore.changeStatus(props.ci.id, targetStatus.value, reason.value);
    emit('success');
    handleClose();
  } catch {
    ElMessage.error('状态变更失败');
  } finally {
    loading.value = false;
  }
};

// 关闭
const handleClose = () => {
  emit('update:visible', false);
};
</script>

<style lang="scss" scoped>
.status-change-dialog {
  .current-status,
  .target-status,
  .reason-section {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;

    .label {
      width: 100px;
      flex-shrink: 0;
      font-weight: 500;
      color: #606266;
      line-height: 32px;
    }
  }

  .status-arrow {
    display: flex;
    justify-content: center;
    margin: 10px 0 20px 100px;
    color: #909399;
    font-size: 24px;
  }

  .reason-section {
    align-items: flex-start;

    .el-input {
      flex: 1;
    }
  }

  .status-info {
    margin-left: 100px;
  }
}
</style>
