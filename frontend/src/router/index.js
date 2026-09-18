import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import OwnerLayout from '@/layouts/OwnerLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: OwnerLayout,
    redirect: '/notice',
    meta: { requiresAuth: true, role: 0 },
    children: [
      { path: 'notice', name: 'NoticeList', component: () => import('@/views/owner/NoticeList.vue') },
      { path: 'notice/:id', name: 'NoticeDetail', component: () => import('@/views/owner/NoticeDetail.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/owner/Profile.vue') },
      { path: 'pay', name: 'MyBill', component: () => import('@/views/owner/Pay.vue') },
      { path: 'repair', component: () => import('@/views/Placeholder.vue'), meta: { title: '报修服务' } },
      { path: 'market', component: () => import('@/views/Placeholder.vue'), meta: { title: '二手市场' } },
      { path: 'visitor', component: () => import('@/views/Placeholder.vue'), meta: { title: '访客登记' } },
      { path: 'ai', component: () => import('@/views/Placeholder.vue'), meta: { title: 'AI 客服' } }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/notice',
    meta: { requiresAuth: true, role: 1 },
    children: [
      { path: 'dashboard', component: () => import('@/views/Placeholder.vue'), meta: { title: '数据看板' } },
      { path: 'notice', name: 'AdminNotice', component: () => import('@/views/admin/NoticeManage.vue'), meta: { title: '公告管理' } },
      { path: 'bill', name: 'AdminBill', component: () => import('@/views/admin/BillManage.vue'), meta: { title: '缴费管理' } },
      { path: 'repair', component: () => import('@/views/Placeholder.vue'), meta: { title: '工单管理' } },
      { path: 'goods', component: () => import('@/views/Placeholder.vue'), meta: { title: '商品审核' } },
      { path: 'visitor', component: () => import('@/views/Placeholder.vue'), meta: { title: '访客管理' } },
      { path: 'knowledge', component: () => import('@/views/Placeholder.vue'), meta: { title: 'AI 知识库' } },
      { path: 'log', component: () => import('@/views/Placeholder.vue'), meta: { title: '操作日志' } }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const roleRule = to.matched.find((record) => record.meta.role !== undefined)?.meta.role

  if (requiresAuth && !store.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (roleRule !== undefined && store.token && store.userInfo.role !== roleRule) {
    return store.userInfo.role === 1 ? '/admin' : '/'
  }

  if ((to.path === '/login' || to.path === '/register') && store.token) {
    return store.userInfo.role === 1 ? '/admin' : '/'
  }

  return true
})

export default router
