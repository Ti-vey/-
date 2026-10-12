<template>
  <div class="login-page">
    <div class="login-box ui-rise">
      <div class="login-head">
        <h1 class="login-title">仓库库存管理系统</h1>
        <p class="login-subtitle">Warehouse Inventory Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入登录密码"
                    :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="demo-tip">
        <div class="tip-label">演示账号，初始密码均为 123456</div>
        <div class="tip-row">系统管理员　admin</div>
        <div class="tip-row">仓库主管　　manager</div>
        <div class="tip-row">仓管员　　　keeper</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
}

/** 提交登录：成功后保存令牌并跳转首页 */
const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await authApi.login(form)
    userStore.setLoginInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: var(--ui-canvas);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-card);
  box-shadow: none;
}

/* 标题区用 1px 细线与表单分隔，形成文档式结构 */
.login-head {
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--ui-border);
}

.login-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.25;
  letter-spacing: -0.02em;
  color: var(--ui-text-strong);
}

.login-subtitle {
  margin: 10px 0 0;
  font-family: var(--ui-font-mono);
  font-size: 11px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--ui-text-muted);
}

.login-box :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-box :deep(.el-button) {
  height: 40px;
  letter-spacing: 0.1em;
}

.demo-tip {
  margin-top: 4px;
  padding-top: 20px;
  border-top: 1px solid var(--ui-border);
  font-size: 12px;
  color: var(--ui-text-muted);
}

.tip-label {
  margin-bottom: 8px;
  letter-spacing: 0.02em;
}

/* 账号名用等宽字体，便于逐行对齐阅读 */
.tip-row {
  font-family: var(--ui-font-mono);
  font-size: 11px;
  line-height: 1.9;
  color: var(--ui-text);
}
</style>
