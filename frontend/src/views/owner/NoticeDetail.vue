<template>
  <el-card v-loading="loading" shadow="never" class="detail-card">
    <template v-if="notice">
      <h2 class="title">
        <el-tag v-if="notice.isTop === 1" type="danger" size="small" effect="dark" class="top-tag">置顶</el-tag>
        {{ notice.title }}
      </h2>
      <div class="time">发布时间：{{ notice.createTime }}</div>
      <el-divider />
      <div class="content">{{ notice.content }}</div>
      <div class="back">
        <el-button @click="router.push('/notice')">
          <el-icon class="btn-icon"><Back /></el-icon>
          返回列表
        </el-button>
      </div>
    </template>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Back } from '@element-plus/icons-vue'
import { getNoticeDetail } from '@/api/notice'

const route = useRoute()
const router = useRouter()
const notice = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    notice.value = await getNoticeDetail(route.params.id)
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-card {
  min-height: 300px;
}

.title {
  margin: 0 0 10px;
  font-size: 22px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.top-tag {
  flex-shrink: 0;
}

.time {
  font-size: 13px;
  color: #909399;
}

.content {
  font-size: 15px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
  min-height: 120px;
}

.back {
  margin-top: 30px;
}

.btn-icon {
  margin-right: 4px;
}
</style>
