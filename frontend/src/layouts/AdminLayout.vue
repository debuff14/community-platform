<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <el-icon :size="24" color="#409EFF"><HomeFilled /></el-icon>
        <span class="brand-name">云上家园后台</span>
      </div>
      <el-menu class="menu" :default-active="route.path" router background-color="#ffffff" text-color="#303133" active-text-color="#409EFF">
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/notice">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/bill">
          <el-icon><Money /></el-icon>
          <span>缴费管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/repair">
          <el-icon><Tools /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/goods">
          <el-icon><Goods /></el-icon>
          <span>商品审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/visitor">
          <el-icon><User /></el-icon>
          <span>访客管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/knowledge">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 知识库</span>
        </el-menu-item>
        <el-menu-item index="/admin/log">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header" height="56px">
        <div class="header-title">{{ pageTitle }}</div>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-entry">
            <el-avatar :size="30" class="avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ store.userInfo.username }}</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const pageTitle = computed(() => route.meta.title || '管理后台')

const handleCommand = async (command) => {
  if (command !== 'logout') return
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
</script>

<style scoped>
.admin-layout {
  height: 100%;
}

.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 18px;
  border-bottom: 1px solid #e4e7ed;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
}

.menu {
  flex: 1;
  border-right: none;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
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
  background: #f5f7fa;
  padding: 20px;
}
</style>
