<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="title">我的商品</span>
      <el-button type="primary" :icon="Plus" @click="router.push('/market/publish')">发布商品</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <el-image :src="row.image" fit="cover" class="thumb" :preview-src-list="[row.image]" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="价格" width="110">
        <template #default="{ row }">
          <span class="price">¥ {{ Number(row.price).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="100">
        <template #default="{ row }">{{ goodsCategory(row.category) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="goodsStatus(row.status).type" size="small">{{ goodsStatus(row.status).text }}</el-tag>
          <div v-if="row.status === 3 && row.rejectReason" class="reject-reason">{{ row.rejectReason }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0 || row.status === 3" link type="primary" @click="edit(row)">编辑</el-button>
          <el-button v-if="row.status === 1" link type="danger" @click="onOffShelf(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="还没有发布商品，点击“发布商品”试试" />

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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMyGoodsPage, offShelfGoods } from '@/api/goods'
import { goodsCategory, goodsStatus } from '@/utils/goods'

const router = useRouter()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const data = await getMyGoodsPage({ page: page.value, size: size.value })
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

const edit = (row) => {
  router.push({ path: '/market/publish', query: { id: row.id } })
}

const onOffShelf = async (row) => {
  try {
    await ElMessageBox.confirm('下架后买家将看不到该商品，确定下架？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await offShelfGoods(row.id)
    ElMessage.success('已下架')
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
