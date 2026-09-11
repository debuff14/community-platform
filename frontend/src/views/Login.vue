<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <el-icon :size="36" color="#409EFF"><HomeFilled /></el-icon>
        <h1>云上家园</h1>
        <p>社区物业一体化服务平台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" class="submit-btn" size="large" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
      <div class="auth-footer">
        还没有账号？
        <router-link class="link" to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await login(form)
    store.setLogin(data)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (data.role === 1) {
      router.push(redirect.startsWith('/admin') ? redirect : '/admin')
    } else {
      router.push(redirect && !redirect.startsWith('/admin') ? redirect : '/')
    }
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #e8f3ff 0%, #f5f7fa 60%);
}

.auth-card {
  width: 400px;
  padding: 40px 36px 28px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(64, 158, 255, 0.12);
}

.auth-brand {
  text-align: center;
  margin-bottom: 28px;
}

.auth-brand h1 {
  margin: 10px 0 4px;
  font-size: 24px;
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
