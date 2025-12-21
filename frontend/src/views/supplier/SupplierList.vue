<template>
  <AppLayout>
    <div class="page-header">
      <h2>供应商管理</h2>
      <p>管理供应商信息与联系方式</p>
    </div>

    <div class="search-form" style="margin-bottom:12px; display:flex; gap:8px; align-items:center;">
      <el-input v-model="searchForm.name" placeholder="供应商名称" clearable style="width:220px"></el-input>
      <el-input v-model="searchForm.contactPerson" placeholder="联系人" clearable style="width:180px"></el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div style="flex:1"></div>
      <div>
        <el-button type="primary" @click="handleCreate">新建供应商</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="suppliers" v-loading="loading" style="width:100%">
        <el-table-column label="序号" width="70">
          <template #default="scope">
            {{ (pagination.page - 1) * pagination.size + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="供应商名称" />
        <el-table-column prop="contactPerson" label="联系人" width="140" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button type="info" size="small" @click="handleView(scope.row)">供货记录</el-button>
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container" style="margin-top:12px; display:flex; justify-content:flex-end;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10,20,50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </AppLayout>
</template>

<script>
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { supplierAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

export default {
  name: 'SupplierList',
  components: { AppLayout },
  setup() {
    const suppliers = ref([])
    const loading = ref(false)
    const router = useRouter()

    const load = async () => {
      loading.value = true
      try {
        const res = await supplierAPI.getList()
        suppliers.value = res.data || []
      } catch (e) {
        ElMessage.error('加载供应商失败')
      } finally {
        loading.value = false
      }
    }

    const handleCreate = () => {
      router.push('/suppliers/add')
    }

    const handleView = (row) => {
      router.push(`/suppliers/view/${row.id}`)
    }

    const handleEdit = (row) => {
      router.push(`/suppliers/edit/${row.id}`)
    }

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定删除该供应商吗？', '确认', { type: 'warning' })
        const res = await supplierAPI.delete(row.id)
        if (res.success) {
          ElMessage.success('删除成功')
          load()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (e) { }
    }

    const searchForm = ref({ name: '', contactPerson: '' })

    const pagination = ref({ page: 1, size: 10, total: 0 })

    const loadPage = async () => {
      loading.value = true
      try {
        const params = {
          name: searchForm.value.name,
          contactPerson: searchForm.value.contactPerson,
          page: pagination.value.page,
          size: pagination.value.size
        }
        const res = await supplierAPI.getList(params)
        suppliers.value = res.data.list || []
        pagination.value.total = res.data.total || 0
      } catch (e) {
        ElMessage.error('加载供应商失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      pagination.value.page = 1
      loadPage()
    }

    const handleReset = () => {
      searchForm.value.name = ''
      searchForm.value.contactPerson = ''
      pagination.value.page = 1
      loadPage()
    }

    const handleSizeChange = (size) => {
      pagination.value.size = size
      pagination.value.page = 1
      loadPage()
    }

    const handleCurrentChange = (page) => {
      pagination.value.page = page
      loadPage()
    }

    onMounted(loadPage)
    return { suppliers, loading, handleCreate, handleEdit, handleDelete, searchForm, handleSearch, handleReset, pagination, handleSizeChange, handleCurrentChange, handleView }
  }
}
</script>

<style scoped>
.page-header { margin-bottom: 12px; }
.table-container { background: #fff; padding: 12px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
</style>


