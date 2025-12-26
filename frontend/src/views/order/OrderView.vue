<template>
  <AppLayout>
    <div class="page-header">
      <div class="header-left">
        <h2>订单详情</h2>
        <p>查看订单信息和状态</p>
      </div>
      <div class="header-right">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
    </div>

    <!-- 订单基本信息 -->
    <div class="order-info-card">
      <div class="card-header">
        <h3>订单信息</h3>
        <el-tag :type="getStatusType(order.status)" size="large">
          {{ getStatusText(order.status) }}
        </el-tag>
      </div>
      <div class="order-details">
        <div class="detail-row">
          <div class="detail-item">
            <label>订单号：</label>
            <span>{{ order.orderNo }}</span>
          </div>
          <div class="detail-item">
            <label>客户姓名：</label>
            <span>{{ order.customerName }}</span>
          </div>
          <div class="detail-item">
            <label>联系电话：</label>
            <span>{{ order.customerPhone }}</span>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item">
            <label>订单金额：</label>
            <span class="amount">¥{{ order.totalAmount }}</span>
          </div>
          <div class="detail-item">
            <label>创建时间：</label>
            <span>{{ formatDateTime(order.createdTime) }}</span>
          </div>
          <div class="detail-item">
            <label>更新时间：</label>
            <span>{{ order.updatedTime ? formatDateTime(order.updatedTime) : '-' }}</span>
          </div>
        </div>
        <div class="detail-row" v-if="order.remark">
          <div class="detail-item full-width">
            <label>备注：</label>
            <span>{{ order.remark }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单商品 -->
    <div class="order-items-card">
      <div class="card-header">
        <h3>订单商品</h3>
      </div>
      <el-table :data="order.items" style="width: 100%">
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="price" label="单价" width="120">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="小计" width="120">
          <template #default="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 状态操作 -->
    <div class="status-actions-card" v-if="canOperate">
      <div class="card-header">
        <h3>状态操作</h3>
      </div>
      <div class="action-buttons">
        <el-button
          v-if="order.status === 'UNPAID'"
          type="success"
          @click="handleStatusTransition('PAID', '客户付款')"
        >
          标记已付款
        </el-button>
        <el-button
          v-if="order.status === 'PAID'"
          type="warning"
          @click="handleStatusTransition('PREPARING', '开始配货')"
        >
          开始配货
        </el-button>
        <el-button
          v-if="order.status === 'PREPARING'"
          type="info"
          @click="handleStatusTransition('SHIPPED', '商品已发货')"
        >
          标记已发货
        </el-button>
        <el-button
          v-if="order.status === 'SHIPPED'"
          type="primary"
          @click="handleStatusTransition('DELIVERED', '商品已送达')"
        >
          标记已送达
        </el-button>
        <el-button
          v-if="order.status === 'DELIVERED'"
          type="success"
          @click="handleStatusTransition('COMPLETED', '订单完成')"
        >
          完成订单
        </el-button>
        <el-button
          v-if="canCancelOrder"
          type="danger"
          @click="handleCancelOrder"
        >
          取消订单
        </el-button>
        <el-button
          v-if="canRefundOrder"
          type="warning"
          @click="handleRefundOrder"
        >
          申请退款
        </el-button>
      </div>
    </div>

    <!-- 状态历史 -->
    <div class="status-history-card">
      <div class="card-header">
        <h3>状态历史</h3>
      </div>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in statusHistory"
          :key="index"
          :timestamp="item.time"
        >
          <el-tag :type="getStatusType(item.status)">
            {{ item.statusText }}
          </el-tag>
          <p>{{ item.reason }}</p>
        </el-timeline-item>
      </el-timeline>
    </div>
  </AppLayout>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { orderAPI } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import { formatDateTime } from '@/utils/time'
// 时间格式化导入

export default {
  name: 'OrderView',
  components: {
    ArrowLeft,
    AppLayout
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const order = ref({})
    const statusHistory = ref([])
    const loading = ref(false)

    // 计算是否可以操作
    const canOperate = computed(() => {
      return order.value.status && !['COMPLETED', 'CANCELLED'].includes(order.value.status)
    })
    
    // 计算是否可以取消订单
    const canCancelOrder = computed(() => {
      return order.value.status && !['COMPLETED', 'CANCELLED', 'REFUNDED'].includes(order.value.status)
    })
    
    // 计算是否可以退款
    const canRefundOrder = computed(() => {
      return order.value.status && ['PAID', 'PREPARING', 'SHIPPED', 'DELIVERED'].includes(order.value.status)
    })

    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        'UNPAID': 'warning',
        'PAID': 'success',
        'PREPARING': 'info',
        'SHIPPED': 'primary',
        'DELIVERED': 'success',
        'COMPLETED': 'success',
        'CANCELLED': 'danger',
        'REFUNDED': 'info'
      }
      return statusMap[status] || 'info'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'UNPAID': '待付款',
        'PAID': '已付款',
        'PREPARING': '配货中',
        'SHIPPED': '已发货',
        'DELIVERED': '已送达',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消',
        'REFUNDED': '已退款'
      }
      return statusMap[status] || status
    }

    // 加载订单详情
    const loadOrderDetails = async () => {
      loading.value = true
      try {
        const orderId = route.params.id
        const response = await orderAPI.getById(orderId)
        if (response.success) {
          order.value = response.data || {}
        } else {
          ElMessage.error(response.message || '加载订单详情失败')
        }
        
        // 加载状态历史
        await loadStatusHistory()
      } catch (error) {
        console.error('加载订单详情失败:', error)
        ElMessage.error('加载订单详情失败')
      } finally {
        loading.value = false
      }
    }

    // 加载状态历史
    const loadStatusHistory = async () => {
      try {
        const orderId = route.params.id
        const response = await orderAPI.getStatusHistory(orderId)
        if (response.success) {
          statusHistory.value = response.data || []
        }
      } catch (error) {
        console.error('加载状态历史失败:', error)
      }
    }

    // 状态流转
    const handleStatusTransition = async (targetStatus, reason) => {
      try {
        await ElMessageBox.confirm(
          `确定要将订单状态更新为 ${getStatusText(targetStatus)} 吗？`,
          '确认操作',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await orderAPI.transitionStatus(order.value.id, {
          status: targetStatus,
          reason: reason
        })

        if (response.success) {
          ElMessage.success('状态更新成功')
          await loadOrderDetails()
        } else {
          ElMessage.error(response.message || '状态更新失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('状态更新失败:', error)
          ElMessage.error('状态更新失败')
        }
      }
    }

    // 取消订单
    const handleCancelOrder = async () => {
      try {
        const { value: reason } = await ElMessageBox.prompt(
          '请输入取消原因',
          '取消订单',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: '请输入取消原因'
          }
        )

        const response = await orderAPI.cancelOrder(order.value.id, { reason })
        if (response.success) {
          ElMessage.success('订单已取消')
          await loadOrderDetails()
        } else {
          ElMessage.error(response.message || '取消订单失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消订单失败:', error)
          ElMessage.error('取消订单失败')
        }
      }
    }
    
    // 申请退款
    const handleRefundOrder = async () => {
      try {
        const { value: reason } = await ElMessageBox.prompt(
          '请输入退款原因',
          '申请退款',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: '请输入退款原因'
          }
        )

        await orderAPI.transitionStatus(order.value.id, {
          status: 'REFUNDED',
          reason: reason
        })
        ElMessage.success('退款申请已提交')
        await loadOrderDetails()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('申请退款失败:', error)
          ElMessage.error('申请退款失败')
        }
      }
    }

    // 返回
    const goBack = () => {
      router.go(-1)
    }

    onMounted(() => {
      loadOrderDetails()
    })

    return {
      order,
      statusHistory,
      loading,
      canOperate,
      canCancelOrder,
      canRefundOrder,
      getStatusType,
      getStatusText,
      formatDateTime,
      handleStatusTransition,
      handleCancelOrder,
      handleRefundOrder,
      goBack
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  color: #333;
  margin: 0 0 8px 0;
}

.header-left p {
  color: #666;
  margin: 0;
}

.order-info-card,
.order-items-card,
.status-actions-card,
.status-history-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  min-width: 200px;
}

.detail-item.full-width {
  flex: 1;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  margin-right: 8px;
  min-width: 80px;
}

.detail-item span {
  color: #333;
}

.detail-item .amount {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .detail-item {
    min-width: auto;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>