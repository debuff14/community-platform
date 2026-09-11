<template>
  <div class="notice-list" v-loading="loading">
    <el-card
      v-for="item in items"
      :key="item.id"
      shadow="hover"
      class="notice-card"
      @click="goDetail(item.id)"
    >
      <div class="notice-head">
        <span class="notice-title">
          <el-tag v-if="item.isTop === 1" type="danger" size="small" effect="dark" class="top-tag">置顶</el-tag>
          {{ item.title }}
        </span>
        <span class="notice-time">{{ item.createTime }}</span>
      </div>
    </el-card>

    <el-empty v-if="!loading && items.length === 0" description="暂无公告，物业发布后会显示在这里" />

    <div class="pagination" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNoticePage } from '@/api/notice'

const router = useRouter()
const items = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(6)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const data = await getNoticePage({ page: page.value, size: size.value })
    items.value = data.records
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
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goDetail = (id) => {
  router.push(`/notice/${id}`)
}

onMounted(load)
</script>

<style scoped>
.notice-list {
  min-height: 300px;
}

.notice-card {
  margin-bottom: 14px;
  cursor: pointer;
}

.notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.notice-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-tag {
  flex-shrink: 0;
}

.notice-time {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
