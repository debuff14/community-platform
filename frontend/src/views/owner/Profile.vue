<template>
  <div class="profile">
    <el-row :gutter="16" class="stats">
      <el-col :span="12">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">本月应缴金额</div>
          <div class="stat-value">¥ {{ Number(stats.unpaidAmount ?? 0).toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">未处理报修数量</div>
          <div class="stat-value">{{ stats.unhandledRepairCount ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" v-loading="loading">
      <template #header>基本资料</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="76px" class="info-form">
        <el-form-item label="用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="楼栋号">
          <el-input :model-value="form.building" disabled />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="请输入房号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, getStats, updateProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const loading = ref(false)
const saving = ref(false)
const formRef = ref()

const stats = reactive({
  unpaidAmount: 0,
  unhandledRepairCount: 0
})

const form = reactive({
  username: '',
  building: '',
  phone: '',
  roomNo: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const [profile, stat] = await Promise.all([getProfile(), getStats()])
    form.username = profile.username
    form.building = profile.building
    form.phone = profile.phone
    form.roomNo = profile.roomNo
    stats.unpaidAmount = stat.unpaidAmount
    stats.unhandledRepairCount = stat.unhandledRepairCount
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}

const save = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateProfile({ phone: form.phone, roomNo: form.roomNo })
    store.setUserInfo({ phone: form.phone, roomNo: form.roomNo })
    ElMessage.success('保存成功')
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.stats {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
  color: #409eff;
}

.info-form {
  max-width: 420px;
}
</style>
