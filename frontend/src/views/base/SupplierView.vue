<template>
  <crud-table title="供应商管理" :api="supplierApi" :columns="columns" :search-fields="searchFields"
              :form-fields="formFields" />
</template>

<script setup>
import CrudTable from '@/components/CrudTable.vue'
import { supplierApi } from '@/api'

const columns = [
  { prop: 'supplierCode', label: '供应商编码', width: 130 },
  { prop: 'supplierName', label: '供应商名称', width: 200 },
  { prop: 'contactPerson', label: '联系人', width: 110 },
  { prop: 'phone', label: '联系电话', width: 140 },
  { prop: 'address', label: '地址' },
  { prop: 'status', label: '状态', width: 90, tagMap: { 1: { label: '合作中', type: 'success' }, 0: { label: '已停用', type: 'info' } } }
]

const searchFields = [{ prop: 'keyword', label: '供应商名称' }]

const formFields = [
  { prop: 'supplierCode', label: '供应商编码', required: true },
  { prop: 'supplierName', label: '供应商名称', required: true },
  { prop: 'contactPerson', label: '联系人' },
  { prop: 'phone', label: '联系电话' },
  { prop: 'email', label: '电子邮箱' },
  { prop: 'address', label: '地址' },
  { prop: 'status', label: '状态', type: 'select', options: [{ label: '合作中', value: 1 }, { label: '已停用', value: 0 }], defaultValue: 1 },
  { prop: 'remark', label: '备注', type: 'textarea' }
]
</script>
