<template>
  <AppLayout>
    <div class="page-header">
      <h2>{{ isEdit ? '编辑供应商' : '新增供应商' }}</h2>
    </div>

    <el-form :model="form" ref="formRef" label-width="100px">
      <el-form-item label="供应商名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="form.contactPerson" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input type="textarea" v-model="form.remark" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </AppLayout>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { supplierAPI } from '@/api'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'SupplierForm',
  components: { AppLayout },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const id = route.params.id
    const isEdit = !!id
    const formRef = ref(null)
    const form = reactive({
      name: '',
      contactPerson: '',
      phone: '',
      email: '',
      address: '',
      remark: ''
    })

    const load = async () => {
      if (isEdit) {
        const res = await supplierAPI.getById(id)
        Object.assign(form, res.data || {})
      }
    }

    const handleSubmit = async () => {
      try {
        if (isEdit) {
          const res = await supplierAPI.update(id, form)
          if (res.success) {
            ElMessage.success('保存成功')
            router.push('/suppliers')
          } else {
            ElMessage.error(res.message || '保存失败')
          }
        } else {
          const res = await supplierAPI.create(form)
          if (res.success) {
            ElMessage.success('创建成功')
            router.push('/suppliers')
          } else {
            ElMessage.error(res.message || '创建失败')
          }
        }
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }

    const handleCancel = () => {
      router.push('/suppliers')
    }

    onMounted(load)
    return { form, formRef, isEdit, handleSubmit, handleCancel }
  }
}
</script>

<style scoped>
.page-header { margin-bottom: 12px; }
</style>


