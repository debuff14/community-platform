<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="onFilterChange">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">未支付</el-radio-button>
        <el-radio-button :value="1">已支付</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="费用类型" width="120">
        <template #default="{ row }">{{ feeTypeName(row.feeType) }}</template>
      </el-table-column>
      <el-table-column label="金额" width="130">
        <template #default="{ row }">
          <span class="amount">¥ {{ Number(row.amount).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="month" label="所属月份" width="120" />
      <el-table-column label="状态" width="220">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">已支付</el-tag>
          <el-tag v-else type="warning" size="small">未支付</el-tag>
          <span v-if="row.status === 1 && row.payTime" class="pay-time">{{ row.payTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="openPay(row)">去支付</el-button>
          <span v-else class="paid-text">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="还没有账单，等物业生成后这里会显示" />

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

    <el-dialog v-model="payVisible" title="确认支付" width="400px" :close-on-click-modal="false">
      <div v-if="current" class="pay-info">
        <div class="pay-row"><span>费用类型</span><b>{{ feeTypeName(current.feeType) }}</b></div>
        <div class="pay-row"><span>所属月份</span><b>{{ current.month }}</b></div>
        <div class="pay-row amount-row"><span>应付金额</span><b class="amount">¥ {{ Number(current.amount).toFixed(2) }}</b></div>
      </div>
      <template #footer>
        <el-button :disabled="paying" @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyBillPage, payBill } from '@/api/bill'

const FEE_TYPE_NAMES = { 1: '物业费', 2: '停车费', 3: '水电费' }

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref(null)
const loading = ref(false)

const payVisible = ref(false)
const paying = ref(false)
const current = ref(null)

const feeTypeName = (type) => FEE_TYPE_NAMES[type] || '其他'

const formatNow = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const load = async () => {
  loading.value = true
  try {
    const data = await getMyBillPage({ page: page.value, size: size.value, status: statusFilter.value ?? undefined })
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

const openPay = (row) => {
  current.value = row
  payVisible.value = true
}

const confirmPay = async () => {
  paying.value = true
  try {
    await payBill(current.value.id)
    current.value.status = 1
    current.value.payTime = formatNow()
    ElMessage.success('支付成功')
    payVisible.value = false
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    paying.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.pay-time {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
}

.paid-text {
  color: #c0c4cc;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.pay-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
}

.amount-row {
  border-top: 1px dashed #e4e7ed;
  margin-top: 6px;
  padding-top: 14px;
}

.amount-row .amount {
  font-size: 20px;
}
</style>
