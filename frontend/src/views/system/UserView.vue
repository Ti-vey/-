<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="账号/姓名/手机号" clearable style="width: 220px"
                @keyup.enter="handleSearch" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">用户管理</span>
        <div class="toolbar-actions">
          <span class="meta">共 {{ total }} 个账号</span>
          <el-button type="primary" :icon="Plus" @click="openDialog()">新增用户</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无用户">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="登录账号" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="roleName" label="角色" width="130" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button link :type="row.status === 1 ? 'info' : 'success'"
                       @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" placeholder="默认 123456" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" style="width: 100%" placeholder="请选择角色">
            <el-option v-for="item in roles" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { userApi } from '@/api'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const roles = ref([])
const total = ref(0)
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, keyword: undefined, status: undefined })
const form = reactive({})

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await userApi.page(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadData()
}

const openDialog = (row) => {
  Object.keys(form).forEach((key) => delete form[key])
  if (row) {
    Object.assign(form, {
      id: row.id,
      username: row.username,
      realName: row.realName,
      roleId: row.roleId,
      phone: row.phone,
      email: row.email,
      status: row.status
    })
  } else {
    Object.assign(form, { password: '123456', status: 1 })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const { roleId, ...payload } = form
    if (form.id) {
      await userApi.update(payload, roleId)
    } else {
      await userApi.save(payload, roleId)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleResetPassword = async (row) => {
  await ElMessageBox.confirm(`确定将「${row.realName}」的密码重置为 123456 吗？`, '提示', { type: 'warning' })
  await userApi.resetPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

const handleToggleStatus = async (row) => {
  const status = row.status === 1 ? 0 : 1
  await userApi.changeStatus(row.id, status)
  ElMessage.success(status === 1 ? '账号已启用' : '账号已禁用')
  loadData()
}

onMounted(async () => {
  const res = await userApi.roles()
  roles.value = res.data || []
  loadData()
})
</script>
