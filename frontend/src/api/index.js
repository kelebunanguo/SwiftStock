import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 前端 API 访问层（Axios 封装）
 *
 * 设计要点：
 * - baseURL 统一加上后端 context-path：/swiftstock
 * - 请求拦截器：自动附带 token（JWT）
 * - 响应拦截器：统一返回 response.data，并对常见 HTTP 错误做提示与跳转
 */
// 创建 axios 实例
const api = axios.create({
  baseURL: '/swiftstock',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：将本地 token 写入 Authorization 头，便于后端基于 JWT 鉴权
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：
// 1) 统一返回 response.data（后端返回结构一般为 {success, data, message}）
// 2) 对 HTTP 错误码进行统一提示，401 时清 token 并跳转登录页
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    // If caller passed { silent: true } in request config, skip global error messages
    const isSilent = error.config && error.config.silent
    if (isSilent) {
      return Promise.reject(error)
    }

    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

// API 接口定义
export const authAPI = {
  // 登录
  login: (data) => api.post('/auth/login', data),
  // 登出
  logout: () => api.post('/auth/logout')
}

export const dashboardAPI = {
  // 获取仪表盘统计数据
  getStats: () => api.get('/dashboard/stats'),
  // 获取最近订单
  getRecentOrders: () => api.get('/dashboard/recent-orders')
}

export const productAPI = {
  // 获取商品列表
  getList: (params) => api.get('/products', { params }),
  // 获取所有可用商品（用于订单创建）
  getAvailable: () => api.get('/products/available'),
  // 获取商品详情
  getDetail: (id) => api.get(`/products/${id}`),
  // 创建商品
  create: (data) => api.post('/products', data),
  // 更新商品
  update: (id, data) => api.put(`/products/${id}`, data),
  // 删除商品
  delete: (id) => api.delete(`/products/${id}`)
}

export const orderAPI = {
  // 获取订单列表
  getList: (params) => api.get('/orders', { params }),
  // 获取订单详情
  getById: (id) => api.get(`/orders/${id}`),
  // 创建订单
  create: (data) => api.post('/orders', data),
  // 更新订单状态
  updateStatus: (id, status) => api.put(`/orders/${id}/status`, { status }),
  // 订单状态流转
  transitionStatus: (id, data) => api.put(`/orders/${id}/transition`, data),
  // 取消订单
  cancelOrder: (id, data) => api.put(`/orders/${id}/cancel`, data),
  // 获取订单状态历史
  getStatusHistory: (id) => api.get(`/orders/${id}/status-history`),
  // 删除订单
  delete: (id) => api.delete(`/orders/${id}`)
}

export const inventoryAPI = {
  // 获取库存列表
  getList: (params) => api.get('/inventory', { params }),
  // 入库
  stockIn: (data) => api.post('/inventory/in', data),
  // 出库
  stockOut: (data) => api.post('/inventory/out', data),
  // 获取库存记录
  getRecords: (productId) => api.get(`/inventory/records/${productId}`),
  // 库存操作
  stockOperation: (data) => api.post('/inventory/operation', data),
  // 批量库存操作
  batchStockOperation: (data) => api.post('/inventory/batch-operation', data)
}

export const supplierAPI = {
  // 获取供应商列表（支持分页与筛选：name, contactPerson, page, size）
  getList: (params) => api.get('/suppliers', { params }),
  // 获取全部供应商
  getAll: () => api.get('/suppliers/all'),
  // 获取供应商详情
  getById: (id) => api.get(`/suppliers/${id}`),
  // 获取供应商供货记录
  getRecords: (id) => api.get(`/suppliers/${id}/records`),
  // 创建供应商
  create: (data) => api.post('/suppliers', data),
  // 更新供应商
  update: (id, data) => api.put(`/suppliers/${id}`, data),
  // 删除供应商
  delete: (id) => api.delete(`/suppliers/${id}`)
}

export const categoryAPI = {
  // 获取分类列表
  getList: () => api.get('/categories'),
  // 创建分类
  create: (data) => api.post('/categories', data),
  // 更新分类
  update: (id, data) => api.put(`/categories/${id}`, data),
  // 删除分类
  delete: (id) => api.delete(`/categories/${id}`)
}

export const reportAPI = {
  // 获取库存报表
  getStockReport: (params) => api.get('/reports/stock', { params }),
  // 获取销售报表
  getSalesReport: (params) => api.get('/reports/sales', { params })
}

export const salesAPI = {
  // 获取销售趋势数据
  getTrend: (period) => api.get('/sales/trend', { params: { period } }),
  // 获取销售概览
  getOverview: () => api.get('/sales/overview')
}

export const stockAlertAPI = {
  // 获取库存预警信息
  getAlertInfo: () => api.get('/stock-alert/info'),
  // 检查指定商品的库存预警状态
  checkProductAlert: (productId) => api.get(`/stock-alert/check/${productId}`),
  // 获取库存预警统计
  getAlertStats: () => api.get('/stock-alert/stats')
}

export default api

// AI API
export const aiAPI = {
  // count of products need reorder
  // accepts optional axios config as second argument, e.g. { silent: true }
  getReplenishmentCount: (params, config = {}) => api.get('/api/ai/forecast/recommend-count', { params, ...config }),
  // detailed recommend list
  getRecommendList: (params, config = {}) => api.get('/api/ai/forecast/recommend-list', { params, ...config })
}
