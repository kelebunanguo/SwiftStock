<template>
  <AppLayout>
    <div class="page-header">
      <h2>商品管理</h2>
      <p>管理商品信息、库存和分类</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-cascader
            v-model="searchForm.categoryId"
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
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 商品表格 -->
    <div class="table-container">
      <el-table
        :data="products"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column label="序号" width="70">
          <template #default="scope">
            {{ (pagination.page - 1) * pagination.size + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="140" />
        <el-table-column prop="code" label="商品编码" width="100" />
        <el-table-column label="分类" width="150">
          <template #default="scope">
            {{ scope.row.category?.fullPath || scope.row.category?.name || '未分类' }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="90">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="70" />
        <el-table-column prop="minStockLevel" label="最低库存" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { productAPI, categoryAPI } from '@/api'
import AppLayout from '@/components/AppLayout.vue'

export default {
  name: 'ProductList',
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
      name: '',
      categoryId: null
    })
    
    const pagination = reactive({
      page: 1,
      size: 10,
      total: 0
    })
    
    const products = ref([])
    const categories = ref([])
    
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
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    
    // 加载商品列表
    const loadProducts = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.page,
          size: pagination.size,
          name: searchForm.name,
          categoryId: searchForm.categoryId
        }
        
        const response = await productAPI.getList(params)
        products.value = response.data.list || []
        pagination.total = response.data.total || 0
      } catch (error) {
        console.error('加载商品列表失败:', error)
        ElMessage.error('加载商品列表失败')
      } finally {
        loading.value = false
      }
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
      searchForm.categoryId = value
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.page = 1
      loadProducts()
    }
    
    // 重置
    const handleReset = () => {
      searchForm.name = ''
      searchForm.categoryId = null
      pagination.page = 1
      loadProducts()
    }
    
    // 添加商品
    const handleAdd = () => {
      router.push('/products/add')
    }
    
    // 编辑商品
    const handleEdit = (row) => {
      router.push(`/products/edit/${row.id}`)
    }
    
    // 删除商品
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除商品"${row.name}"吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await productAPI.delete(row.id)
        ElMessage.success('删除成功')
        loadProducts()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除商品失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.page = 1
      loadProducts()
    }
    
    // 当前页变化
    const handleCurrentChange = (page) => {
      pagination.page = page
      loadProducts()
    }
    
    onMounted(() => {
      loadProducts()
      loadCategories()
    })
    
    return {
      loading,
      searchForm,
      pagination,
      products,
      categories,
      cascaderProps,
      categoryOptions,
      formatDate,
      handleCategoryChange,
      handleSearch,
      handleReset,
      handleAdd,
      handleEdit,
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

@media (max-width: 768px) {
  .button-group {
    flex-direction: column;
  }
  
  .search-form .el-form {
    flex-direction: column;
  }
}
</style>
