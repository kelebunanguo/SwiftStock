import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import ProductList from '../views/product/ProductList.vue'
import ProductForm from '../views/product/ProductForm.vue'
import OrderList from '../views/order/OrderList.vue'
import OrderForm from '../views/order/OrderForm.vue'
import OrderView from '../views/order/OrderView.vue'
import InventoryList from '../views/inventory/InventoryList.vue'
import InventoryRecords from '../views/inventory/InventoryRecords.vue'
import ReportStock from '../views/report/ReportStock.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList,
    meta: { requiresAuth: true }
  },
  {
    path: '/products/add',
    name: 'ProductAdd',
    component: ProductForm,
    meta: { requiresAuth: true }
  },
  {
    path: '/products/edit/:id',
    name: 'ProductEdit',
    component: ProductForm,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: OrderList,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/add',
    name: 'OrderAdd',
    component: OrderForm,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/view/:id',
    name: 'OrderView',
    component: OrderView,
    meta: { requiresAuth: true }
  },
  {
    path: '/inventory',
    name: 'InventoryList',
    component: InventoryList,
    meta: { requiresAuth: true }
  },
  {
    path: '/inventory/records/:productId',
    name: 'InventoryRecords',
    component: InventoryRecords,
    meta: { requiresAuth: true }
  },
  {
    path: '/reports/stock',
    name: 'ReportStock',
    component: ReportStock,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫（前端鉴权）
 *
 * 规则：
 * - 路由 meta.requiresAuth=true 时必须存在 token，否则跳转 /login
 * - 已登录状态访问 /login 会自动跳到 /dashboard
 *
 * 说明：这里的 token 有效性校验为“简化版本”（仅判断非空/非 null/非 undefined）。
 * 实际生产应调用后端校验 token 或解析 JWT 过期时间。
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 检查token是否有效（这里简化处理，实际项目中应该验证token的有效性）
  const isAuthenticated = token && token !== 'null' && token !== 'undefined'
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    // 清除可能存在的无效token
    localStorage.removeItem('token')
    next('/login')
  } else if (to.path === '/login' && isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
