<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="title">访客管理（当日及未来）</span>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="业主" width="160">
        <template #default="{ row }">{{ row.username }}（{{ row.building }}{{ row.roomNo }}）</template>
      </el-table-column>
      <el-table-column prop="visitorName" label="访客姓名" width="110" />
      <el-table-column label="车牌号" width="110">
        <template #default="{ row }">{{ row.carNo || '—' }}</template>
      </el-table-column>
      <el-table-column prop="visitDate" label="来访日期" width="120" />
      <el-table-column label="预计时段" width="150">
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
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="onEnter(row)">登记入场</el-button>
          <el-button v-if="row.status === 1" type="warning" size="small" @click="onLeave(row)">登记离场</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无访客登记" />

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
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { enterVisitor, getAdminVisitorPage, leaveVisitor } from '@/api/visitor'
import { visitorStatus } from '@/utils/visitor'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const shortTime = (time) => (time ? String(time).slice(0, 5) : '—')

const load = async () => {
  loading.value = true
  try {
    const data = await getAdminVisitorPage({ page: page.value, size: size.value })
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

const onEnter = async (row) => {
  try {
    await enterVisitor(row.id)
    ElMessage.success('已登记入场')
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

const onLeave = async (row) => {
  try {
    await leaveVisitor(row.id)
    ElMessage.success('已登记离场')
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
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
