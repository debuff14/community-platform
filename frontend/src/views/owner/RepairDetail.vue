<template>
  <el-card v-loading="loading" shadow="never" class="detail-card">
    <template v-if="order">
      <div class="header">
        <div class="title">
          <el-tag :type="repairStatus(order.status).type" :effect="repairStatus(order.status).effect || 'light'">
            {{ repairStatus(order.status).text }}
          </el-tag>
          <el-tag v-if="order.urgency === 1" type="danger" effect="dark">紧急</el-tag>
          <span class="type">{{ repairTypeName(order.type) }}</span>
        </div>
        <el-button link @click="router.push('/repair')">返回列表</el-button>
      </div>

      <el-descriptions :column="1" border class="desc">
        <el-descriptions-item label="问题描述">{{ order.description }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="order.acceptTime" label="接单时间">{{ order.acceptTime }}</el-descriptions-item>
        <el-descriptions-item v-if="order.finishTime" label="完成时间">{{ order.finishTime }}</el-descriptions-item>
        <el-descriptions-item v-if="order.result" label="处理结果">{{ order.result }}</el-descriptions-item>
        <el-descriptions-item v-if="order.cancelTime" label="取消时间">{{ order.cancelTime }}</el-descriptions-item>
        <el-descriptions-item v-if="images.length" label="现场照片">
          <div class="images">
            <el-image
              v-for="(img, index) in images"
              :key="index"
              :src="img"
              :preview-src-list="images"
              :initial-index="index"
              fit="cover"
              class="image"
            />
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="order.star" class="comment">
        <div class="comment-title">我的评价</div>
        <el-rate :model-value="order.star" disabled />
        <div v-if="order.commentContent" class="comment-content">{{ order.commentContent }}</div>
      </div>

      <div class="actions">
        <el-button v-if="order.status === 3" type="warning" @click="openEvaluate">去评价</el-button>
        <el-button v-if="order.status === 0" type="danger" plain @click="onCancel">取消报修</el-button>
      </div>
    </template>

    <el-dialog v-model="evaluateVisible" title="评价报修" width="440px" :close-on-click-modal="false">
      <div class="evaluate-row">
        <span>服务评分</span>
        <el-rate v-model="rating" />
      </div>
      <el-input v-model="commentContent" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="说说这次的维修体验（选填）" />
      <template #footer>
        <el-button :disabled="evaluating" @click="evaluateVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluating" @click="submitEvaluate">提交评价</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelRepair, evaluateRepair, getRepairDetail } from '@/api/repair'
import { repairImages, repairStatus, repairTypeName } from '@/utils/repair'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(false)

const evaluateVisible = ref(false)
const evaluating = ref(false)
const rating = ref(5)
const commentContent = ref('')

const images = computed(() => (order.value ? repairImages(order.value.images) : []))

const load = async () => {
  loading.value = true
  try {
    order.value = await getRepairDetail(route.params.id)
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}

const openEvaluate = () => {
  rating.value = 5
  commentContent.value = ''
  evaluateVisible.value = true
}

const submitEvaluate = async () => {
  evaluating.value = true
  try {
    await evaluateRepair(order.value.id, { star: rating.value, content: commentContent.value })
    ElMessage.success('评价成功')
    evaluateVisible.value = false
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    evaluating.value = false
  }
}

const onCancel = async () => {
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
    await cancelRepair(order.value.id)
    ElMessage.success('已取消报修')
    load()
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

onMounted(load)
</script>

<style scoped>
.detail-card {
  min-height: 300px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.type {
  font-size: 16px;
  font-weight: 600;
}

.desc {
  max-width: 720px;
}

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image {
  width: 110px;
  height: 110px;
  border-radius: 6px;
  cursor: pointer;
}

.comment {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fb;
  border-radius: 8px;
  max-width: 720px;
}

.comment-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.comment-content {
  margin-top: 8px;
  color: #606266;
}

.actions {
  margin-top: 24px;
}

.evaluate-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
</style>
