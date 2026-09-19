<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="onFilterChange">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button v-for="(item, key) in REPAIR_STATUS" :key="key" :value="Number(key)">
          {{ item.text }}
        </el-radio-button>
      </el-radio-group>
      <el-button type="primary" :icon="Plus" @click="router.push('/repair/new')">我要报修</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe @row-click="goDetail">
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ repairTypeName(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="description" label="问题描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="紧急程度" width="100">
        <template #default="{ row }">
          <el-tag :type="row.urgency === 1 ? 'danger' : 'info'" size="small">
            {{ row.urgency === 1 ? '紧急' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="repairStatus(row.status).type" :effect="repairStatus(row.status).effect || 'light'" size="small">
            {{ repairStatus(row.status).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 3" link type="warning" @click.stop="openEvaluate(row)">去评价</el-button>
          <el-button v-if="row.status === 0" link type="danger" @click.stop="onCancel(row)">取消</el-button>
          <el-button link type="primary" @click.stop="goDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="还没有报修记录，点击“我要报修”提交" />

    <div class="pagination" v-if="total > 0">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>

    <el-dialog v-model="evaluateVisible" title="评价报修" width="440px" :close-on-click-modal="false">
      <div class="evaluate-form">
        <div class="evaluate-row">
          <span>服务评分</span>
          <el-rate v-model="rating" />
        </div>
        <el-input v-model="commentContent" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="说说这次的维修体验（选填）" />
      </div>
      <template #footer>
        <el-button :disabled="evaluating" @click="evaluateVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluating" @click="submitEvaluate">提交评价</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { cancelRepair, evaluateRepair, getMyRepairPage } from '@/api/repair'
import { REPAIR_STATUS, repairStatus, repairTypeName } from '@/utils/repair'

const router = useRouter()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref(null)
const loading = ref(false)

const evaluateVisible = ref(false)
const evaluating = ref(false)
const currentOrder = ref(null)
const rating = ref(5)
const commentContent = ref('')

const load = async () => {
  loading.value = true
  try {
    const data = await getMyRepairPage({ page: page.value, size: size.value, status: statusFilter.value ?? undefined })
    list.value = data.records
    total.value = data.total
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  page.value = 1
  load()
}

const onPageChange = (p) => {
  page.value = p
  load()
}

const goDetail = (row) => {
  router.push(`/repair/${row.id}`)
}

const openEvaluate = (row) => {
  currentOrder.value = row
  rating.value = 5
  commentContent.value = ''
  evaluateVisible.value = true
}

const submitEvaluate = async () => {
  evaluating.value = true
  try {
    await evaluateRepair(currentOrder.value.id, { star: rating.value, content: commentContent.value })
    ElMessage.success('评价成功')
    evaluateVisible.value = false
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    evaluating.value = false
  }
}

const onCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该报修吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await cancelRepair(row.id)
    ElMessage.success('已取消报修')
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.evaluate-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
</style>
