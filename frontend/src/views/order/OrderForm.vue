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
                <h2>创建订单</h2>
                <p>创建新的客户订单</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单表单 -->
        <div class="form-container">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="120px"
            class="order-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="订单编号" prop="orderNo">
                  <el-input
                    v-model="form.orderNo"
                    placeholder="系统自动生成"
                    disabled
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户姓名" prop="customerName">
                  <el-input
                    v-model="form.customerName"
                    placeholder="请输入客户姓名"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系电话" prop="customerPhone">
                  <el-input
                    v-model="form.customerPhone"
                    placeholder="请输入联系电话"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="订单状态" prop="status">
                  <el-select v-model="form.status" placeholder="请选择状态">
                    <el-option label="已付款" value="PAID" />
                    <el-option label="待付款" value="UNPAID" />
                    <el-option label="已取消" value="CANCELLED" />
                    <el-option label="已完成" value="COMPLETED" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入订单备注"
              />
            </el-form-item>

            <!-- 商品选择区域 -->
            <div class="product-section">
              <div class="section-header">
                <h3>选择商品</h3>
                <el-button type="primary" @click="showProductDialog = true">
                  <el-icon><Plus /></el-icon>
                  添加商品
                </el-button>
              </div>

              <el-table :data="form.items" style="width: 100%" class="items-table">
                <el-table-column prop="productName" label="商品名称" />
                <el-table-column prop="quantity" label="数量" width="120">
                  <template #default="scope">
                    <el-input-number
                      v-model="scope.row.quantity"
                      :min="1"
                      :max="scope.row.stockQuantity"
                      @change="updateItemAmount(scope.$index)"
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="price" label="单价" width="120">
                  <template #default="scope">
                    ¥{{ scope.row.price }}
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额" width="120">
                  <template #default="scope">
                    ¥{{ scope.row.amount }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button
                      type="danger"
                      size="small"
                      @click="removeItem(scope.$index)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="total-section">
                <div class="total-info">
                  <span class="total-label">订单总金额：</span>
                  <span class="total-amount">¥{{ totalAmount }}</span>
                </div>
              </div>
            </div>

            <el-form-item class="form-actions">
              <el-button @click="handleCancel">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="loading">
                创建订单
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </main>
    </div>

    <!-- 商品选择对话框 -->
    <el-dialog
      v-model="showProductDialog"
      title="选择商品"
      width="800px"
      :before-close="handleDialogClose"
    >
      <div class="product-dialog">
        <el-input
          v-model="productSearchKeyword"
          placeholder="搜索商品名称"
          @input="searchProducts"
          style="margin-bottom: 20px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-table
          :data="filteredProducts"
          @selection-change="handleProductSelection"
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="code" label="商品编码" width="120" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="stockQuantity" label="库存" width="80" />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="showProductDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmProductSelection">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { orderAPI, productAPI } from '@/api'

export default {
  name: 'OrderForm',
  setup() {
    const router = useRouter()
    const activeMenu = ref('/orders')
    const loading = ref(false)
    const formRef = ref()
    const showProductDialog = ref(false)
    const productSearchKeyword = ref('')
    const selectedProducts = ref([])
    const products = ref([])

    const form = reactive({
      orderNo: '',
      customerName: '',
      customerPhone: '',
      status: 'UNPAID',
      remark: '',
      items: []
    })

    const rules = {
      customerName: [
        { required: true, message: '请输入客户姓名', trigger: 'blur' }
      ],
      customerPhone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择订单状态', trigger: 'change' }
      ]
    }

    // 计算总金额
    const totalAmount = computed(() => {
      return form.items.reduce((total, item) => total + item.amount, 0)
    })

    // 过滤后的商品列表
    const filteredProducts = computed(() => {
      if (!productSearchKeyword.value) {
        return products.value
      }
      return products.value.filter(product =>
        product.name.toLowerCase().includes(productSearchKeyword.value.toLowerCase())
      )
    })

    // 生成订单编号
    const generateOrderNo = () => {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const time = String(now.getTime()).slice(-6)
      form.orderNo = `ORD${year}${month}${day}${time}`
    }

    // 更新商品金额
    const updateItemAmount = (index) => {
      const item = form.items[index]
      item.amount = item.quantity * item.price
    }

    // 移除商品
    const removeItem = (index) => {
      form.items.splice(index, 1)
    }

    // 搜索商品
    const searchProducts = () => {
      // 搜索逻辑在computed中处理
    }

    // 处理商品选择
    const handleProductSelection = (selection) => {
      selectedProducts.value = selection
    }

    // 确认商品选择
    const confirmProductSelection = () => {
      selectedProducts.value.forEach(product => {
        // 检查是否已经添加过
        const existingIndex = form.items.findIndex(item => item.productId === product.id)
        if (existingIndex === -1) {
          form.items.push({
            productId: product.id,
            productName: product.name,
            quantity: 1,
            price: product.price,
            amount: product.price,
            stockQuantity: product.stockQuantity
          })
        }
      })
      showProductDialog.value = false
      selectedProducts.value = []
    }

    // 关闭对话框
    const handleDialogClose = () => {
      showProductDialog.value = false
      selectedProducts.value = []
    }

    // 加载商品列表
    const loadProducts = async () => {
      try {
        // 使用专门的可用商品接口，获取所有有库存的商品
        const response = await productAPI.getAvailable()
        products.value = response.data.list || []
        console.log('加载的可用商品列表:', products.value)
      } catch (error) {
        console.error('加载商品列表失败:', error)
        ElMessage.error('加载商品列表失败')
      }
    }

    // 提交表单
    const handleSubmit = async () => {
      if (!formRef.value) return

      try {
        await formRef.value.validate()
        
        if (form.items.length === 0) {
          ElMessage.error('请至少添加一个商品')
          return
        }

        loading.value = true

        const orderData = {
          ...form,
          totalAmount: totalAmount.value
        }

        await orderAPI.create(orderData)
        ElMessage.success('订单创建成功')
        router.push('/orders')
      } catch (error) {
        console.error('创建订单失败:', error)
        ElMessage.error('创建订单失败')
      } finally {
        loading.value = false
      }
    }

    // 取消
    const handleCancel = () => {
      router.push('/orders')
    }

    // 返回
    const handleBack = () => {
      router.push('/orders')
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
      generateOrderNo()
      loadProducts()
    })

    return {
      activeMenu,
      loading,
      formRef,
      showProductDialog,
      productSearchKeyword,
      selectedProducts,
      products,
      form,
      rules,
      totalAmount,
      filteredProducts,
      updateItemAmount,
      removeItem,
      searchProducts,
      handleProductSelection,
      confirmProductSelection,
      handleDialogClose,
      handleSubmit,
      handleCancel,
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

.form-container {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-form {
  max-width: 800px;
}

.product-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.items-table {
  margin-bottom: 20px;
}

.total-section {
  text-align: right;
  padding: 20px 0;
  border-top: 1px solid #eee;
}

.total-info {
  font-size: 18px;
  font-weight: bold;
}

.total-label {
  color: #666;
}

.total-amount {
  color: #1890ff;
  margin-left: 10px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions .el-button {
  margin: 0 10px;
}

.product-dialog {
  max-height: 400px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .form-container {
    padding: 20px;
  }
  
  .order-form {
    max-width: 100%;
  }
}
</style>
