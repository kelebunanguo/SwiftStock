<template>
  <AppLayout>
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="handleBack" class="back-button">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div class="title-section">
            <h2>{{ isEdit ? '编辑商品' : '添加商品' }}</h2>
            <p>{{ isEdit ? '修改商品信息' : '创建新的商品' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品表单 -->
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="product-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入商品名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编码" prop="code">
              <el-input
                v-model="form.code"
                placeholder="请输入商品编码"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-cascader
                v-model="form.categoryId"
                :options="categoryOptions"
                :props="cascaderProps"
                placeholder="请选择分类"
                style="width: 100%"
                clearable
                filterable
                @change="handleCategoryChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                placeholder="请输入价格"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stockQuantity">
              <el-input-number
                v-model="form.stockQuantity"
                :min="0"
                placeholder="请输入库存数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低库存" prop="minStockLevel">
              <el-input-number
                v-model="form.minStockLevel"
                :min="0"
                placeholder="请输入最低库存"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplier">
              <el-input
                v-model="form.supplier"
                placeholder="请输入供应商"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存放位置" prop="location">
              <el-input
                v-model="form.location"
                placeholder="请输入存放位置"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </AppLayout>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { productAPI, categoryAPI } from '@/api'
import AppLayout from '@/components/AppLayout.vue'

export default {
  name: 'ProductForm',
  components: {
    ArrowLeft,
    Plus,
    AppLayout
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loading = ref(false)
    const formRef = ref()
    const categories = ref([])
    
    const isEdit = computed(() => !!route.params.id)
    
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
    
    const form = reactive({
      id: null,
      name: '',
      code: '',
      categoryId: null,
      price: 0,
      stockQuantity: 0,
      minStockLevel: 0,
      supplier: '',
      location: '',
      description: '',
      status: 1
    })
    
    const rules = {
      name: [
        { required: true, message: '请输入商品名称', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      code: [
        { required: true, message: '请输入商品编码', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      categoryId: [
        { required: true, message: '请选择商品分类', trigger: 'change' }
      ],
      price: [
        { required: true, message: '请输入商品价格', trigger: 'blur' },
        { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
      ],
      stockQuantity: [
        { required: true, message: '请输入库存数量', trigger: 'blur' },
        { type: 'number', min: 0, message: '库存数量必须大于等于0', trigger: 'blur' }
      ],
      minStockLevel: [
        { required: true, message: '请输入最低库存', trigger: 'blur' },
        { type: 'number', min: 0, message: '最低库存必须大于等于0', trigger: 'blur' }
      ]
    }
    
    // 加载分类列表
    const loadCategories = async () => {
      try {
        const response = await categoryAPI.getList()
        categories.value = response.data || []
      } catch (error) {
        console.error('加载分类列表失败:', error)
        ElMessage.error('加载分类列表失败')
      }
    }
    
    // 加载商品详情
    const loadProductDetails = async () => {
      if (!isEdit.value) return
      
      try {
        const response = await productAPI.getDetail(route.params.id)
        if (response.success) {
          Object.assign(form, response.data)
        } else {
          ElMessage.error(response.message || '加载商品详情失败')
        }
      } catch (error) {
        console.error('加载商品详情失败:', error)
        ElMessage.error('加载商品详情失败')
      }
    }
    
    
    // 提交表单
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        loading.value = true
        
        if (isEdit.value) {
          await productAPI.update(form.id, form)
          ElMessage.success('商品更新成功')
        } else {
          await productAPI.create(form)
          ElMessage.success('商品创建成功')
        }
        
        router.push('/products')
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败，请检查表单信息')
      } finally {
        loading.value = false
      }
    }
    
    // 取消
    const handleCancel = () => {
      router.push('/products')
    }
    
    // 返回
    const handleBack = () => {
      router.push('/products')
    }
    
    // 分类变化
    const handleCategoryChange = (value) => {
      console.log('分类选择变化:', value)
    }
    
    // 加载商品数据
    const loadProduct = async () => {
      await Promise.all([
        loadCategories(),
        loadProductDetails()
      ])
    }
    
    onMounted(() => {
      loadProduct()
    })
    
    return {
      loading,
      formRef,
      categories,
      isEdit,
      cascaderProps,
      categoryOptions,
      form,
      rules,
      loadCategories,
      loadProductDetails,
      handleSubmit,
      handleCancel,
      handleBack,
      handleCategoryChange
    }
  }
}
</script>

<style scoped>
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
  gap: 8px;
}

.title-section h2 {
  color: #333;
  margin: 0 0 4px 0;
  font-size: 24px;
}

.title-section p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.form-container {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-form {
  max-width: 800px;
}


@media (max-width: 768px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .form-container {
    padding: 16px;
  }
}
</style>