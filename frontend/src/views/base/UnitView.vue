<template>
  <crud-table title="计量单位" :api="unitApi" :columns="columns" :search-fields="searchFields"
              :form-fields="formFields" />
</template>

<script setup>
import CrudTable from '@/components/CrudTable.vue'
import { unitApi } from '@/api'

const columns = [
  { prop: 'unitName', label: '单位名称', width: 160 },
  { prop: 'unitCode', label: '单位编码', width: 160 },
  { prop: 'remark', label: '备注' }
]

const searchFields = [{ prop: 'keyword', label: '单位名称' }]

const formFields = [
  { prop: 'unitName', label: '单位名称', required: true },
  { prop: 'unitCode', label: '单位编码', required: true }
]
</script>
