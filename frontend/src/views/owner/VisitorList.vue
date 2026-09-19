<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="title">我的访客</span>
      <el-button type="primary" :icon="Plus" @click="openRegister">登记访客</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="visitorName" label="访客姓名" width="110" />
      <el-table-column label="车牌号" width="110">
        <template #default="{ row }">{{ row.carNo || '—' }}</template>
      </el-table-column>
      <el-table-column prop="visitDate" label="来访日期" width="120" />
      <el-table-column label="预计时段" width="160">
        <template #default="{ row }">{{ shortTime(row.timeStart) }} - {{ shortTime(row.timeEnd) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.expired" type="info" size="small">已过期</el-tag>
          <el-tag v-else :type="visitorStatus(row.status).type" size="small">{{ visitorStatus(row.status).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="入场时间" width="170">
        <template #default="{ row }">{{ row.enterTime || '—' }}</template>
      </el-table-column>
      <el-table-column label="离场时间" width="170">
        <template #default="{ row }">{{ row.leaveTime || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="danger" @click="onCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无访客登记记录" />

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

    <el-dialog v-model="dialogVisible" title="登记访客" width="460px" :close-on-click-modal="false" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="form.visitorName" maxlength="50" placeholder="请输入访客姓名" />
        </el-form-item>
        <el-form-item label="车牌号">
          <el-input v-model="form.carNo" maxlength="20" placeholder="选填" />
        </el-form-item>
        <el-form-item label="来访日期" prop="visitDate">
          <el-date-picker
            v-model="form.visitDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择来访日期"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预计时段" prop="timeRange">
          <el-time-picker
            v-model="form.timeRange"
            is-range
            value-format="HH:mm:ss"
            format="HH:mm"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交登记</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { cancelVisitor, getMyVisitorPage, registerVisitor } from '@/api/visitor'
import { visitorStatus } from '@/utils/visitor'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  visitorName: '',
  carNo: '',
  visitDate: '',
  timeRange: []
})

const rules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择来访日期', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择预计时段', trigger: 'change' }]
}

const shortTime = (time) => (time ? String(time).slice(0, 5) : '—')

const disabledDate = (date) => date.getTime() < Date.now() - 24 * 3600 * 1000

const load = async () => {
  loading.value = true
  try {
    const data = await getMyVisitorPage({ page: page.value, size: size.value })
    list.value = data.records
    total.value = data.total
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}

const onPageChange = (p) => {
  page.value = p
  load()
}

const openRegister = () => {
  dialogVisible.value = true
}

const resetForm = () => {
  form.visitorName = ''
  form.carNo = ''
  form.visitDate = ''
  form.timeRange = []
  formRef.value?.clearValidate()
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await registerVisitor({
      visitorName: form.visitorName,
      carNo: form.carNo || undefined,
      visitDate: form.visitDate,
      timeStart: form.timeRange[0],
      timeEnd: form.timeRange[1]
    })
    ElMessage.success('访客登记成功')
    dialogVisible.value = false
    page.value = 1
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    submitting.value = false
  }
}

const onCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该访客登记吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await cancelVisitor(row.id)
    ElMessage.success('已取消登记')
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
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
