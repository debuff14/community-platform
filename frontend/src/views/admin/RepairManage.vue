<template>
  <el-card shadow="never">
    <div class="toolbar">
      <div class="filters">
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 130px" @change="onFilterChange">
          <el-option v-for="(item, key) in REPAIR_STATUS" :key="key" :label="item.text" :value="Number(key)" />
        </el-select>
        <el-select v-model="filters.type" placeholder="类型" clearable style="width: 120px" @change="onFilterChange">
          <el-option label="水电气" :value="1" />
          <el-option label="门窗" :value="2" />
          <el-option label="电梯" :value="3" />
          <el-option label="其他" :value="0" />
        </el-select>
        <el-select v-model="filters.urgency" placeholder="紧急程度" clearable style="width: 120px" @change="onFilterChange">
          <el-option label="紧急" :value="1" />
          <el-option label="普通" :value="0" />
        </el-select>
      </div>
      <span class="filter-tip">已取消的工单默认不显示，可通过状态筛选查看</span>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column width="50" align="center">
        <template #default="{ row }">
          <span v-if="row.urgency === 1" class="urgent-mark" title="紧急工单">!</span>
        </template>
      </el-table-column>
      <el-table-column label="业主" width="150">
        <template #default="{ row }">{{ row.username }}（{{ row.building }}{{ row.roomNo }}）</template>
      </el-table-column>
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ repairTypeName(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="description" label="问题描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="紧急" width="80">
        <template #default="{ row }">
          <el-tag :type="row.urgency === 1 ? 'danger' : 'info'" size="small">{{ row.urgency === 1 ? '紧急' : '普通' }}</el-tag>
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
      <el-table-column label="评价" width="170">
        <template #default="{ row }">
          <template v-if="row.star">
            <el-rate :model-value="row.star" disabled size="small" />
            <div v-if="row.commentContent" class="comment-text">{{ row.commentContent }}</div>
          </template>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="onAccept(row)">接单</el-button>
          <el-button v-if="row.status === 1" type="primary" size="small" @click="onStart(row)">开始处理</el-button>
          <el-button v-if="row.status === 2" type="success" size="small" @click="openComplete(row)">填写结果并完成</el-button>
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无工单" />

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

    <el-dialog v-model="completeVisible" title="填写处理结果" width="520px" :close-on-click-modal="false">
      <el-input
        v-model="resultText"
        type="textarea"
        :rows="5"
        maxlength="500"
        show-word-limit
        placeholder="请填写处理说明，例如：已更换水阀，试水正常"
      />
      <template #footer>
        <el-button :disabled="completing" @click="completeVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!resultText.trim()" :loading="completing" @click="submitComplete">确定完成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工单详情" width="620px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="业主">{{ detail.username }}（{{ detail.building }}{{ detail.roomNo }}）</el-descriptions-item>
        <el-descriptions-item label="类型">{{ repairTypeName(detail.type) }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">{{ detail.urgency === 1 ? '紧急' : '普通' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ repairStatus(detail.status).text }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ detail.description }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.acceptTime" label="接单时间">{{ detail.acceptTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.finishTime" label="完成时间">{{ detail.finishTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.result" label="处理结果">{{ detail.result }}</el-descriptions-item>
        <el-descriptions-item v-if="detailImages.length" label="现场照片">
          <div class="images">
            <el-image
              v-for="(img, index) in detailImages"
              :key="index"
              :src="img"
              :preview-src-list="detailImages"
              :initial-index="index"
              fit="cover"
              class="image"
            />
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  acceptRepair,
  completeRepair,
  getAdminRepairDetail,
  getAdminRepairPage,
  startRepair
} from '@/api/repair'
import { REPAIR_STATUS, repairImages, repairStatus, repairTypeName } from '@/utils/repair'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const filters = reactive({ status: null, type: null, urgency: null })

const completeVisible = ref(false)
const completing = ref(false)
const currentOrder = ref(null)
const resultText = ref('')

const detailVisible = ref(false)
const detail = ref(null)

const detailImages = computed(() => (detail.value ? repairImages(detail.value.images) : []))

const load = async () => {
  loading.value = true
  try {
    const data = await getAdminRepairPage({
      page: page.value,
      size: size.value,
      status: filters.status ?? undefined,
      type: filters.type ?? undefined,
      urgency: filters.urgency ?? undefined
    })
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

const onAccept = async (row) => {
  try {
    await acceptRepair(row.id)
    ElMessage.success('接单成功')
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

const onStart = async (row) => {
  try {
    await startRepair(row.id)
    ElMessage.success('已开始处理')
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

const openComplete = (row) => {
  currentOrder.value = row
  resultText.value = ''
  completeVisible.value = true
}

const submitComplete = async () => {
  completing.value = true
  try {
    await completeRepair(currentOrder.value.id, { result: resultText.value })
    ElMessage.success('工单已完成')
    completeVisible.value = false
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    completing.value = false
  }
}

const openDetail = async (row) => {
  try {
    detail.value = await getAdminRepairDetail(row.id)
    detailVisible.value = true
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
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filter-tip {
  font-size: 13px;
  color: #909399;
}

.urgent-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  font-weight: 700;
}

.comment-text {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.muted {
  color: #c0c4cc;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image {
  width: 100px;
  height: 100px;
  border-radius: 6px;
}
</style>
