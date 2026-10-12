<template>
  <crud-table title="库位管理" :api="locationApi" :columns="columns" :search-fields="searchFields"
              :form-fields="formFields" />
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import CrudTable from '@/components/CrudTable.vue'
import { locationApi, warehouseApi } from '@/api'

const columns = [
  { prop: 'locationCode', label: '库位编码', width: 160 },
  { prop: 'locationName', label: '库位名称', width: 180 },
  { prop: 'warehouseId', label: '所属仓库', width: 140, formatter: (row) => row.warehouseId },
  { prop: 'status', label: '状态', width: 90, tagMap: { 1: { label: '可用', type: 'success' }, 0: { label: '停用', type: 'info' } } },
  { prop: 'remark', label: '备注' }
]

const searchFields = [{ prop: 'keyword', label: '库位编码' }]

const formFields = reactive([
  { prop: 'locationCode', label: '库位编码', required: true },
  { prop: 'locationName', label: '库位名称', required: true },
  { prop: 'warehouseId', label: '所属仓库', type: 'select', options: [], required: true },
  { prop: 'status', label: '状态', type: 'select', options: [{ label: '可用', value: 1 }, { label: '停用', value: 0 }], defaultValue: 1 },
  { prop: 'remark', label: '备注', type: 'textarea' }
])

onMounted(async () => {
  const res = await warehouseApi.list()
  formFields[2].options = (res.data || []).map((item) => ({ label: item.warehouseName, value: item.id }))
})
</script>
