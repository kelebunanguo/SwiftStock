<template>
	<AppLayout>
		<div class="page-header">
			<h2>供应商详情</h2>
		</div>

		<div class="card">
			<!-- 供应商基本信息 -->
			<el-descriptions title="供应商信息" :column="2" border>
				<el-descriptions-item label="名称">{{ supplier.name }}</el-descriptions-item>
				<el-descriptions-item label="联系人">{{ supplier.contactPerson }}</el-descriptions-item>
				<el-descriptions-item label="电话">{{ supplier.phone }}</el-descriptions-item>
				<el-descriptions-item label="邮箱">{{ supplier.email }}</el-descriptions-item>
				<el-descriptions-item label="地址" :span="2">{{ supplier.address }}</el-descriptions-item>
			</el-descriptions>
		</div>

		<div class="mt-12">
			<div style="display:flex; justify-content:space-between; align-items:center;">
				<h3>供货记录</h3>
				<el-button type="primary" size="small" @click="openAdd">新增供货记录</el-button>
			</div>

			<!-- 使用与供应商列表相同的 table-container 样式 -->
			<div class="table-container" style="margin-top:8px;">
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
					<el-table-column label="操作" width="160">
						<template #default="scope">
							<el-button type="primary" size="mini" @click="openEdit(scope.row)">编辑</el-button>
							<el-button type="danger" size="mini" @click="confirmDelete(scope.row.id)">删除</el-button>
						</template>
					</el-table-column>
				</el-table>
				<div class="pagination-container">
					<el-pagination
						v-model:current-page="page"
						v-model:page-size="size"
						:page-sizes="[10,20,50]"
						:total="total"
						background
						layout="total, sizes, prev, pager, next, jumper"
						@size-change="onSizeChange"
						@current-change="onPageChange"
					/>
				</div>
			</div>
		</div>

		<!-- 新增/编辑弹窗：使用 el-dialog + el-form -->
		<el-dialog :title="isEditing ? '编辑供货记录' : '新增供货记录'" v-model="dialogVisible" width="560px">
			<el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
				<el-form-item label="商品名称" prop="productId">
					<el-select v-model="form.productId" placeholder="请选择商品" filterable clearable>
						<el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
					</el-select>
				</el-form-item>

				<el-form-item label="数量" prop="quantity">
					<el-input-number v-model="form.quantity" :min="1" @change="recomputeTotal" />
				</el-form-item>

				<el-form-item label="单价" prop="unitPrice">
					<el-input-number v-model="form.unitPrice" :min="0" :step="0.01" @change="recomputeTotal" />
				</el-form-item>

				<el-form-item label="总额">
					<span>¥{{ totalAmountDisplay }}</span>
				</el-form-item>

				<el-form-item label="到货时间" prop="receiveTime">
					<el-date-picker
						v-model="form.receiveTime"
						type="datetime"
						placeholder="选择到货时间"
						format="YYYY-MM-DD HH:mm:ss"
						value-format="YYYY-MM-DD HH:mm:ss"
					/>
				</el-form-item>

				<el-form-item label="备注" prop="remark">
					<el-input v-model="form.remark" type="textarea" :rows="3" />
				</el-form-item>
			</el-form>

			<div slot="footer" class="dialog-footer">
				<el-button @click="dialogVisible = false">取消</el-button>
				<el-button type="primary" @click="save">提交</el-button>
			</div>
		</el-dialog>
	</AppLayout>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { supplierAPI, productAPI, supplyRecordAPI } from '@/api'
import api from '@/api'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
	name: 'SupplierView',
	components: { AppLayout },
	setup() {
		const route = useRoute()
		const id = route.params.id
		const supplier = ref({})
		const records = ref([])
		const loading = ref(false)

		// 分页
		const page = ref(1)
		const size = ref(10)
		const total = ref(0)

		// 弹窗与表单
		const dialogVisible = ref(false)
		const isEditing = ref(false)
		const formRef = ref(null)
		const form = reactive({
			id: null,
			supplierId: id,
			productId: null,
			productName: '',
			quantity: 1,
			unitPrice: 0,
			totalAmount: 0,
			receiveTime: null,
			remark: ''
		})

		const rules = {
			productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
			quantity: [{ required: true, message: '请输入数量', trigger: 'change' }],
			unitPrice: [{ required: true, message: '请输入单价', trigger: 'change' }]
		}

		const products = ref([])

		// 计算显示用的总额（基于数量 * 单价，保留两位小数）
		const totalAmountDisplay = computed(() => {
			const q = Number(form.quantity || 0)
			const u = Number(form.unitPrice || 0)
			const t = (q * u).toFixed(2)
			return t
		})

		// 将计算结果写入表单的 totalAmount 字段（在提交给后端前同步）
		const recomputeTotal = () => {
			const q = Number(form.quantity || 0)
			const u = Number(form.unitPrice || 0)
			form.totalAmount = Number((q * u).toFixed(2))
		}

		const loadSupplier = async () => {
			try {
				const s = await supplierAPI.getById(id)
				supplier.value = s.data || {}
			} catch (e) {
				ElMessage.error('加载供应商信息失败')
			}
		}

		const loadProducts = async () => {
			try {
				// 后端没有按 supplierId 返回商品的接口，使用可用商品列表作为下拉数据保证有 name 字段
				const res = await productAPI.getAvailable()
				products.value = res.data?.list || res.data || []
			} catch (e) {
				products.value = []
			}
		}

		// 加载供货记录（分页），返回的数据结构为 { list, total }
		const loadRecords = async (p = page.value) => {
			loading.value = true
			try {
				const res = await supplyRecordAPI.getList({ supplierId: id, page: p, size: size.value })
				if (!res || !res.success) {
					ElMessage.error(res?.message || '加载供货记录失败')
					return
				}
				const data = res.data || {}
				records.value = data.list || []
				total.value = data.total || 0
			} catch (e) {
				ElMessage.error('加载供货记录失败')
			} finally {
				loading.value = false
			}
		}

		const onPageChange = (p) => {
			page.value = p
			loadRecords(p)
		}

		const onSizeChange = (newSize) => {
			size.value = newSize
			page.value = 1
			loadRecords(1)
		}

		// 格式化时间为 YYYY-MM-DD HH:mm:ss（用于默认 receiveTime）
		const formatDateTime = (date) => {
			const pad = (n) => String(n).padStart(2, '0')
			const Y = date.getFullYear()
			const M = pad(date.getMonth() + 1)
			const D = pad(date.getDate())
			const h = pad(date.getHours())
			const m = pad(date.getMinutes())
			const s = pad(date.getSeconds())
			return `${Y}-${M}-${D} ${h}:${m}:${s}`
		}

		const openAdd = () => {
			// debug: 打开新增弹窗
			console.log('[SupplierView] openAdd')
			isEditing.value = false
			Object.assign(form, {
				id: null,
				supplierId: id,
				productId: null,
				productName: '',
				quantity: 1,
				unitPrice: 0,
				totalAmount: 0,
				receiveTime: formatDateTime(new Date()),
				remark: ''
			})
			dialogVisible.value = true
		}

		const openEdit = (row) => {
			// debug: 打开编辑弹窗
			console.log('[SupplierView] openEdit', row && row.id)
			isEditing.value = true
			Object.assign(form, {
				id: row.id,
				supplierId: row.supplierId,
				productId: row.productId,
				productName: row.productName,
				quantity: row.quantity,
				unitPrice: row.unitPrice,
				totalAmount: row.totalAmount,
				receiveTime: row.receiveTime,
				remark: row.remark
			})
			dialogVisible.value = true
		}

		const save = () => {
			// debug: 点击保存
			console.log('[SupplierView] save', { isEditing: isEditing.value, form })
			form.productName = products.value.find(p => p.id === form.productId)?.name || ''
			recomputeTotal()
			// 如果未填写到货时间，前端默认当前时间（后端也会处理）
			if (!form.receiveTime) {
				form.receiveTime = formatDateTime(new Date())
			}
			formRef.value.validate(async (valid) => {
				if (!valid) return
				try {
					if (isEditing.value) {
						const res = await supplyRecordAPI.update(form.id, {
							supplierId: form.supplierId,
							productId: form.productId,
							productName: form.productName,
							quantity: form.quantity,
							unitPrice: form.unitPrice,
							receiveTime: form.receiveTime,
							remark: form.remark
						})
						if (res && res.success) {
							ElMessage.success('更新成功')
							dialogVisible.value = false
							loadRecords(page.value)
						} else {
							ElMessage.error(res?.message || '更新失败')
						}
					} else {
						const res = await supplyRecordAPI.create({
							supplierId: form.supplierId,
							productId: form.productId,
							productName: form.productName,
							quantity: form.quantity,
							unitPrice: form.unitPrice,
							receiveTime: form.receiveTime,
							remark: form.remark
						})
						if (res && res.success) {
							ElMessage.success('创建成功')
							dialogVisible.value = false
							loadRecords(1)
						} else {
							ElMessage.error(res?.message || '创建失败')
						}
					}
				} catch (e) {
					ElMessage.error('保存失败')
				}
			})
		}

		// 删除确认：在用户确认后调用后端删除接口
		const confirmDelete = (idToDelete) => {
			ElMessageBox.confirm('确认删除该供货记录吗？', '提示', {
				type: 'warning'
			}).then(async () => {
				try {
					const res = await supplyRecordAPI.delete(idToDelete)
					if (res && res.success) {
						ElMessage.success('删除成功')
						// 删除后保持当前页刷新
						loadRecords(page.value)
					} else {
						ElMessage.error(res?.message || '删除失败')
					}
				} catch (e) {
					ElMessage.error('删除失败')
				}
			}).catch(() => {
				// cancel
			})
		}

		onMounted(async () => {
			await Promise.all([loadSupplier(), loadProducts()])
			loadRecords()
		})

		return {
			supplier,
			records,
			loading,
			dialogVisible,
			form,
			products,
			formRef,
			rules,
			isEditing,
			page,
			size,
			total,
			onPageChange,
			onSizeChange,
			openAdd,
			openEdit,
			save,
			confirmDelete,
			totalAmountDisplay,
			recomputeTotal
		}
	}
}
</script>

<style scoped>
.card { background: #fff; padding: 12px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.mt-12 { margin-top: 12px; }
.table-container { background: #fff; padding: 12px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.pagination-container { margin-top: 12px; display:flex; justify-content:flex-end; }
</style>


