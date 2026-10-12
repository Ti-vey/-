<template>
  <crud-table title="物料分类" :api="categoryApi" :columns="columns" :search-fields="searchFields"
              :form-fields="formFields" />
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import CrudTable from '@/components/CrudTable.vue'
import { categoryApi } from '@/api'

const columns = [
  { prop: 'categoryCode', label: '分类编码', width: 140 },
  { prop: 'categoryName', label: '分类名称', width: 160 },
  { prop: 'parentId', label: '上级分类', width: 120, formatter: (row) => (row.parentId === 0 ? '顶级分类' : row.parentId) },
  { prop: 'sortNo', label: '排序号', width: 100 },
  { prop: 'remark', label: '备注' }
]

const searchFields = [{ prop: 'keyword', label: '分类名称' }]

const formFields = reactive([
  { prop: 'categoryCode', label: '分类编码', required: true },
  { prop: 'categoryName', label: '分类名称', required: true },
  { prop: 'parentId', label: '上级分类', type: 'select', options: [{ label: '顶级分类', value: 0 }], defaultValue: 0 },
  { prop: 'sortNo', label: '排序号', type: 'number', defaultValue: 0 },
  { prop: 'remark', label: '备注', type: 'textarea' }
])

// 加载分类列表作为上级分类下拉选项
onMounted(async () => {
  const res = await categoryApi.list()
  const options = [{ label: '顶级分类', value: 0 }]
  ;(res.data || []).forEach((item) => options.push({ label: item.categoryName, value: item.id }))
  formFields[2].options = options
})
</script>
