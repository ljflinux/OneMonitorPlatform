<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="layout-header">
      <div class="header-content">
        <div class="header-left">
          <el-button 
            type="text" 
            icon="Menu" 
            @click="toggleSidebar"
            class="sidebar-toggle"
          />
          <div class="logo">OneMonitor</div>
        </div>
        <div class="header-right">
          <el-dropdown>
            <el-button type="text" class="user-info">
              <el-avatar size="small" :src="userStore.userInfo?.avatar">{{ userStore.fullName.charAt(0) }}</el-avatar>
              <span>{{ userStore.fullName }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">
                  <el-icon><user /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="handleSettings">
                  <el-icon><setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><switch-button /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <!-- 主体内容 -->
    <main class="layout-main">
      <!-- 侧边栏 -->
      <aside class="layout-sidebar" :class="{ collapsed }">
        <el-menu 
          :default-active="activeMenu" 
          class="el-menu-vertical-demo"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#ffd04b"
          :collapse="collapsed"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><data-analysis /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>
          
          <el-sub-menu index="monitor">
            <template #title>
              <el-icon><monitor /></el-icon>
              <span>监控管理</span>
            </template>
            <el-menu-item index="/monitor/server">服务器监控</el-menu-item>
            <el-menu-item index="/monitor/network">网络设备监控</el-menu-item>
            <el-menu-item index="/monitor/cloud">云平台监控</el-menu-item>
            <el-menu-item index="/monitor/k8s">容器K8s监控</el-menu-item>
            <el-menu-item index="/monitor/business">业务系统监控</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="alert">
            <template #title>
              <el-icon><warning /></el-icon>
              <span>告警管理</span>
            </template>
            <el-menu-item index="/alert/rule">告警规则</el-menu-item>
            <el-menu-item index="/alert/history">告警历史</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/report">
            <el-icon><document /></el-icon>
            <template #title>报表中心</template>
          </el-menu-item>
          
          <el-sub-menu index="settings">
            <template #title>
              <el-icon><setting /></el-icon>
              <span>系统设置</span>
            </template>
            <el-menu-item index="/settings/user">用户管理</el-menu-item>
            <el-menu-item index="/settings/system">系统配置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </aside>
      
      <!-- 内容区域 -->
      <section class="layout-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

// 状态
const collapsed = ref(false)
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

// 处理用户信息
const handleProfile = () => {
  // TODO: 跳转到个人中心
  console.log('Go to profile')
}

// 处理系统设置
const handleSettings = () => {
  router.push('/settings/system')
}

// 处理退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.sidebar-toggle {
  font-size: 20px;
  color: #333;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #001529;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #333;
  font-size: 14px;
}

.el-menu {
  height: 100%;
  border-right: none;
}

.el-menu-item {
  height: 60px;
  line-height: 60px;
  font-size: 14px;
}

.el-sub-menu__title {
  height: 60px;
  line-height: 60px;
  font-size: 14px;
}
</style>