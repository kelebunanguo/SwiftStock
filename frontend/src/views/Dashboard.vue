<template>
  <AppLayout>
    <div class="page-header">
      <h2>仪表盘</h2>
      <p>欢迎使用SwiftStock电商仓库管理系统</p>
    </div>

    <!-- 顶部统计区域（新版：左右各 50%） -->
    <el-row :gutter="20" class="top-stats-row" style="margin-bottom:20px; height:450px;">
      <!-- 左侧：3 个横向卡片，均分左侧空间 -->
      <el-col :xs="24" :md="12">
        <div class="left-stack-vertical" style="height:100%; display:flex; flex-direction:column; gap:12px; overflow:hidden;">
          <el-card class="stat-card stacked" shadow="hover" style="flex:1; display:flex; align-items:center; padding:12px;">
            <div class="stat-row" style="width:100%; align-items:center;">
              <div class="stat-icon products"><el-icon><Box /></el-icon></div>
              <div class="stat-body" style="flex-direction:row; align-items:center; gap:12px;">
                <div class="stat-number">{{ stats.totalProducts }}</div>
                <div class="stat-label">商品总数</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card stacked" shadow="hover" style="flex:1; display:flex; align-items:center; padding:12px;">
            <div class="stat-row" style="width:100%; align-items:center;">
              <div class="stat-icon orders"><el-icon><Document /></el-icon></div>
              <div class="stat-body" style="flex-direction:row; align-items:center; gap:12px;">
                <div class="stat-number">{{ stats.totalOrders }}</div>
                <div class="stat-label">订单总数</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card stacked" shadow="hover" style="flex:1; display:flex; align-items:center; padding:12px;">
            <div class="stat-row" style="width:100%; align-items:center;">
              <div class="stat-icon inventory"><el-icon><Grid /></el-icon></div>
              <div class="stat-body" style="flex-direction:row; align-items:center; gap:12px;">
                <div class="stat-number">{{ stats.totalInventory }}</div>
                <div class="stat-label">库存总量</div>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>

      <!-- 右侧：AI 大卡片，占右侧 50% -->
      <el-col :xs="24" :md="12">
        <el-card class="ai-card hero" shadow="hover" @click="gotoAi"style="height:100%; overflow:hidden !important; touch-action: none; cursor: pointer;">          <div class="ai-hero-inner">
            <div class="ai-hero-left">
              <div class="ai-hero-icon"><el-icon><TrendCharts /></el-icon></div>
            </div>

            <div class="ai-hero-right">
              <div class="ai-hero-title">AI智能补货推荐</div>

              <div class="ai-hero-number-wrap">
                <el-icon v-if="aiLoading" class="ai-hero-number-icon"><Loading /></el-icon>
                <span v-else-if="aiReplenishCount !== null" class="ai-hero-number">{{ aiReplenishCount }}</span>
                <span v-else class="ai-hero-number">-</span>
              </div>

              <div class="ai-hero-sub">件商品需补货 · 点击查看详情</div>
            </div>

            <!-- 装饰：半透明图形/波纹（纯装饰，不影响布局） -->
            <div class="ai-hero-decor" aria-hidden="true"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 库存预警 -->
    <div class="alert-section">
      <StockAlertWidget />
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
          <el-table-column prop="orderNo" label="订单号" />
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
import { useRouter } from 'vue-router'
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
import { dashboardAPI, categoryAPI, salesAPI, productAPI, aiAPI } from '@/api'
import StockAlertWidget from '@/components/StockAlertWidget.vue'
import AppLayout from '@/components/AppLayout.vue'
import { Check, Loading } from '@element-plus/icons-vue'

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
    
    const aiReplenishCount = ref(null)
    const aiLoading = ref(false)
    const aiSuccess = ref(false)

    const loadAiReplenish = async () => {
      aiLoading.value = true
      aiSuccess.value = false
      try {
        // pass silent:true to avoid global error messages for this request
        const res = await aiAPI.getReplenishmentCount({ days: 7 }, { silent: true })
        // 调试日志，便于前端排查返回结构
        console.debug('loadAiReplenish response ->', res)

        // 兼容多种返回结构：直接数值、{data: num}、{success,data:num}、{code,data}
        let value = null
        if (res === null || res === undefined) {
          value = null
        } else if (typeof res === 'number') {
          value = res
        } else if (typeof res === 'object') {
          if (typeof res.data === 'number') {
            value = res.data
          } else if (res.data && typeof res.data.count === 'number') {
            value = res.data.count
          } else if (typeof res.success === 'boolean' && typeof res.data === 'number') {
            value = res.data
          } else if (typeof res.code === 'number' && typeof res.data === 'number') {
            value = res.data
          }
        }

        if (value !== null) {
          aiReplenishCount.value = Number(value)
          aiSuccess.value = true
        } else {
          aiSuccess.value = false
        }
      } catch (e) {
        console.debug('AI 补货接口未就绪，使用回退值', e)
        aiSuccess.value = false
      } finally {
        aiLoading.value = false
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
        // 加载 AI 补货建议
        await loadAiReplenish()
        
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    }
    
    onMounted(() => {
      // 并行：避免某些接口失败（如 product/category）导致 AI 补货计数无法加载
      loadData()
      loadAiReplenish()
    })

    const router = useRouter()
    const gotoAi = () => {
      router.push('/ai/forecast')
    }

    // prevent middle-click autoscroll on AI card
    const handleAiMouseDown = (e) => {
      try {
        if (e && e.button === 1) {
          e.preventDefault()
          e.stopPropagation()
        }
      } catch (err) {
        // ignore
      }
    }
    
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
      getStatusText,
      aiReplenishCount,
      aiLoading,
      aiSuccess,
      handleAiMouseDown,
      gotoAi
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

/* AI 卡片样式，确保白色字体在深色背景上可见 */
.ai-card {
  background: linear-gradient(90deg, #1890ff);
  color: #fff;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
}

/* AI 卡片增强样式 */
.ai-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.ai-icon-wrapper {
  flex: 0 0 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.08);
  border-radius: 12px;
}
.ai-icon {
  font-size: 36px;
  color: #fff;
}
.ai-text {
  flex: 1;
}
.ai-title {
  font-size: 30px;
  color: rgba(255,255,255,0.95);
  margin-bottom: 6px;
}
.ai-number {
  font-size: 50px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
}
.ai-sub {
  font-size: 12px;
  color: rgba(255,255,255,0.85);
  margin-top: 6px;
}

/* spinner & success icon */
.ai-number-wrap { display:flex; align-items:center; gap:10px; }
.ai-spinner {
  width:18px;
  height:18px;
  border-radius:50%;
  border:2px solid rgba(255,255,255,0.2);
  border-top-color: #fff;
  animation: spin 1s linear infinite;
  display:inline-block;
}
.ai-success { color:#fff; font-size:18px; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Layout for redesigned top */
.top-row { display:flex; align-items:stretch; }
.left-stack { display:flex; flex-direction:column; gap:16px; height:100%; }
.left-stack .stat-card.stacked { flex:1; display:flex; align-items:center; padding:18px; }
.ai-card.large { height:100%; display:flex; align-items:stretch; padding:0; overflow:hidden; }
.ai-large-inner { display:flex; align-items:center; height:100%; padding:28px; gap:24px; }
.ai-left { flex:0 0 160px; display:flex; align-items:center; justify-content:center; }
.ai-icon-large { width:120px; height:120px; border-radius:12px; background: rgba(255,255,255,0.08); display:flex; align-items:center; justify-content:center; font-size:48px; color:#fff; }
.ai-right { flex:1; display:flex; flex-direction:column; justify-content:center; align-items:flex-start; padding-right:24px; }
.ai-title-large { font-size:20px; color:rgba(255,255,255,0.95); margin-bottom:8px; font-weight:600; }
.ai-number-large-wrap { display:flex; align-items:center; gap:12px; margin-bottom:8px; }
.ai-number-large { font-size:64px; font-weight:800; color:#fff; line-height:1; }

@media (max-width: 768px) {
  .ai-left { flex:0 0 72px; }
  .ai-icon-large { width:56px; height:56px; font-size:22px; }
  .ai-number-large { font-size:36px; }
  .left-stack .stat-card.stacked { padding:12px; }
  .ai-large-inner { padding:16px; gap:12px; }
}

/* New top-stats styles */
.top-stats-row { display:flex; align-items:stretch; }
.left-stats { height:100%; }
.stat-card.horizontal { display:flex; align-items:center; justify-content:flex-start; border-radius:12px; padding:18px; box-shadow:0 6px 20px rgba(0,0,0,0.06); }
.stat-row { display:flex; align-items:center; gap:16px; width:100%; }
.stat-icon { width:64px; height:64px; border-radius:10px; display:flex; align-items:center; justify-content:center; font-size:28px; color:#fff; }
.stat-icon.products { background:#1890ff; }
.stat-icon.orders { background:#52c41a; }
.stat-icon.inventory { background:#faad14; }
.stat-body { display:flex; flex-direction:row; justify-content:center; align-items:center; flex:1; gap:8px; }
.stat-number { font-size:24px; font-weight:700; color:#111; }
.stat-label { font-size:13px; color:#666; margin:0; }

.ai-card.hero { border-radius:14px; overflow:hidden; background:linear-gradient(135deg,#409EFF 0%, #79bbff 100%); color:#fff; padding:0; }
.ai-hero-inner { display:flex; align-items:center; height:100%; position:relative; padding:12px 12px; gap:12px; box-sizing:border-box; }
.ai-hero-left { flex:0 0 auto; display:flex; align-items:center; justify-content:center; }
.ai-hero-icon { width:50%; height:50%; max-width:220px; max-height:220px; border-radius:12px; background:rgba(255,255,255,0.12); display:flex; align-items:center; justify-content:center; font-size:clamp(28px,6vw,64px); color:#fff; }
.ai-hero-right { flex:1; display:flex; flex-direction:column; justify-content:center; align-items:flex-start; }
.ai-hero-title { font-size:20px; font-weight:700; color:rgba(255,255,255,0.98); margin-bottom:12px; }
.ai-hero-number-wrap { display:flex; align-items:center; gap:16px; margin-bottom:8px; }
.ai-hero-number { font-size:clamp(28px,6vw,96px); font-weight:900; color:#fff; line-height:1; white-space:nowrap; }
.ai-hero-sub { color:rgba(255,255,255,0.9); font-size:14px; }
.ai-hero-decor { position:absolute; right:-40px; bottom:-40px; width:280px; height:280px; background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.06), rgba(255,255,255,0.02) 40%, transparent 60%); transform:rotate(20deg); pointer-events:none; }

/* hide any internal scrollbars inside AI card */
.ai-card.hero, .ai-card.hero * {
  scrollbar-width: none; /* Firefox */
}
.ai-card.hero::-webkit-scrollbar { display: none; }

@media (max-width: 992px) {
  .top-stats-row { height:auto !important; }
  .ai-hero-inner { padding:20px; }
  .ai-hero-left { flex:0 0 72px; }
  .ai-hero-icon { width:56px; height:56px; font-size:22px; border-radius:10px; }
  .ai-hero-number { font-size:36px; }
  .stat-icon { width:48px; height:48px; font-size:20px; }
  .stat-number { font-size:20px; }
}

/* Left stack overflow hide to prevent scrollbars */
.left-stack-vertical { overflow: hidden; }
.left-stack-vertical .el-card { overflow: hidden; }
.left-stack-vertical::-webkit-scrollbar { display: none; }
.left-stack-vertical { scrollbar-width: none; }
.ai-hero-number-icon { font-size: clamp(28px,6vw,96px); display:inline-flex; align-items:center; justify-content:center; color: #fff; }
</style>
