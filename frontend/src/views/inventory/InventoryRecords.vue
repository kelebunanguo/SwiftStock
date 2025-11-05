<template>
  <div class="layout-container">
    <!-- 头部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>电商仓库管理系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            管理员
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Box /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/inventory">
            <el-icon><Grid /></el-icon>
            <span>库存管理</span>
          </el-menu-item>
          <el-menu-item index="/reports/stock">
            <el-icon><TrendCharts /></el-icon>
            <span>报表统计</span>
          </el-menu-item>
          <el-menu-item index="/settings/category">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <div class="page-header">
          <div class="header-content">
            <div class="header-left">
              <el-button @click="handleBack" class="back-button">
                <el-icon><ArrowLeft /></el-icon>
                返回
              </el-button>
              <div class="title-section">
                <h2>库存记录</h2>
                <p>查看商品库存变动记录</p>
              </div>
            </div>
          </div>
        </div>

        <div v-loading="loading" class="inventory-records">
          <!-- 商品信息 -->
          <div class="info-card" v-if="product">
            <div class="card-header">
              <h3>商品信息</h3>
            </div>
            <div class="card-content">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="info-item">
                    <label>商品名称：</label>
                    <span>{{ product.name }}</span>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="info-item">
                    <label>商品编码：</label>
                    <span>{{ product.code }}</span>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="info-item">
                    <label>当前库存：</label>
                    <span class="stock-quantity">{{ product.stockQuantity }}</span>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="info-item">
                    <label>最低库存：</label>
                    <span>{{ product.minStockLevel }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>

          <!-- 库存记录表格 -->
          <div class="info-card">
            <div class="card-header">
              <h3>库存记录</h3>
            </div>
            <div class="card-content">
              <el-table :data="records" style="width: 100%">
                <el-table-column prop="type" label="操作类型" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.type === 'IN' ? 'success' : 'warning'">
                      {{ scope.row.type === 'IN' ? '入库' : '出库' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="quantity" label="数量" width="100" />
                <el-table-column prop="reason" label="操作原因" min-width="200" />
                <el-table-column prop="createdTime" label="操作时间" width="180">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.createdTime) }}
                  </template>
                </el-table-column>
              </el-table>
              
              <div v-if="records.length === 0" class="empty-state">
                <el-empty description="暂无库存记录" />
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { inventoryAPI } from '@/api'

export default {
  name: 'InventoryRecords',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const activeMenu = ref('/inventory')
    const loading = ref(false)
    
    const product = ref(null)
    const records = ref([])

    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN')
    }

    // 加载库存记录
    const loadRecords = async () => {
      const productId = route.params.productId
      if (!productId) {
        ElMessage.error('商品ID不存在')
        router.push('/inventory')
        return
      }

      loading.value = true
      try {
        const response = await inventoryAPI.getRecords(productId)
        if (response.data) {
          product.value = response.data.product
          records.value = response.data.records || []
        } else {
          ElMessage.error('获取库存记录失败')
          router.push('/inventory')
        }
      } catch (error) {
        console.error('加载库存记录失败:', error)
        ElMessage.error('加载库存记录失败')
        router.push('/inventory')
      } finally {
        loading.value = false
      }
    }

    // 返回
    const handleBack = () => {
      router.push('/inventory')
    }

    // 处理命令
    const handleCommand = (command) => {
      if (command === 'logout') {
        localStorage.removeItem('token')
        ElMessage.success('已退出登录')
        router.push('/login')
      }
    }

    onMounted(() => {
      loadRecords()
    })

    return {
      activeMenu,
      loading,
      product,
      records,
      formatDateTime,
      handleBack,
      handleCommand
    }
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left h1 {
  color: #1890ff;
  font-size: 24px;
  margin: 0;
  display: inline-block;
}

.subtitle {
  color: #666;
  font-size: 14px;
  margin-left: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}

.main-layout {
  display: flex;
  min-height: calc(100vh - 60px);
}

.sidebar {
  width: 200px;
  background: #001529;
}

.sidebar-menu {
  border: none;
  height: 100%;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.title-section h2 {
  color: #333;
  margin: 0 0 8px 0;
}

.title-section p {
  color: #666;
  margin: 0;
}

.inventory-records {
  max-width: 1000px;
}

.info-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.card-header {
  padding: 20px 20px 0 20px;
  border-bottom: 1px solid #eee;
}

.card-header h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
}

.card-content {
  padding: 20px;
}

.info-item {
  margin-bottom: 15px;
}

.info-item label {
  font-weight: bold;
  color: #666;
  margin-right: 8px;
}

.info-item .stock-quantity {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

@media (max-width: 768px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .main-content {
    padding: 15px;
  }
  
  .card-content {
    padding: 15px;
  }
}
</style>
