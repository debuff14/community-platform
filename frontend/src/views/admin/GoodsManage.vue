<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="onFilterChange">
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">在售</el-radio-button>
        <el-radio-button :value="3">已驳回</el-radio-button>
        <el-radio-button :value="2">已下架</el-radio-button>
        <el-radio-button :value="null">全部</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称"
        clearable
        style="width: 220px"
        @keyup.enter="onFilterChange"
        @clear="onFilterChange"
      />
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <el-image :src="row.image" fit="cover" class="thumb" :preview-src-list="[row.image]" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
      <el-table-column label="价格" width="110">
        <template #default="{ row }">
          <span class="price">¥ {{ Number(row.price).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="100">
        <template #default="{ row }">{{ goodsCategory(row.category) }}</template>
      </el-table-column>
      <el-table-column label="卖家" width="120">
        <template #default="{ row }">{{ row.building }}{{ row.roomNo }}</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="goodsStatus(row.status).type" size="small">{{ goodsStatus(row.status).text }}</el-tag>
          <div v-if="row.status === 3 && row.rejectReason" class="reject-reason">{{ row.rejectReason }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="onApprove(row)">通过</el-button>
            <el-button link type="danger" @click="openReject(row)">驳回</el-button>
          </template>
          <el-button v-if="row.status === 1" link type="danger" @click="onForceOffShelf(row)">强制下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无商品" />

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

    <el-dialog v-model="rejectVisible" title="驳回商品" width="460px" :close-on-click-modal="false">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        maxlength="255"
        show-word-limit
        placeholder="请填写驳回原因，例如：图片与实物不符，请重新上传"
      />
      <template #footer>
        <el-button :disabled="rejecting" @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!rejectReason.trim()" :loading="rejecting" @click="submitReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveGoods, forceOffShelfGoods, getAdminGoodsPage, rejectGoods } from '@/api/goods'
import { goodsCategory, goodsStatus } from '@/utils/goods'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref(0)
const keyword = ref('')
const loading = ref(false)

const rejectVisible = ref(false)
const rejecting = ref(false)
const rejectReason = ref('')
const currentGoods = ref(null)

const load = async () => {
  loading.value = true
  try {
    const data = await getAdminGoodsPage({
      page: page.value,
      size: size.value,
      status: statusFilter.value ?? undefined,
      keyword: keyword.value || undefined
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

const onApprove = async (row) => {
  try {
    await approveGoods(row.id)
    ElMessage.success('审核通过')
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

const openReject = (row) => {
  currentGoods.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  rejecting.value = true
  try {
    await rejectGoods(currentGoods.value.id, { reason: rejectReason.value })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    rejecting.value = false
  }
}

const onForceOffShelf = async (row) => {
  try {
    await ElMessageBox.confirm('确定强制下架该商品？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await forceOffShelfGoods(row.id)
    ElMessage.success('已强制下架')
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
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 6px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.reject-reason {
  margin-top: 4px;
  font-size: 12px;
  color: #f56c6c;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
