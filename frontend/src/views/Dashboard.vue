<template>
  <AppLayout>
    <div class="page-header">
      <h2>仪表盘</h2>
      <p>欢迎使用SwiftStock电商仓库管理系统</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon products">
          <el-icon><Box /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon orders">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalOrders }}</div>
          <div class="stat-label">订单总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon inventory">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalInventory }}</div>
          <div class="stat-label">库存总量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon low-stock">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.lowStockProducts }}</div>
          <div class="stat-label">库存不足</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3>销售趋势</h3>
          <el-select v-model="salesPeriod" size="small" style="width: 120px">
            <el-option label="最近7天" value="7d" />
            <el-option label="最近30天" value="30d" />
            <el-option label="最近90天" value="90d" />
          </el-select>
        </div>
        <div class="chart-content">
          <v-chart :option="salesChartOption" style="height: 300px" />
        </div>
      </div>
      
      <div class="chart-card">
        <div class="chart-header">
          <h3>商品分类分布</h3>
        </div>
        <div class="chart-content">
          <v-chart :option="categoryChartOption" style="height: 300px" />
        </div>
      </div>
    </div>

    <!-- 库存预警 -->
    <div class="alert-section">
      <StockAlertWidget />
    </div>

    <!-- 最近订单 -->
    <div class="recent-orders">
      <div class="page-card">
        <div class="card-header">
          <h3>最近订单</h3>
          <el-button type="primary" size="small" @click="$router.push('/orders')">
            查看全部
          </el-button>
        </div>
        <el-table :data="recentOrders" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="150" />
          <el-table-column prop="customerName" label="客户姓名" width="120" />
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
        </el-table>
      </div>
    </div>
  </AppLayout>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { dashboardAPI, categoryAPI, salesAPI, productAPI } from '@/api'
import StockAlertWidget from '@/components/StockAlertWidget.vue'
import AppLayout from '@/components/AppLayout.vue'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

export default {
  name: 'Dashboard',
  components: {
    VChart,
    StockAlertWidget,
    AppLayout
  },
  setup() {
    const salesPeriod = ref('7d')
    
    const stats = reactive({
      totalProducts: 0,
      totalOrders: 0,
      totalInventory: 0,
      lowStockProducts: 0
    })
    
    const recentOrders = ref([])
    const categoryData = ref([])
    const salesTrendData = ref([])
    
    // 销售趋势图表配置
    const salesChartOption = computed(() => {
      const dates = salesTrendData.value.map(item => item.date)
      const amounts = salesTrendData.value.map(item => item.amount)
      
      return {
        title: {
          text: '销售趋势',
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const data = params[0]
            return `${data.axisValue}<br/>销售额: ¥${data.value}`
          }
        },
        xAxis: {
          type: 'category',
          data: dates.length > 0 ? dates : ['暂无数据']
        },
        yAxis: {
          type: 'value',
          name: '销售额(元)'
        },
        series: [{
          name: '销售额',
          type: 'line',
          data: amounts.length > 0 ? amounts : [0],
          smooth: true,
          itemStyle: { color: '#1890ff' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(24, 144, 255, 0.3)'
              }, {
                offset: 1, color: 'rgba(24, 144, 255, 0.1)'
              }]
            }
          }
        }]
      }
    })
    
    // 分类分布图表配置
    const categoryChartOption = computed(() => ({
      title: {
        text: '商品分类分布',
        left: 'center',
        textStyle: { fontSize: 16 }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [{
        name: '商品分类',
        type: 'pie',
        radius: '50%',
        data: categoryData.value,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }))
    
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
    
    // 加载销售趋势数据
    const loadSalesTrend = async () => {
      try {
        const response = await salesAPI.getTrend(salesPeriod.value)
        if (response.success) {
          salesTrendData.value = response.data || []
        } else {
          console.error('Failed to load sales trend:', response.message)
          salesTrendData.value = []
        }
      } catch (error) {
        console.error('Error loading sales trend:', error)
        salesTrendData.value = []
      }
    }
    
    // 加载数据
    const loadData = async () => {
      try {
        // 并行加载所有数据
        const [statsResponse, recentOrdersResponse, categoryResponse] = await Promise.all([
          dashboardAPI.getStats(),
          dashboardAPI.getRecentOrders(),
          categoryAPI.getList()
        ])
        
        // 处理统计数据
        if (statsResponse.success) {
          const statsData = statsResponse.data
          stats.totalProducts = statsData.totalProducts || 0
          stats.totalOrders = statsData.totalOrders || 0
          stats.totalInventory = statsData.totalInventory || 0
          stats.lowStockProducts = statsData.lowStockProducts || 0
        }
        
        // 处理最近订单
        if (recentOrdersResponse.success) {
          recentOrders.value = recentOrdersResponse.data.map(order => ({
            orderNo: order.orderNo,
            customerName: order.customerName,
            totalAmount: order.totalAmount,
            status: order.status,
            createdTime: order.createdTime
          }))
        }
        
        // 处理分类数据 - 需要获取所有商品来计算分类分布
        const productResponse = await productAPI.getList({ page: 1, size: 1000 }) // 获取所有商品
        const products = productResponse.data.list || []
        const categories = categoryResponse.data || []
        
        const categoryStats = {}
        products.forEach(product => {
          const category = categories.find(cat => cat.id === product.categoryId)
          if (category) {
            const categoryName = category.fullPath || category.name
            categoryStats[categoryName] = (categoryStats[categoryName] || 0) + 1
          }
        })
        
        categoryData.value = Object.entries(categoryStats).map(([name, count]) => ({
          name,
          value: count
        }))
        
        // 加载销售趋势数据
        await loadSalesTrend()
        
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    }
    
    onMounted(() => {
      loadData()
    })
    
    // 监听时间段变化，重新加载销售趋势数据
    watch(salesPeriod, () => {
      loadSalesTrend()
    })
    
    return {
      salesPeriod,
      stats,
      recentOrders,
      salesChartOption,
      categoryChartOption,
      getStatusType,
      getStatusText
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: #fff;
}

.stat-icon.products { background: #1890ff; }
.stat-icon.orders { background: #52c41a; }
.stat-icon.inventory { background: #faad14; }
.stat-icon.low-stock { background: #ff4d4f; }

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.alert-section {
  margin-bottom: 30px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  color: #333;
}

.recent-orders {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-card {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
