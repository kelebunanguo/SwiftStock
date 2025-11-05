<template>
  <AppLayout>
    <div class="page-header">
      <h2>订单管理</h2>
      <p>管理订单信息和状态</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input
            v-model="searchForm.customerName"
            placeholder="请输入客户姓名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待付款" value="UNPAID" />
            <el-option label="已付款" value="PAID" />
            <el-option label="配货中" value="PREPARING" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已送达" value="DELIVERED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="button-group">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建订单
      </el-button>
    </div>

    <!-- 订单表格 -->
    <div class="table-container">
      <el-table
        :data="orders"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="customerPhone" label="联系电话" width="130" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click="handleView(scope.row)"
              >
                查看
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </AppLayout>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { orderAPI } from '@/api'
import AppLayout from '@/components/AppLayout.vue'

export default {
  name: 'OrderList',
  components: {
    Search,
    Refresh,
    Plus,
    AppLayout
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    
    const searchForm = reactive({
      orderNo: '',
      customerName: '',
      status: ''
    })
    
    const pagination = reactive({
      page: 1,
      size: 10,
      total: 0
    })
    
    const orders = ref([])
    
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
    
    // 加载订单列表
    const loadOrders = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.page,
          size: pagination.size,
          ...searchForm
        }
        
        const response = await orderAPI.getList(params)
        orders.value = response.data.list || []
        pagination.total = response.data.total || 0
      } catch (error) {
        console.error('加载订单列表失败:', error)
        ElMessage.error('加载订单列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.page = 1
      loadOrders()
    }
    
    // 重置
    const handleReset = () => {
      searchForm.orderNo = ''
      searchForm.customerName = ''
      searchForm.status = ''
      pagination.page = 1
      loadOrders()
    }
    
    // 创建订单
    const handleCreate = () => {
      router.push('/orders/add')
    }
    
    // 查看订单
    const handleView = (row) => {
      router.push(`/orders/view/${row.id}`)
    }
    
    // 删除订单
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除订单 ${row.orderNo} 吗？此操作不可恢复！`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await orderAPI.delete(row.id)
        if (response.success) {
          ElMessage.success('订单删除成功')
          loadOrders()
        } else {
          ElMessage.error(response.message || '订单删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除订单失败:', error)
          ElMessage.error('删除订单失败')
        }
      }
    }
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.page = 1
      loadOrders()
    }
    
    // 当前页变化
    const handleCurrentChange = (page) => {
      pagination.page = page
      loadOrders()
    }
    
    onMounted(() => {
      loadOrders()
    })
    
    return {
      loading,
      searchForm,
      pagination,
      orders,
      getStatusType,
      getStatusText,
      handleSearch,
      handleReset,
      handleCreate,
      handleView,
      handleDelete,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  color: #333;
  margin: 0 0 8px 0;
}

.page-header p {
  color: #666;
  margin: 0;
}

.search-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.button-group {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 0;
}

@media (max-width: 768px) {
  .button-group {
    flex-direction: column;
  }
  
  .search-form .el-form {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
