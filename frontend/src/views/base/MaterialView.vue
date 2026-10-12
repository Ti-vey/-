<template>
  <crud-table title="物料档案" :api="materialApi" :columns="columns" :search-fields="searchFields"
              :form-fields="formFields" />
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import CrudTable from '@/components/CrudTable.vue'
import { categoryApi, materialApi, unitApi } from '@/api'

const columns = [
  { prop: 'materialCode', label: '物料编码', width: 110 },
  { prop: 'materialName', label: '物料名称', width: 160 },
  { prop: 'spec', label: '规格', width: 130 },
  { prop: 'categoryId', label: '分类', width: 100 },
  { prop: 'safetyStock', label: '安全库存', width: 100, align: 'right' },
  { prop: 'purchasePrice', label: '采购单价', width: 100, align: 'right' },
  { prop: 'abcType', label: 'ABC分类', width: 100, align: 'center' },
  { prop: 'status', label: '状态', width: 90, tagMap: { 1: { label: '启用', type: 'success' }, 0: { label: '停用', type: 'info' } } }
]

const searchFields = [
  { prop: 'keyword', label: '物料名称/编码' },
  {
    prop: 'abcType',
    label: 'ABC分类',
    type: 'select',
    options: [
      { label: 'A 类（重点管理）', value: 'A' },
      { label: 'B 类（常规管理）', value: 'B' },
      { label: 'C 类（简化管理）', value: 'C' }
    ]
  }
]

const formFields = reactive([
  { prop: 'materialCode', label: '物料编码', required: true },
  { prop: 'materialName', label: '物料名称', required: true },
  { prop: 'categoryId', label: '所属分类', type: 'select', options: [], required: true },
  { prop: 'unitId', label: '计量单位', type: 'select', options: [], required: true },
  { prop: 'spec', label: '规格' },
  { prop: 'model', label: '型号' },
  { prop: 'brand', label: '品牌' },
  { prop: 'safetyStock', label: '安全库存', type: 'number', precision: 2, defaultValue: 0 },
  { prop: 'maxStock', label: '库存上限', type: 'number', precision: 2, defaultValue: 0 },
  { prop: 'purchasePrice', label: '采购单价', type: 'number', precision: 2, defaultValue: 0 },
  { prop: 'salePrice', label: '出库单价', type: 'number', precision: 2, defaultValue: 0 },
  { prop: 'shelfLifeDays', label: '保质期(天)', type: 'number', defaultValue: 0 },
  {
    prop: 'abcType',
    label: 'ABC分类',
    type: 'select',
    options: [
      { label: 'A 类', value: 'A' },
      { label: 'B 类', value: 'B' },
      { label: 'C 类', value: 'C' }
    ],
    defaultValue: 'C'
  },
  { prop: 'status', label: '状态', type: 'select', options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }], defaultValue: 1 },
  { prop: 'remark', label: '备注', type: 'textarea' }
])

// 加载分类与单位下拉选项
onMounted(async () => {
  const [categories, units] = await Promise.all([categoryApi.list(), unitApi.list()])
  formFields[2].options = (categories.data || []).map((item) => ({ label: item.categoryName, value: item.id }))
  formFields[3].options = (units.data || []).map((item) => ({ label: item.unitName, value: item.id }))
})
</script>
