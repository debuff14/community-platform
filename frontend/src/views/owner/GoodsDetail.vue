<template>
  <el-card v-loading="loading" shadow="never" class="detail-card">
    <template v-if="goods">
      <div class="back">
        <el-button link @click="router.push('/market')">返回市场</el-button>
      </div>
      <div class="content">
        <el-image :src="goods.image" fit="cover" class="image" :preview-src-list="[goods.image]" />
        <div class="info">
          <h2 class="name">{{ goods.name }}</h2>
          <div class="price">¥ {{ Number(goods.price).toFixed(2) }}</div>
          <div class="row"><span class="label">分类</span><el-tag size="small">{{ goodsCategory(goods.category) }}</el-tag></div>
          <div class="row"><span class="label">卖家</span>{{ goods.building }}{{ goods.roomNo }}</div>
          <div class="row"><span class="label">发布时间</span>{{ goods.createTime }}</div>
          <div v-if="goods.description" class="desc">{{ goods.description }}</div>
          <el-button v-if="goods.status === 1" type="primary" size="large" class="want-btn" @click="onWant">
            我想要
          </el-button>
        </div>
      </div>

      <el-dialog v-model="contactVisible" title="联系卖家" width="360px">
        <div class="contact">卖家手机号：<b>{{ phone }}</b></div>
        <div class="contact-tip">请电话联系卖家，平台暂不提供站内聊天</div>
      </el-dialog>
    </template>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMarketDetail, getSellerContact } from '@/api/goods'
import { goodsCategory } from '@/utils/goods'

const route = useRoute()
const router = useRouter()

const goods = ref(null)
const loading = ref(false)
const contactVisible = ref(false)
const phone = ref('')

const onWant = async () => {
  try {
    phone.value = await getSellerContact(goods.value.id)
    contactVisible.value = true
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

onMounted(async () => {
  loading.value = true
  try {
    goods.value = await getMarketDetail(route.params.id)
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

.back {
  margin-bottom: 12px;
}

.content {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}

.image {
  width: 320px;
  height: 320px;
  border-radius: 10px;
}

.info {
  flex: 1;
  min-width: 260px;
}

.name {
  margin: 0 0 10px;
  font-size: 22px;
}

.price {
  color: #f56c6c;
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 16px;
}

.row {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.label {
  display: inline-block;
  width: 70px;
  color: #909399;
}

.desc {
  margin-top: 16px;
  padding: 12px;
  background: #f8f9fb;
  border-radius: 8px;
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
}

.want-btn {
  margin-top: 20px;
}

.contact {
  font-size: 16px;
  color: #303133;
}

.contact-tip {
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}
</style>
