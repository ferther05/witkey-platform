import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/api/request'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录 / 注册' }
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'tasks',
        name: 'taskHall',
        component: () => import('@/views/TaskHall.vue'),
        meta: { title: '任务大厅' }
      },
      {
        path: 'tasks/publish',
        name: 'taskPublish',
        component: () => import('@/views/TaskPublish.vue'),
        meta: { title: '发布任务', requiresAuth: true }
      },
      {
        path: 'tasks/:id(\\d+)',
        name: 'taskDetail',
        component: () => import('@/views/TaskDetail.vue'),
        meta: { title: '任务详情' }
      },
      {
        path: 'tasks/:id(\\d+)/edit',
        name: 'taskEdit',
        component: () => import('@/views/TaskPublish.vue'),
        meta: { title: '编辑任务', requiresAuth: true }
      },
      {
        path: 'my-tasks',
        name: 'myTasks',
        component: () => import('@/views/MyTasks.vue'),
        meta: { title: '我的任务', requiresAuth: true }
      },
      {
        path: 'my-bids',
        name: 'myBids',
        component: () => import('@/views/MyBids.vue'),
        meta: { title: '我的投标', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/OrdersView.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'orders/:id(\\d+)',
        name: 'orderDetail',
        component: () => import('@/views/OrderDetail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      {
        path: 'wallet',
        name: 'wallet',
        component: () => import('@/views/WalletView.vue'),
        meta: { title: '我的钱包', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'notice',
        name: 'notice',
        component: () => import('@/views/NoticeView.vue'),
        meta: { title: '消息中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'adminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据看板', requiresAuth: true }
      },
      {
        path: 'tasks',
        name: 'adminTasks',
        component: () => import('@/views/admin/TaskAudit.vue'),
        meta: { title: '任务审核', requiresAuth: true }
      },
      {
        path: 'categories',
        name: 'adminCategories',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类维护', requiresAuth: true }
      },
      {
        path: 'finance',
        name: 'adminFinance',
        component: () => import('@/views/admin/FinanceView.vue'),
        meta: { title: '财务总览', requiresAuth: true }
      },
      {
        path: 'files',
        name: 'adminFiles',
        component: () => import('@/views/admin/FileManage.vue'),
        meta: { title: '文件管理', requiresAuth: true }
      },
      {
        path: 'users',
        name: 'adminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', requiresAuth: true }
      },
      {
        path: 'reports',
        name: 'adminReports',
        component: () => import('@/views/admin/ReportManage.vue'),
        meta: { title: '举报处理', requiresAuth: true }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 威客帮` : '威客帮'
  if (to.meta.requiresAuth && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && getToken()) {
    return { path: '/home' }
  }
  return true
})

export default router
