<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <el-icon :size="36" color="#409EFF"><HomeFilled /></el-icon>
        <h1>注册业主账号</h1>
        <p>加入云上家园，享受便捷社区服务</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-20位字母/数字/下划线）" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Iphone" clearable />
        </el-form-item>
        <el-form-item prop="building">
          <el-select v-model="form.building" placeholder="请选择楼栋号" style="width: 100%">
            <el-option v-for="b in buildings" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="房号（如 502）" :prefix-icon="OfficeBuilding" clearable />
        </el-form-item>
        <el-button type="primary" class="submit-btn" size="large" :loading="loading" :disabled="!canSubmit" @click="onSubmit">
          注 册
        </el-button>
      </el-form>
      <div class="auth-footer">
        已有账号？
        <router-link class="link" to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, OfficeBuilding } from '@element-plus/icons-vue'
import { checkUsername, register } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const usernameChecking = ref(false)
const usernameTaken = ref(false)

const buildings = Array.from({ length: 10 }, (_, i) => `${i + 1}栋`)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  building: '',
  roomNo: ''
})

const validateUsername = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入用户名'))
  }
  if (!/^[a-zA-Z0-9_]{3,20}$/.test(value)) {
    return callback(new Error('用户名需为3-20位字母、数字或下划线'))
  }
  usernameChecking.value = true
  try {
    const available = await checkUsername(value)
    usernameTaken.value = !available
    if (!available) {
      callback(new Error('该用户名已被占用'))
    } else {
      callback()
    }
  } catch (e) {
    callback()
  } finally {
    usernameChecking.value = false
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请再次输入密码'))
  }
  if (value !== form.password) {
    return callback(new Error('两次输入的密码不一致'))
  }
  callback()
}

const rules = {
  username: [{ validator: validateUsername, trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  building: [{ required: true, message: '请选择楼栋号', trigger: 'change' }],
  roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' }]
}

const canSubmit = computed(() => {
  return (
    form.username &&
    form.password &&
    form.confirmPassword &&
    form.phone &&
    form.building &&
    form.roomNo &&
    !usernameTaken.value &&
    !usernameChecking.value &&
    form.password === form.confirmPassword &&
    /^1[3-9]\d{9}$/.test(form.phone)
  )
})

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  background: linear-gradient(160deg, #e8f3ff 0%, #f5f7fa 60%);
}

.auth-card {
  width: 420px;
  padding: 36px 36px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(64, 158, 255, 0.12);
}

.auth-brand {
  text-align: center;
  margin-bottom: 24px;
}

.auth-brand h1 {
  margin: 10px 0 4px;
  font-size: 22px;
  color: #303133;
}

.auth-brand p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.submit-btn {
  width: 100%;
  margin-top: 4px;
}

.auth-footer {
  margin-top: 18px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.link {
  color: #409eff;
}
</style>
