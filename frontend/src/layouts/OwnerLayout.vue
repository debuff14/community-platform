<template>
  <el-container class="owner-layout">
    <el-header class="header" height="60px">
      <div class="header-inner">
        <div class="brand" @click="router.push('/notice')">
          <el-icon :size="26" color="#409EFF"><HomeFilled /></el-icon>
          <span class="brand-name">云上家园</span>
        </div>
        <el-menu class="nav-menu" mode="horizontal" :default-active="activeMenu" :ellipsis="false" router>
          <el-menu-item index="/notice">公告</el-menu-item>
          <el-menu-item index="/pay">我的缴费</el-menu-item>
          <el-menu-item index="/repair">报修服务</el-menu-item>
          <el-menu-item index="/market">二手市场</el-menu-item>
          <el-menu-item index="/visitor">访客登记</el-menu-item>
          <el-menu-item index="/ai">AI 客服</el-menu-item>
        </el-menu>
        <el-popover placement="bottom-end" :width="360" trigger="click" @show="loadMessages">
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="bell-badge">
              <el-icon :size="20" class="bell-icon"><Bell /></el-icon>
            </el-badge>
          </template>
          <div class="message-panel">
            <div class="message-header">
              <span>消息通知</span>
              <el-button link type="primary" size="small" :disabled="unreadCount === 0" @click="onReadAll">
                全部已读
              </el-button>
            </div>
            <el-scrollbar max-height="320px">
              <div v-if="messages.length === 0" class="message-empty">暂无消息</div>
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="message-item"
                :class="{ unread: msg.isRead === 0 }"
                @click="onMessageClick(msg)"
              >
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-time">{{ msg.createTime }}</div>
              </div>
            </el-scrollbar>
          </div>
        </el-popover>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-entry">
            <el-avatar :size="32" class="avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ store.userInfo.username }}</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="main">
      <div class="page-container">
        <router-view />
      </div>
    </el-main>

    <el-dialog
      v-model="pwdVisible"
      title="修改密码"
      width="420px"
      :close-on-click-modal="false"
      @closed="resetPwdForm"
    >
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="86px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { UserFilled, ArrowDown, Bell } from '@element-plus/icons-vue'
import { changePassword } from '@/api/user'
import { getMessagePage, getUnreadCount, markAllRead } from '@/api/message'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const activeMenu = computed(() => (route.path.startsWith('/notice') ? '/notice' : route.path))

const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请再次输入新密码'))
  }
  if (value !== pwdForm.newPassword) {
    return callback(new Error('两次输入的密码不一致'))
  }
  callback()
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }]
}

const resetPwdForm = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

const submitPwd = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await changePassword(pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
    pwdVisible.value = false
    store.logout()
    router.push('/login')
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    pwdLoading.value = false
  }
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'password') {
    pwdVisible.value = true
    return
  }
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '退出',
        cancelButtonText: '取消'
      })
      store.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (e) {
      /* 用户取消 */
    }
  }
}

const unreadCount = ref(0)
const messages = ref([])
let socket = null
let reconnectTimer = null

const loadUnread = async () => {
  try {
    unreadCount.value = await getUnreadCount()
  } catch (e) {
    /* 忽略 */
  }
}

const loadMessages = async () => {
  try {
    const data = await getMessagePage({ page: 1, size: 20 })
    messages.value = data.records
  } catch (e) {
    /* 忽略 */
  }
}

const onReadAll = async () => {
  try {
    await markAllRead()
    unreadCount.value = 0
    messages.value = messages.value.map((msg) => ({ ...msg, isRead: 1 }))
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
}

const onMessageClick = (msg) => {
  if (msg.type === 1 && msg.relatedId) {
    router.push(`/repair/${msg.relatedId}`)
  }
}

const connectSocket = () => {
  const token = store.token
  if (!token) return
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(`${protocol}://${window.location.host}/api/ws?token=${encodeURIComponent(token)}`)
  socket.onopen = () => {
    loadUnread()
  }
  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (typeof data.unreadCount === 'number') {
        unreadCount.value = data.unreadCount
      }
      if (data.content) {
        ElNotification({ title: '新的站内消息', message: data.content, type: 'info', duration: 4000 })
      }
    } catch (e) {
      /* 忽略非 JSON 消息 */
    }
  }
  socket.onclose = () => {
    scheduleReconnect()
  }
  socket.onerror = () => {
    socket?.close()
  }
}

const scheduleReconnect = () => {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectSocket()
  }, 3000)
}

onMounted(() => {
  loadUnread()
  connectSocket()
})

onBeforeUnmount(() => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
  socket?.close()
})
</script>

<style scoped>
.owner-layout {
  min-height: 100%;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-inner {
  max-width: 1200px;
  height: 60px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  height: 60px;
}

.nav-menu :deep(.el-menu-item) {
  height: 60px;
  font-size: 15px;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  flex-shrink: 0;
}

.avatar {
  background: #ecf5ff;
  color: #409eff;
}

.username {
  font-size: 14px;
  color: #303133;
}

.arrow {
  color: #909399;
}

.main {
  padding: 20px 16px;
}

.bell-badge {
  cursor: pointer;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.bell-icon {
  color: #606266;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
}

.message-empty {
  text-align: center;
  color: #909399;
  padding: 24px 0;
  font-size: 13px;
}

.message-item {
  padding: 10px 8px;
  border-radius: 6px;
  cursor: pointer;
}

.message-item:hover {
  background: #f5f7fa;
}

.message-item.unread .message-content {
  font-weight: 600;
}

.message-content {
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
