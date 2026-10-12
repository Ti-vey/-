<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <div class="search-bar">
      <template v-for="field in searchFields" :key="field.prop">
        <el-input v-if="!field.type || field.type === 'text'" v-model="query[field.prop]"
                  :placeholder="field.label" clearable style="width: 200px" @keyup.enter="handleSearch" />
        <el-select v-else-if="field.type === 'select'" v-model="query[field.prop]" :placeholder="field.label"
                   clearable style="width: 160px">
          <el-option v-for="opt in resolveOptions(field)" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </template>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">{{ title }}</span>
        <div class="toolbar-actions">
          <span class="meta">共 {{ total }} 条记录</span>
          <el-button v-if="showAdd" type="primary" :icon="Plus" @click="openDialog()">新增</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无数据">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label"
                         :width="col.width" :align="col.align || 'left'" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="col.tagMap" :type="col.tagMap[row[col.prop]]?.type || 'info'" size="small">
              {{ col.tagMap[row[col.prop]]?.label ?? row[col.prop] }}
            </el-tag>
            <span v-else>{{ formatValue(row, col) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该记录吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
                       @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <!-- 新增 / 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? `编辑${title}` : `新增${title}`" width="620px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item v-for="field in formFields" :key="field.prop" :label="field.label" :prop="field.prop">
          <el-input v-if="!field.type" v-model="form[field.prop]" :placeholder="`请输入${field.label}`" />
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="3" />
          <el-input-number v-else-if="field.type === 'number'" v-model="form[field.prop]" :min="0"
                           :precision="field.precision ?? 0" style="width: 100%" />
          <el-select v-else-if="field.type === 'select'" v-model="form[field.prop]" clearable
                     :placeholder="`请选择${field.label}`" style="width: 100%">
            <el-option v-for="opt in field.options || []" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
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
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

/**
 * 通用单表增删改查组件。
 *
 * 基础数据（物料分类、仓库、库位、供应商、客户、计量单位）的页面结构与交互完全一致，
 * 因此抽取为本组件，各页面只需传入列定义与接口对象即可，避免重复开发与维护。
 */
const props = defineProps({
  title: { type: String, required: true },
  api: { type: Object, required: true },
  columns: { type: Array, required: true },
  searchFields: { type: Array, default: () => [] },
  formFields: { type: Array, default: () => [] },
  showAdd: { type: Boolean, default: true }
})

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref()
const form = reactive({})
const query = reactive({ pageNum: 1, pageSize: 10 })

/** 校验规则由表单字段自动生成 */
const formRules = props.formFields.reduce((rules, field) => {
  if (field.required) {
    rules[field.prop] = [{ required: true, message: `请输入或选择${field.label}`, trigger: 'blur' }]
  }
  return rules
}, {})

const resolveOptions = (field) => field.options || []

const formatValue = (row, col) => {
  const value = row[col.prop]
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return col.formatter ? col.formatter(row) : value
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await props.api.page(query)
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

const handleReset = () => {
  props.searchFields.forEach((field) => {
    query[field.prop] = undefined
  })
  query.pageNum = 1
  loadData()
}

const openDialog = (row) => {
  Object.keys(form).forEach((key) => delete form[key])
  props.formFields.forEach((field) => {
    form[field.prop] = row ? row[field.prop] : field.defaultValue ?? undefined
  })
  if (row) {
    form.id = row.id
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await props.api.update({ ...form })
    } else {
      await props.api.save({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await props.api.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)

defineExpose({ loadData })
</script>

<style scoped>
/* 计数说明与操作按钮同排，形成「说明 + 动作」的右侧对齐结构 */
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}
</style>
