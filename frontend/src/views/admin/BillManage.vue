<template>
  <el-card shadow="never">
    <div class="toolbar">
      <div class="filters">
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="onFilterChange">
          <el-option label="未支付" :value="0" />
          <el-option label="已支付" :value="1" />
        </el-select>
        <el-select v-model="filters.building" placeholder="楼栋" clearable style="width: 120px" @change="onFilterChange">
          <el-option v-for="b in buildings" :key="b" :label="b" :value="b" />
        </el-select>
        <el-date-picker
          v-model="filters.month"
          type="month"
          value-format="YYYY-MM"
          placeholder="所属月份"
          clearable
          style="width: 160px"
          @change="onFilterChange"
        />
      </div>
      <div class="actions">
        <el-button :icon="Upload" :loading="exporting" @click="onExport">导出 Excel</el-button>
        <el-button :icon="Setting" @click="openStandard">收费标准</el-button>
        <el-button type="primary" :icon="Plus" @click="openGenerate">生成账单</el-button>
      </div>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="username" label="业主姓名" width="110" />
      <el-table-column label="楼栋房号" width="120">
        <template #default="{ row }">{{ row.building }}{{ row.roomNo }}</template>
      </el-table-column>
      <el-table-column label="费用类型" width="110">
        <template #default="{ row }">{{ feeTypeName(row.feeType) }}</template>
      </el-table-column>
      <el-table-column label="金额" width="120">
        <template #default="{ row }">
          <span class="amount">¥ {{ Number(row.amount).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="month" label="所属月份" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">已支付</el-tag>
          <el-tag v-else type="warning" size="small">未支付</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="170">
        <template #default="{ row }">{{ row.payTime || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" :disabled="row.status === 1" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无账单，可点击“生成账单”批量创建" />

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

    <el-dialog v-model="generateVisible" title="批量生成账单" width="460px" :close-on-click-modal="false" @closed="resetGenerateForm">
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="90px">
        <el-form-item label="楼栋" prop="building">
          <el-select v-model="generateForm.building" placeholder="全部楼栋" style="width: 100%">
            <el-option label="全部楼栋" value="" />
            <el-option v-for="b in buildings" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型" prop="feeType">
          <el-select v-model="generateForm.feeType" placeholder="请选择费用类型" style="width: 100%">
            <el-option label="物业费" :value="1" />
            <el-option label="停车费" :value="2" />
            <el-option label="水电费" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="generateForm.amount" :precision="2" :min="0.01" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="所属月份" prop="month">
          <el-date-picker
            v-model="generateForm.month"
            type="month"
            value-format="YYYY-MM"
            placeholder="请选择所属月份"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="generating" @click="generateVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="submitGenerate">确定生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="standardVisible" title="收费标准" width="440px" :close-on-click-modal="false">
      <div class="standard-tip">修改后，下批自动生成的账单将按新标准计算金额。</div>
      <div v-for="item in standards" :key="item.feeType" class="standard-row">
        <span>{{ feeTypeName(item.feeType) }}</span>
        <el-input-number v-model="item.amount" :precision="2" :min="0.01" :step="10" />
      </div>
      <template #footer>
        <el-button :disabled="savingStandard" @click="standardVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingStandard" @click="saveStandard">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Setting, Upload } from '@element-plus/icons-vue'
import { deleteBill, exportBills, generateBills, getAdminBillPage, getFeeStandards, updateFeeStandards } from '@/api/bill'

const FEE_TYPE_NAMES = { 1: '物业费', 2: '停车费', 3: '水电费' }
const buildings = Array.from({ length: 10 }, (_, i) => `${i + 1}栋`)

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const exporting = ref(false)

const filters = reactive({ status: null, building: null, month: null })

const generateVisible = ref(false)
const generating = ref(false)
const generateFormRef = ref()
const generateForm = reactive({ building: '', feeType: 1, amount: 350, month: '' })

const standardVisible = ref(false)
const savingStandard = ref(false)
const standards = ref([])

const feeTypeName = (type) => FEE_TYPE_NAMES[type] || '其他'

const generateRules = {
  feeType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  month: [{ required: true, message: '请选择所属月份', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const data = await getAdminBillPage({
      page: page.value,
      size: size.value,
      status: filters.status ?? undefined,
      building: filters.building || undefined,
      month: filters.month || undefined
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

const openGenerate = () => {
  generateVisible.value = true
}

const resetGenerateForm = () => {
  generateForm.building = ''
  generateForm.feeType = 1
  generateForm.amount = 350
  generateForm.month = ''
  generateFormRef.value?.clearValidate()
}

const submitGenerate = async () => {
  const valid = await generateFormRef.value.validate().catch(() => false)
  if (!valid) return
  generating.value = true
  try {
    const data = await generateBills({
      building: generateForm.building || undefined,
      feeType: generateForm.feeType,
      amount: generateForm.amount,
      month: generateForm.month
    })
    ElMessage.success(`本次生成 ${data.generated} 条账单，跳过 ${data.skipped} 条已存在的`)
    generateVisible.value = false
    page.value = 1
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    generating.value = false
  }
}

const onDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该账单？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await deleteBill(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

const onExport = async () => {
  exporting.value = true
  try {
    const { blob, total: count } = await exportBills({
      status: filters.status ?? undefined,
      building: filters.building || undefined,
      month: filters.month || undefined
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `账单明细_${new Date().getTime()}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success(`已导出 ${count} 条`)
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    exporting.value = false
  }
}

const openStandard = async () => {
  try {
    standards.value = await getFeeStandards()
    standardVisible.value = true
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

const saveStandard = async () => {
  savingStandard.value = true
  try {
    await updateFeeStandards(standards.value.map((item) => ({ feeType: item.feeType, amount: item.amount })))
    ElMessage.success('保存成功')
    standardVisible.value = false
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    savingStandard.value = false
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

.filters {
  display: flex;
  gap: 10px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.standard-tip {
  font-size: 13px;
  color: #909399;
  margin-bottom: 14px;
}

.standard-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}
</style>
