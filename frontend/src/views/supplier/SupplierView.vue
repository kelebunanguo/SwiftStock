<template>
  <AppLayout>
    <div class="page-header">
      <h2>供应商详情</h2>
    </div>

    <div class="card">
      <el-descriptions title="供应商信息" :column="2" border>
        <el-descriptions-item label="名称">{{ supplier.name }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ supplier.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ supplier.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ supplier.email }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ supplier.address }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="mt-12">
      <h3>供货记录</h3>
      <div class="table-container">
        <el-table :data="records" v-loading="loading" style="width:100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="unitPrice" label="单价" width="120">
            <template #default="scope">¥{{ scope.row.unitPrice }}</template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="总额" width="120">
            <template #default="scope">¥{{ scope.row.totalAmount }}</template>
          </el-table-column>
          <el-table-column prop="receiveTime" label="到货时间" width="180"/>
          <el-table-column prop="remark" label="备注" />
        </el-table>
      </div>
    </div>
  </AppLayout>
</template>

<script>
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { supplierAPI } from '@/api'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'SupplierView',
  components: { AppLayout },
  setup() {
    const route = useRoute()
    const id = route.params.id
    const supplier = ref({})
    const records = ref([])
    const loading = ref(false)

    const load = async () => {
      loading.value = true
      try {
        const s = await supplierAPI.getById(id)
        supplier.value = s.data || {}
        const r = await supplierAPI.getRecords(id)
        records.value = r.data || []
      } catch (e) {
        ElMessage.error('加载失败')
      } finally {
        loading.value = false
      }
    }

    onMounted(load)
    return { supplier, records, loading }
  }
}
</script>

<style scoped>
.card { background: #fff; padding: 12px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.mt-12 { margin-top: 12px; }
</style>


