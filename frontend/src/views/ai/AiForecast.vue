<template>
  <AppLayout>
    <div class="page-header">
      <h2>AI智能补货推荐</h2>
      <p>基于大模型预测的补货建议，按建议补货量排序</p>
    </div>

    <div class="page-card">
      <el-row :gutter="20" style="margin-bottom:16px;align-items:center;">
        <el-col :span="6">
          <el-card class="count-card" shadow="hover">
            <div style="display:flex;flex-direction:column;align-items:flex-start;gap:8px;">
              <div style="font-size:12px;color:#888;">需要补货商品数</div>
              <div style="font-size:32px;font-weight:700;">{{ count }}</div>
              <div style="font-size:12px;color:#999;">基于模型推荐的实时结果</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="18" style="display:flex;justify-content:flex-end;">
          <el-button type="primary" :loading="loading" @click="reload">刷新</el-button>
        </el-col>
      </el-row>

      <el-table
        v-loading="loading"
        :data="list"
        stripe
        style="width:100%"
        empty-text="暂无推荐"
      >
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="productCode" label="商品编码" width="140" />
        <el-table-column prop="currentStock" label="当前库存" width="120" />
        <el-table-column prop="minStock" label="最小库存" width="120" />

        <el-table-column prop="forecastSales7Days" label="预测7天销量" width="140">
          <template #default="scope">
            <span style="color:#fa8c16;font-weight:600">{{ scope.row.forecastSales7Days }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="suggestReorderQuantity" label="建议补货量" width="160">
          <template #default="scope">
            <span style="color:#ff4d4f;font-weight:800">{{ scope.row.suggestReorderQuantity }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="advice" label="AI专家建议" min-width="360">
          <template #default="scope">
            <el-card shadow="hover" class="advice-card">
              <div v-html="formatAdvice(scope.row.advice)"></div>
            </el-card>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <el-empty description="暂无补货建议"></el-empty>
      </div>
    </div>
  </AppLayout>
</template>

<script>
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { aiAPI } from '@/api'

export default {
  name: 'AiForecast',
  components: { AppLayout },
  setup() {
    const count = ref(0)
    const list = ref([])
    const loading = ref(false)

    const load = async () => {
      loading.value = true
      try {
        const resCount = await aiAPI.getReplenishmentCount()
        if (resCount) {
          if (typeof resCount === 'number') {
            count.value = resCount
          } else if (resCount.data !== undefined) {
            count.value = resCount.data
          }
        }

        const resList = await aiAPI.getRecommendList()
        if (resList && resList.data) {
          list.value = resList.data
        } else if (Array.isArray(resList)) {
          list.value = resList
        } else {
          list.value = []
        }
      } catch (e) {
        console.error('加载AI补货建议失败', e)
      } finally {
        loading.value = false
      }
    }

    const reload = () => {
      load()
    }

    const formatAdvice = (text) => {
      if (!text) return ''
      // 简单将换行替换为 <br/>
      return text.replace(/\n/g, '<br/>')
    }

    onMounted(() => {
      load()
    })

    return {
      count,
      list,
      loading,
      reload,
      formatAdvice
    }
  }
}
</script>

<style scoped>
.page-header {margin-bottom: 20px;}
.page-header h2 {color: #333;margin: 0 0 8px 0;}
.page-header p {color: #666; margin: 0;}

.page-card { background:#fff; padding:16px; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,0.04); margin-top:12px; }
.count-card { background: linear-gradient(90deg,#f0f5ff,#fff); }
.advice-card { background: linear-gradient(180deg, #f5f7ff, #ffffff); border-radius:6px; padding:10px; }
.el-table .advice-card { box-shadow: none; }
.empty-state { padding: 40px; text-align: center; }
</style>


