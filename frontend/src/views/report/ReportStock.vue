<template>
  <AppLayout>
    <div class="page-header">
      <h2>库存报表</h2>
      <p>查看库存统计和分析数据</p>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-form">
      <el-form :model="filterForm" inline>
        <el-form-item label="商品分类">
          <el-cascader
            v-model="filterForm.categoryId"
            :options="categoryOptions"
            :props="cascaderProps"
            placeholder="请选择分类"
            clearable
            filterable
            style="width: 200px"
            @change="handleCategoryChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Box /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon normal">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.normalStock }}</div>
          <div class="stat-label">库存正常</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon low">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.lowStock }}</div>
          <div class="stat-label">库存不足</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon out">
          <el-icon><Close /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.outOfStock }}</div>
          <div class="stat-label">缺货商品</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3>库存状态分布</h3>
        </div>
        <div class="chart-content">
          <v-chart :option="stockStatusOption" style="height: 300px" />
        </div>
      </div>
      
      <div class="chart-card">
        <div class="chart-header">
          <h3>分类库存统计</h3>
        </div>
        <div class="chart-content">
          <v-chart :option="categoryStockOption" style="height: 300px" />
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Box, Check, Warning, Close } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { productAPI, categoryAPI } from '@/api'
import AppLayout from '@/components/AppLayout.vue'

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

export default {
  name: 'ReportStock',
  components: {
    VChart,
    Search,
    Refresh,
    Box,
    Check,
    Warning,
    Close,
    AppLayout
  },
  setup() {
    const loading = ref(false)
    const categories = ref([])
    const products = ref([])
    
    const filterForm = reactive({
      categoryId: null
    })
    
    const stats = reactive({
      totalProducts: 0,
      normalStock: 0,
      lowStock: 0,
      outOfStock: 0
    })
    
    // 级联选择器配置
    const cascaderProps = {
      value: 'id',
      label: 'name',
      children: 'children',
      emitPath: false
    }
    
    // 构建级联选择器选项
    const categoryOptions = computed(() => {
      const buildTree = (categories, parentId = null) => {
        return categories
          .filter(cat => cat.parentId === parentId)
          .map(cat => ({
            ...cat,
            children: buildTree(categories, cat.id)
          }))
      }
      return buildTree(categories.value)
    })
    
    // 库存状态分布图表配置
    const stockStatusOption = computed(() => ({
      title: {
        text: '库存状态分布',
        left: 'center',
        textStyle: { fontSize: 16 }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [{
        name: '库存状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: stats.normalStock, name: '库存正常', itemStyle: { color: '#52c41a' } },
          { value: stats.lowStock, name: '库存不足', itemStyle: { color: '#faad14' } },
          { value: stats.outOfStock, name: '缺货', itemStyle: { color: '#ff4d4f' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }))
    
    // 分类库存统计图表配置 - 基于实际数据
    const categoryStockOption = computed(() => {
      // 计算各分类的库存统计
      const categoryStats = {}
      
      products.value.forEach(product => {
        const category = categories.value.find(cat => cat.id === product.categoryId)
        if (category) {
          const categoryName = category.fullPath || category.name
          if (!categoryStats[categoryName]) {
            categoryStats[categoryName] = {
              totalStock: 0,
              productCount: 0,
              normalCount: 0,
              lowCount: 0,
              outCount: 0
            }
          }
          
          categoryStats[categoryName].totalStock += product.stockQuantity || 0
          categoryStats[categoryName].productCount += 1
          
          if (product.stockQuantity === 0) {
            categoryStats[categoryName].outCount += 1
          } else if (product.stockQuantity <= product.minStockLevel) {
            categoryStats[categoryName].lowCount += 1
          } else {
            categoryStats[categoryName].normalCount += 1
          }
        }
      })
      
      const categoryNames = Object.keys(categoryStats)
      const stockValues = Object.values(categoryStats).map(stat => stat.totalStock)
      
      return {
        title: {
          text: '分类库存统计',
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            const dataIndex = params[0].dataIndex
            const categoryName = categoryNames[dataIndex]
            const stat = categoryStats[categoryName]
            return `${categoryName}<br/>
                    总库存: ${stat.totalStock}<br/>
                    商品数: ${stat.productCount}<br/>
                    正常: ${stat.normalCount} | 不足: ${stat.lowCount} | 缺货: ${stat.outCount}`
          }
        },
        xAxis: {
          type: 'category',
          data: categoryNames.length > 0 ? categoryNames : ['暂无数据'],
          axisLabel: {
            rotate: 45,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '库存数量'
        },
        series: [{
          name: '库存数量',
          type: 'bar',
          data: stockValues.length > 0 ? stockValues : [0],
          itemStyle: { 
            color: function(params) {
              const dataIndex = params.dataIndex
              if (categoryNames.length === 0) return '#ccc'
              const stat = categoryStats[categoryNames[dataIndex]]
              if (stat.outCount > 0) return '#ff4d4f'
              if (stat.lowCount > 0) return '#faad14'
              return '#52c41a'
            }
          }
        }]
      }
    })
    
    // 加载商品数据
    const loadProducts = async () => {
      try {
        const params = {
          page: 1,
          size: 1000, // 获取所有商品
          categoryId: filterForm.categoryId
        }
        
        const response = await productAPI.getList(params)
        products.value = response.data.list || []
        
        // 计算统计数据
        calculateStats()
      } catch (error) {
        console.error('加载商品数据失败:', error)
        ElMessage.error('加载商品数据失败')
      }
    }
    
    // 计算统计数据
    const calculateStats = () => {
      stats.totalProducts = products.value.length
      stats.normalStock = 0
      stats.lowStock = 0
      stats.outOfStock = 0
      
      products.value.forEach(product => {
        const stock = product.stockQuantity || 0
        const minStock = product.minStockLevel || 0
        
        if (stock === 0) {
          stats.outOfStock++
        } else if (stock <= minStock) {
          stats.lowStock++
        } else {
          stats.normalStock++
        }
      })
    }
    
    // 加载分类列表
    const loadCategories = async () => {
      try {
        const response = await categoryAPI.getList()
        categories.value = response.data || []
      } catch (error) {
        console.error('加载分类列表失败:', error)
      }
    }
    
    // 处理分类变化
    const handleCategoryChange = (value) => {
      console.log('分类选择变化:', value)
      filterForm.categoryId = value
    }
    
    // 搜索
    const handleSearch = () => {
      loadProducts()
    }
    
    // 重置
    const handleReset = () => {
      filterForm.categoryId = null
      loadProducts()
    }
    
    onMounted(() => {
      loadCategories()
      loadProducts()
    })
    
    return {
      loading,
      categories,
      products,
      filterForm,
      stats,
      cascaderProps,
      categoryOptions,
      stockStatusOption,
      categoryStockOption,
      handleCategoryChange,
      handleSearch,
      handleReset
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

.filter-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.stat-icon.total { background: #1890ff; }
.stat-icon.normal { background: #52c41a; }
.stat-icon.low { background: #faad14; }
.stat-icon.out { background: #ff4d4f; }

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
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-header h3 {
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
  
  .filter-form .el-form {
    flex-direction: column;
  }
}
</style>