<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑配置项' : '新建配置项'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      status-icon
    >
      <el-form-item label="类型" prop="typeId">
        <el-select
          v-model="form.typeId"
          placeholder="请选择配置项类型"
          :disabled="isEdit"
          style="width: 100%"
        >
          <el-option
            v-for="type in ciTypes"
            :key="type.id"
            :label="type.name"
            :value="type.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入配置项名称（英文、数字、下划线）"
          :disabled="isEdit"
        />
      </el-form-item>

      <el-form-item label="显示名称" prop="displayName">
        <el-input
          v-model="form.displayName"
          placeholder="请输入显示名称"
        />
      </el-form-item>

      <el-form-item label="环境" prop="environment">
        <el-select
          v-model="form.environment"
          placeholder="请选择环境"
          clearable
          style="width: 100%"
        >
          <el-option label="生产环境" value="production" />
          <el-option label="测试环境" value="testing" />
          <el-option label="开发环境" value="development" />
          <el-option label="预发布环境" value="staging" />
        </el-select>
      </el-form-item>

      <el-form-item label="负责人" prop="owner">
        <el-input
          v-model="form.owner"
          placeholder="请输入负责人"
        />
      </el-form-item>

      <el-form-item label="部门" prop="department">
        <el-input
          v-model="form.department"
          placeholder="请输入所属部门"
        />
      </el-form-item>

      <el-form-item label="位置" prop="location">
        <el-input
          v-model="form.location"
          placeholder="请输入位置信息"
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入描述信息"
        />
      </el-form-item>

      <!-- 动态属性区域 -->
      <el-divider v-if="dynamicAttributes.length > 0">扩展属性</el-divider>

      <template v-for="attr in dynamicAttributes" :key="attr.key">
        <el-form-item :label="attr.label" :prop="'attributes.' + attr.key">
          <el-input
            v-if="attr.type === 'text'"
            v-model="form.attributes[attr.key]"
            :placeholder="`请输入${attr.label}`"
          />
          <el-input-number
            v-else-if="attr.type === 'number'"
            v-model="form.attributes[attr.key]"
            :placeholder="`请输入${attr.label}`"
            style="width: 100%"
          />
          <el-switch
            v-else-if="attr.type === 'boolean'"
            v-model="form.attributes[attr.key]"
          />
          <el-input
            v-else
            v-model="form.attributes[attr.key]"
            :placeholder="`请输入${attr.label}`"
          />
        </el-form-item>
      </template>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useCmdbStore } from '@/stores/cmdb';
import type { CIResponse, CICreateCommand } from '@/types/cmdb';

const props = defineProps<{
  visible: boolean;
  ci: CIResponse | null;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  success: [];
}>();

const cmdbStore = useCmdbStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

// 表单数据
const form = reactive<CICreateCommand>({
  typeId: '',
  name: '',
  displayName: '',
  attributes: {},
  labels: {},
  owner: '',
  department: '',
  location: '',
  environment: '',
  description: '',
  createdBy: '',
});

// 表单验证规则
const rules: FormRules = {
  typeId: [
    { required: true, message: '请选择配置项类型', trigger: 'change' },
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '名称只能包含字母、数字、下划线、横线', trigger: 'blur' },
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
  ],
};

// 是否是编辑模式
const isEdit = computed(() => props.ci !== null);

// CI类型列表（实际应从API获取）
const ciTypes = ref([
  { id: 'server', name: '服务器' },
  { id: 'database', name: '数据库' },
  { id: 'application', name: '应用程序' },
  { id: 'network', name: '网络设备' },
  { id: 'storage', name: '存储设备' },
  { id: 'container', name: '容器' },
  { id: 'vm', name: '虚拟机' },
]);

// 动态属性配置（实际应从CIType定义获取）
const dynamicAttributes = computed(() => {
  const typeAttrs: Record<string, { key: string; label: string; type: string }[]> = {
    server: [
      { key: 'ip', label: 'IP地址', type: 'text' },
      { key: 'cpu_cores', label: 'CPU核心数', type: 'number' },
      { key: 'memory_gb', label: '内存(GB)', type: 'number' },
      { key: 'os', label: '操作系统', type: 'text' },
    ],
    database: [
      { key: 'db_type', label: '数据库类型', type: 'text' },
      { key: 'version', label: '版本', type: 'text' },
      { key: 'port', label: '端口', type: 'number' },
    ],
    application: [
      { key: 'version', label: '版本号', type: 'text' },
      { key: 'port', label: '服务端口', type: 'number' },
    ],
  };
  
  return typeAttrs[form.typeId] || [];
});

// 监听对话框显示
watch(() => props.visible, (val) => {
  if (val && props.ci) {
    // 编辑模式
    form.typeId = props.ci.typeId;
    form.name = props.ci.name;
    form.displayName = props.ci.displayName || '';
    form.attributes = { ...props.ci.attributes };
    form.labels = { ...props.ci.labels };
    form.owner = props.ci.owner || '';
    form.department = props.ci.department || '';
    form.location = props.ci.location || '';
    form.environment = props.ci.environment || '';
    form.description = props.ci.description || '';
  } else if (val && !props.ci) {
    // 创建模式
    form.typeId = '';
    form.name = '';
    form.displayName = '';
    form.attributes = {};
    form.labels = {};
    form.owner = '';
    form.department = '';
    form.location = '';
    form.environment = '';
    form.description = '';
  }
});

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      if (isEdit.value && props.ci) {
        // 更新
        await cmdbStore.updateCI(props.ci.id, {
          attributes: form.attributes,
          labels: form.labels,
          owner: form.owner,
          displayName: form.displayName,
          department: form.department,
          location: form.location,
          environment: form.environment,
          description: form.description,
        });
      } else {
        // 创建
        await cmdbStore.createCI({
          typeId: form.typeId,
          name: form.name.toLowerCase(),
          displayName: form.displayName,
          attributes: form.attributes,
          labels: form.labels,
          owner: form.owner,
          department: form.department,
          location: form.location,
          environment: form.environment,
          description: form.description,
        });
      }
      emit('success');
      handleClose();
    } catch {
      ElMessage.error('操作失败');
    } finally {
      loading.value = false;
    }
  });
};

// 关闭
const handleClose = () => {
  emit('update:visible', false);
};
</script>
