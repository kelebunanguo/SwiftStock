<template>
  <div class="stock-alert-widget">
    <div class="alert-header">
      <h3>库存预警</h3>
      <el-button type="text" @click="refreshAlerts">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    
    <div class="alert-stats" v-if="alertStats">
      <div class="stat-item low-stock">
        <div class="stat-number">{{ alertStats.lowStockCount }}</div>
        <div class="stat-label">库存不足</div>
      </div>
      <div class="stat-item out-stock">
        <div class="stat-number">{{ alertStats.outOfStockCount }}</div>
        <div class="stat-label">缺货商品</div>
      </div>
    </div>
    
    <div class="alert-list" v-if="lowStockProducts.length > 0">
      <div class="alert-title">需要关注的商品：</div>
      <div class="alert-items">
        <div 
          v-for="product in lowStockProducts.slice(0, 5)" 
          :key="product.id"
          class="alert-item"
        >
          <span class="product-name">{{ product.name }}</span>
          <span class="stock-info">
            库存: {{ product.stockQuantity }} / 安全库存: {{ product.minStockLevel }}
          </span>
        </div>
      </div>
      <div v-if="lowStockProducts.length > 5" class="more-alerts">
        还有 {{ lowStockProducts.length - 5 }} 个商品需要关注...
      </div>
    </div>
    
    <div v-else class="no-alerts">
      <el-icon><Check /></el-icon>
      <span>所有商品库存正常</span>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check } from '@element-plus/icons-vue'
import { stockAlertAPI } from '@/api'

export default {
  name: 'StockAlertWidget',
  components: {
    Refresh,
    Check
  },
  setup() {
    const alertStats = ref(null)
    const lowStockProducts = ref([])
    const loading = ref(false)
    
    // 加载预警信息
    const loadAlertInfo = async () => {
      loading.value = true
      try {
        const response = await stockAlertAPI.getAlertInfo()
        if (response.success) {
          alertStats.value = {
            lowStockCount: response.data.lowStockCount,
            outOfStockCount: response.data.outOfStockCount,
            totalAlertCount: response.data.totalAlertCount
          }
          lowStockProducts.value = response.data.lowStockProducts || []
        }
      } catch (error) {
        console.error('加载库存预警信息失败:', error)
        ElMessage.error('加载库存预警信息失败')
      } finally {
        loading.value = false
      }
    }
    
    // 刷新预警信息
    const refreshAlerts = () => {
      loadAlertInfo()
    }
    
    onMounted(() => {
      loadAlertInfo()
    })
    
    return {
      alertStats,
      lowStockProducts,
      loading,
      refreshAlerts
    }
  }
}
</script>

<style scoped>
.stock-alert-widget {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.alert-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.alert-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  border-radius: 6px;
}

.stat-item.low-stock {
  background: #fff7e6;
  border: 1px solid #ffd591;
}

.stat-item.out-stock {
  background: #fff2f0;
  border: 1px solid #ffccc7;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.alert-list {
  margin-top: 16px;
}

.alert-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.alert-items {
  max-height: 200px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 4px;
  background: #fafafa;
  border-radius: 4px;
  font-size: 13px;
}

.product-name {
  color: #333;
  font-weight: 500;
}

.stock-info {
  color: #666;
  font-size: 12px;
}

.more-alerts {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-top: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.no-alerts {
  text-align: center;
  color: #52c41a;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.no-alerts .el-icon {
  font-size: 24px;
}
</style>
