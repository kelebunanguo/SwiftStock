<template>
  <AppLayout>
    <div class="page-header">
      <h2>库存管理</h2>
      <p>管理商品库存和出入库记录</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.productName"
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
        <el-form-item label="库存状态">
          <el-select
            v-model="searchForm.stockStatus"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="正常" value="normal" />
            <el-option label="库存不足" value="low" />
            <el-option label="缺货" value="out" />
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
      <el-button type="success" @click="handleBatchStockIn">
        <el-icon><Plus /></el-icon>
        批量入库
      </el-button>
      <el-button type="warning" @click="handleBatchStockOut">
        <el-icon><Minus /></el-icon>
        批量出库
      </el-button>
    </div>

    <!-- 库存表格 -->
    <div class="table-container">
      <el-table
        :data="inventoryList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="code" label="商品编码" width="120" />
        <el-table-column label="分类" width="150">
          <template #default="scope">
            {{ scope.row.category?.fullPath || scope.row.category?.name || '未分类' }}
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="当前库存" width="100">
          <template #default="scope">
            <span :class="getStockClass(scope.row)">
              {{ scope.row.stockQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStockLevel" label="最低库存" width="100" />
        <el-table-column prop="supplier" label="供应商" width="120" />
        <el-table-column label="库存状态" width="100">
          <template #default="scope">
            <el-tag :type="getStockStatusType(scope.row)">
              {{ getStockStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click="handleViewRecords(scope.row)"
              >
                查看记录
              </el-button>
              <el-dropdown @command="(command) => handleStockOperation(scope.row, command)">
                <el-button type="success" size="small">
                  入库<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="purchase">采购入库</el-dropdown-item>
                    <el-dropdown-item command="return">退货入库</el-dropdown-item>
                    <el-dropdown-item command="transfer_in">调拨入库</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-dropdown @command="(command) => handleStockOperation(scope.row, command)">
                <el-button type="warning" size="small">
                  出库<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="sales">销售出库</el-dropdown-item>
                    <el-dropdown-item command="transfer_out">调拨出库</el-dropdown-item>
                    <el-dropdown-item command="damage">报损出库</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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

    <!-- 出入库对话框 -->
    <el-dialog
      v-model="stockDialogVisible"
      :title="stockDialogTitle"
      width="600px"
      @close="handleStockDialogClose"
    >
      <el-form
        ref="stockFormRef"
        :model="stockForm"
        :rules="stockRules"
        label-width="120px"
      >
        <el-form-item label="商品名称">
          <el-input v-model="stockForm.productName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input v-model="stockForm.currentStock" disabled />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="stockForm.operationTypeText" disabled />
        </el-form-item>
        <el-form-item label="操作数量" prop="quantity">
          <el-input-number
            v-model="stockForm.quantity"
            :min="1"
            :max="stockForm.maxQuantity"
            placeholder="请输入数量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="操作原因" prop="reason">
          <el-input
            v-model="stockForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入操作原因"
          />
        </el-form-item>
        <el-form-item v-if="stockForm.operationType === 'transfer_out'" label="调拨目标">
          <el-input
            v-model="stockForm.transferTarget"
            placeholder="请输入调拨目标仓库"
          />
        </el-form-item>
        <el-form-item v-if="stockForm.operationType === 'transfer_in'" label="调拨来源">
          <el-input
            v-model="stockForm.transferSource"
            placeholder="请输入调拨来源仓库"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit" :loading="stockLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量操作对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      :title="batchDialogTitle"
      width="800px"
      @close="handleBatchDialogClose"
    >
      <div class="batch-operation">
        <div class="selected-items">
          <h4>已选择商品 ({{ selectedItems.length }}个)</h4>
          <el-tag
            v-for="item in selectedItems"
            :key="item.id"
            closable
            @close="removeSelectedItem(item)"
            style="margin: 4px"
          >
            {{ item.name }} (库存: {{ item.stockQuantity }})
          </el-tag>
        </div>
        <el-form
          ref="batchFormRef"
          :model="batchForm"
          :rules="batchRules"
          label-width="120px"
        >
          <el-form-item label="操作类型">
            <el-input v-model="batchForm.operationTypeText" disabled />
          </el-form-item>
          <el-form-item label="操作数量" prop="quantity">
            <el-input-number
              v-model="batchForm.quantity"
              :min="1"
              placeholder="请输入数量"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="操作原因" prop="reason">
            <el-input
              v-model="batchForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入操作原因"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="batchLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

  </AppLayout>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Plus, 
  Minus, 
  List, 
  ArrowDown 
} from '@element-plus/icons-vue'
import { inventoryAPI, categoryAPI } from '@/api'
import AppLayout from '@/components/AppLayout.vue'

export default {
  name: 'InventoryList',
  components: {
    Search,
    Refresh,
    Plus,
    Minus,
    List,
    ArrowDown,
    AppLayout
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const stockDialogVisible = ref(false)
    const batchDialogVisible = ref(false)
    const stockLoading = ref(false)
    const batchLoading = ref(false)
    const stockFormRef = ref()
    const batchFormRef = ref()
    const categories = ref([])
    const selectedItems = ref([])
    
    const searchForm = reactive({
      productName: '',
      categoryId: null,
      stockStatus: ''
    })
    
    const pagination = reactive({
      page: 1,
      size: 10,
      total: 0
    })
    
    const inventoryList = ref([])
    
    // 操作类型映射
    const operationTypeMap = {
      'purchase': '采购入库',
      'return': '退货入库',
      'transfer_in': '调拨入库',
      'sales': '销售出库',
      'transfer_out': '调拨出库',
      'damage': '报损出库'
    }
    
    const stockDialogType = ref('in')
    const stockDialogTitle = computed(() => {
      const type = stockForm.operationType
      return operationTypeMap[type] || '库存操作'
    })
    
    const batchDialogTitle = computed(() => {
      const type = batchForm.operationType
      return `批量${operationTypeMap[type] || '操作'}`
    })
    
    const stockForm = reactive({
      productId: null,
      productName: '',
      currentStock: 0,
      quantity: 1,
      reason: '',
      operationType: '',
      operationTypeText: '',
      maxQuantity: 9999,
      transferTarget: '',
      transferSource: ''
    })
    
    const batchForm = reactive({
      operationType: '',
      operationTypeText: '',
      quantity: 1,
      reason: ''
    })
    
    const stockRules = {
      quantity: [
        { required: true, message: '请输入操作数量', trigger: 'blur' },
        { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
      ],
      reason: [
        { required: true, message: '请输入操作原因', trigger: 'blur' }
      ]
    }
    
    const batchRules = {
      quantity: [
        { required: true, message: '请输入操作数量', trigger: 'blur' },
        { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
      ],
      reason: [
        { required: true, message: '请输入操作原因', trigger: 'blur' }
      ]
    }
    
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
    
    // 获取库存状态类型
    const getStockStatusType = (row) => {
      if (row.stockQuantity === 0) return 'danger'
      if (row.stockQuantity <= row.minStockLevel) return 'warning'
      return 'success'
    }
    
    // 获取库存状态文本
    const getStockStatusText = (row) => {
      if (row.stockQuantity === 0) return '缺货'
      if (row.stockQuantity <= row.minStockLevel) return '库存不足'
      return '正常'
    }
    
    // 获取库存样式类
    const getStockClass = (row) => {
      if (row.stockQuantity === 0) return 'stock-out'
      if (row.stockQuantity <= row.minStockLevel) return 'stock-low'
      return 'stock-normal'
    }
    
    // 加载库存列表
    const loadInventoryList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.page,
          size: pagination.size,
          ...searchForm
        }
        
        const response = await inventoryAPI.getList(params)
        inventoryList.value = response.data.list || []
        pagination.total = response.data.total || 0
      } catch (error) {
        console.error('加载库存列表失败:', error)
        ElMessage.error('加载库存列表失败')
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
      loadInventoryList()
    }
    
    // 重置
    const handleReset = () => {
      searchForm.productName = ''
      searchForm.categoryId = null
      searchForm.stockStatus = ''
      pagination.page = 1
      loadInventoryList()
    }
    
    // 查看记录
    const handleViewRecords = (row) => {
      router.push(`/inventory/records/${row.id}`)
    }
    
    // 库存操作
    const handleStockOperation = (row, operationType) => {
      stockForm.productId = row.id
      stockForm.productName = row.name
      stockForm.currentStock = row.stockQuantity
      stockForm.quantity = 1
      stockForm.reason = ''
      stockForm.operationType = operationType
      stockForm.operationTypeText = operationTypeMap[operationType]
      stockForm.transferTarget = ''
      stockForm.transferSource = ''
      
      // 设置最大数量限制
      if (['sales', 'transfer_out', 'damage'].includes(operationType)) {
        stockForm.maxQuantity = row.stockQuantity
      } else {
        stockForm.maxQuantity = 9999
      }
      
      stockDialogVisible.value = true
    }
    
    // 批量入库
    const handleBatchStockIn = () => {
      if (selectedItems.value.length === 0) {
        ElMessage.warning('请先选择要操作的商品')
        return
      }
      batchForm.operationType = 'purchase'
      batchForm.operationTypeText = '采购入库'
      batchForm.quantity = 1
      batchForm.reason = ''
      batchDialogVisible.value = true
    }
    
    // 批量出库
    const handleBatchStockOut = () => {
      if (selectedItems.value.length === 0) {
        ElMessage.warning('请先选择要操作的商品')
        return
      }
      batchForm.operationType = 'sales'
      batchForm.operationTypeText = '销售出库'
      batchForm.quantity = 1
      batchForm.reason = ''
      batchDialogVisible.value = true
    }
    
    // 选择变化
    const handleSelectionChange = (selection) => {
      selectedItems.value = selection
    }
    
    // 移除选中项
    const removeSelectedItem = (item) => {
      const index = selectedItems.value.findIndex(selected => selected.id === item.id)
      if (index > -1) {
        selectedItems.value.splice(index, 1)
      }
    }
    
    // 提交出入库
    const handleStockSubmit = async () => {
      if (!stockFormRef.value) return
      
      try {
        await stockFormRef.value.validate()
        stockLoading.value = true
        
        const params = {
          productId: stockForm.productId,
          quantity: stockForm.quantity,
          reason: stockForm.reason,
          operationType: stockForm.operationType,
          transferTarget: stockForm.transferTarget,
          transferSource: stockForm.transferSource
        }
        
        await inventoryAPI.stockOperation(params)
        ElMessage.success(`${stockForm.operationTypeText}成功`)
        
        stockDialogVisible.value = false
        loadInventoryList()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败，请检查表单信息')
      } finally {
        stockLoading.value = false
      }
    }
    
    // 提交批量操作
    const handleBatchSubmit = async () => {
      if (!batchFormRef.value) return
      
      try {
        await batchFormRef.value.validate()
        batchLoading.value = true
        
        const params = {
          productIds: selectedItems.value.map(item => item.id),
          quantity: batchForm.quantity,
          reason: batchForm.reason,
          operationType: batchForm.operationType
        }
        
        await inventoryAPI.batchStockOperation(params)
        ElMessage.success(`批量${batchForm.operationTypeText}成功`)
        
        batchDialogVisible.value = false
        selectedItems.value = []
        loadInventoryList()
      } catch (error) {
        console.error('批量操作失败:', error)
        ElMessage.error('批量操作失败，请检查表单信息')
      } finally {
        batchLoading.value = false
      }
    }
    
    // 关闭对话框
    const handleStockDialogClose = () => {
      stockFormRef.value?.resetFields()
    }
    
    const handleBatchDialogClose = () => {
      batchFormRef.value?.resetFields()
    }
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.page = 1
      loadInventoryList()
    }
    
    // 当前页变化
    const handleCurrentChange = (page) => {
      pagination.page = page
      loadInventoryList()
    }
    
    onMounted(() => {
      loadInventoryList()
      loadCategories()
    })
    
    return {
      loading,
      stockDialogVisible,
      batchDialogVisible,
      stockLoading,
      batchLoading,
      stockFormRef,
      batchFormRef,
      categories,
      selectedItems,
      searchForm,
      pagination,
      inventoryList,
      stockDialogType,
      stockDialogTitle,
      batchDialogTitle,
      stockForm,
      batchForm,
      stockRules,
      batchRules,
      cascaderProps,
      categoryOptions,
      getStockStatusType,
      getStockStatusText,
      getStockClass,
      handleCategoryChange,
      handleSearch,
      handleReset,
      handleViewRecords,
      handleStockOperation,
      handleBatchStockIn,
      handleBatchStockOut,
      handleSelectionChange,
      removeSelectedItem,
      handleStockSubmit,
      handleBatchSubmit,
      handleStockDialogClose,
      handleBatchDialogClose,
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

.batch-operation {
  margin-bottom: 20px;
}

.selected-items {
  margin-bottom: 20px;
}

.selected-items h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.stock-normal {
  color: #52c41a;
  font-weight: bold;
}

.stock-low {
  color: #faad14;
  font-weight: bold;
}

.stock-out {
  color: #ff4d4f;
  font-weight: bold;
}

@media (max-width: 768px) {
  .search-form .el-form {
    flex-direction: column;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
