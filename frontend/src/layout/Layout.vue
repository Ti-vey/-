<template>
  <el-container class="layout">
    <!-- 左侧菜单 -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-title">仓库库存管理系统</span>
        <span class="logo-sub">INVENTORY</span>
      </div>
      <el-menu :default-active="route.path" router background-color="#FFFFFF" text-color="#4A4F52"
               active-text-color="#111111">
        <el-sub-menu v-for="group in menuGroups" :key="group.name" :index="group.name">
          <template #title>
            <el-icon><Menu /></el-icon>
            <span>{{ group.name }}</span>
          </template>
          <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-title">{{ route.meta.title || '首页' }}</div>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="30">{{ (userStore.realName || 'U').charAt(0) }}</el-avatar>
            <span class="user-name">{{ userStore.realName }}</span>
            <el-tag size="small" type="info">{{ userStore.userInfo.roleName }}</el-tag>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import router from '@/router'

const route = useRoute()
const routerInstance = useRouter()
const userStore = useUserStore()

/**
 * 根据路由配置自动生成分组菜单，避免菜单与路由两处维护。
 * 用户管理仅对系统管理员可见。
 */
const menuGroups = computed(() => {
  const root = router.options.routes.find((item) => item.path === '/')
  const groups = []
  ;(root?.children || []).forEach((item) => {
    if (item.path === 'user' && !userStore.isAdmin) {
      return
    }
    const groupName = item.meta?.group || '其他'
    let group = groups.find((g) => g.name === groupName)
    if (!group) {
      group = { name: groupName, items: [] }
      groups.push(group)
    }
    group.items.push({
      path: `/${item.path}`,
      title: item.meta?.title,
      icon: item.meta?.icon
    })
  })
  return groups
})

const handleCommand = async (command) => {
  if (command !== 'logout') {
    return
  }
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  try {
    await authApi.logout()
  } catch (e) {
    // 退出接口失败不影响本地清理
  }
  userStore.logout()
  routerInstance.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background-color: var(--ui-surface);
  border-right: 1px solid var(--ui-border);
  overflow-y: auto;
}

.logo {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 64px;
  padding: 0 20px;
  background-color: var(--ui-surface);
  border-bottom: 1px solid var(--ui-border);
}

/* 标题 + 等宽英文副标，拉开字号与字重层级 */
.logo-title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.3;
  letter-spacing: -0.01em;
  color: var(--ui-text-strong);
}

.logo-sub {
  margin-top: 2px;
  font-family: var(--ui-font-mono);
  font-size: 10px;
  letter-spacing: 0.14em;
  color: var(--ui-text-muted);
}

.aside :deep(.el-menu) {
  border-right: none;
  background-color: transparent;
}

.aside :deep(.el-sub-menu__title),
.aside :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  font-size: 13px;
}

.aside :deep(.el-menu-item:hover),
.aside :deep(.el-sub-menu__title:hover) {
  background-color: #F4F3F0 !important;
}

/* 选中态使用中性暖灰，而不是彩色高亮 */
.aside :deep(.el-menu-item.is-active) {
  background-color: #F0EFEC !important;
  font-weight: 600;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: var(--ui-surface);
  border-bottom: 1px solid var(--ui-border);
  box-shadow: none;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--ui-text-strong);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.user-name {
  font-size: 13px;
  color: var(--ui-text);
}

.user-info :deep(.el-avatar) {
  background-color: #F0EFEC;
  color: var(--ui-text-strong);
  font-weight: 600;
}

.main {
  padding: 0;
  background: var(--ui-canvas);
  overflow-y: auto;
}
</style>
