import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        redirect: '/monitor/server',
        meta: { title: '监控管理' },
        children: [
          {
            path: 'server',
            name: 'ServerMonitor',
            component: () => import('../views/monitor/ServerMonitor.vue'),
            meta: { title: '服务器监控' }
          },
          {
            path: 'network',
            name: 'NetworkMonitor',
            component: () => import('../views/monitor/NetworkMonitor.vue'),
            meta: { title: '网络设备监控' }
          },
          {
            path: 'cloud',
            name: 'CloudMonitor',
            component: () => import('../views/monitor/CloudMonitor.vue'),
            meta: { title: '云平台监控' }
          },
          {
            path: 'k8s',
            name: 'K8sMonitor',
            component: () => import('../views/monitor/K8sMonitor.vue'),
            meta: { title: '容器K8s监控' }
          },
          {
            path: 'business',
            name: 'BusinessMonitor',
            component: () => import('../views/monitor/BusinessMonitor.vue'),
            meta: { title: '业务系统监控' }
          }
        ]
      },
      {
        path: 'alert',
        name: 'Alert',
        redirect: '/alert/rule',
        meta: { title: '告警管理' },
        children: [
          {
            path: 'rule',
            name: 'AlertRule',
            component: () => import('../views/alert/AlertRule.vue'),
            meta: { title: '告警规则' }
          },
          {
            path: 'history',
            name: 'AlertHistory',
            component: () => import('../views/alert/AlertHistory.vue'),
            meta: { title: '告警历史' }
          }
        ]
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('../views/Report.vue'),
        meta: { title: '报表中心' }
      },
      {
        path: 'settings',
        name: 'Settings',
        redirect: '/settings/user',
        meta: { title: '系统设置' },
        children: [
          {
            path: 'user',
            name: 'UserManagement',
            component: () => import('../views/settings/UserManagement.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'system',
            name: 'SystemSettings',
            component: () => import('../views/settings/SystemSettings.vue'),
            meta: { title: '系统配置' }
          }
        ]
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面不存在' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - OneMonitor`
  } else {
    document.title = 'OneMonitor - 企业级一体化运维监控平台'
  }

  const userStore = useUserStore()
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (userStore.isLoggedIn) {
      next()
    } else {
      next({ name: 'Login' })
    }
  } else {
    next()
  }
})

export default router