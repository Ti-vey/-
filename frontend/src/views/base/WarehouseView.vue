<template>
  <crud-table title="仓库管理" :api="warehouseApi" :columns="columns" :search-fields="searchFields"
              :form-fields="formFields" />
</template>

<script setup>
import CrudTable from '@/components/CrudTable.vue'
import { warehouseApi } from '@/api'

const columns = [
  { prop: 'warehouseCode', label: '仓库编码', width: 130 },
  { prop: 'warehouseName', label: '仓库名称', width: 150 },
  { prop: 'address', label: '仓库地址' },
  { prop: 'manager', label: '负责人', width: 110 },
  { prop: 'phone', label: '联系电话', width: 140 },
  { prop: 'status', label: '状态', width: 90, tagMap: { 1: { label: '启用', type: 'success' }, 0: { label: '停用', type: 'info' } } }
]

const searchFields = [{ prop: 'keyword', label: '仓库名称' }]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const formFields = [
  { prop: 'warehouseCode', label: '仓库编码', required: true },
  { prop: 'warehouseName', label: '仓库名称', required: true },
  { prop: 'address', label: '仓库地址' },
  { prop: 'manager', label: '负责人' },
  { prop: 'phone', label: '联系电话' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, defaultValue: 1 },
  { prop: 'remark', label: '备注', type: 'textarea' }
]
</script>
