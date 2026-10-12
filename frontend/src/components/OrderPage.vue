<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="query.keyword" :placeholder="`${config.title}单号`" clearable style="width: 220px"
                @keyup.enter="handleSearch" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 160px">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">{{ config.title }}</span>
        <div class="toolbar-actions">
          <span class="meta">共 {{ total }} 张单据</span>
          <el-button type="primary" :icon="Plus" @click="openForm()">新建单据</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无单据">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column :prop="config.noField" label="单据编号" width="200" />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row[config.typeField]) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="config.mode === 'transfer'" label="调拨方向" width="240">
          <template #default="{ row }">{{ row.fromWarehouseName }} → {{ row.toWarehouseName }}</template>
        </el-table-column>
        <template v-else>
          <el-table-column prop="warehouseName" label="仓库" width="130" />
          <el-table-column :prop="config.partyField" :label="config.partyLabel" width="180">
            <template #default="{ row }">{{ row[config.partyNameField] || '-' }}</template>
          </el-table-column>
        </template>
        <el-table-column v-if="config.showPrice" prop="totalAmount" label="总金额(元)" width="130" align="right" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status).type" size="small">{{ statusTag(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="制单人" width="110" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="310" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">
              {{ canEdit(row) ? '编辑' : '查看' }}
            </el-button>
            <el-button v-if="canSubmit(row)" link type="warning" @click="handleSubmit(row)">提交审核</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="openAudit(row)">审核</el-button>
            <el-button link type="info" @click="handlePrint(row)">打印</el-button>
            <el-button v-if="row.status === 0 || row.status === 3" link type="danger"
                       @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                       @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <!-- 单据编辑对话框 -->
    <el-dialog v-model="formVisible" :title="formTitle" width="1000px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" :disabled="readonly">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item :label="config.noLabel">
              <el-input v-model="form[config.noField]" placeholder="保存时自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单据类型">
              <el-select v-model="form[config.typeField]" style="width: 100%">
                <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item v-if="config.mode === 'transfer'" label="调出仓库" prop="fromWarehouseId">
              <el-select v-model="form.fromWarehouseId" style="width: 100%">
                <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item v-else label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" style="width: 100%">
                <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col v-if="config.mode === 'transfer'" :span="8">
            <el-form-item label="调入仓库" prop="toWarehouseId">
              <el-select v-model="form.toWarehouseId" style="width: 100%">
                <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-else :span="8">
            <el-form-item :label="config.partyLabel" :prop="config.partyField">
              <el-select v-model="form[config.partyField]" clearable style="width: 100%"
                         :placeholder="`请选择${config.partyLabel}`">
                <el-option v-for="item in parties" :key="item.id" :label="item[config.partyNameField]" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 明细表格 -->
      <el-divider content-position="left">单据明细</el-divider>
      <el-button v-if="!readonly" type="primary" size="small" :icon="Plus" @click="addItem">添加明细</el-button>
      <el-table :data="form.items" border style="margin-top: 10px">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="物料" min-width="230">
          <template #default="{ row }">
            <el-select v-model="row.materialId" filterable placeholder="请选择物料" :disabled="readonly"
                       style="width: 100%" @change="onMaterialChange(row)">
              <el-option v-for="item in materials" :key="item.id"
                         :label="`${item.materialCode} ${item.materialName}`" :value="item.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="0.01" :precision="2" :controls="false"
                             :disabled="readonly" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column v-if="config.showPrice" label="单价(元)" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :controls="false"
                             :disabled="readonly" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column v-if="config.showPrice" label="金额(元)" width="120" align="right">
          <template #default="{ row }">{{ itemAmount(row) }}</template>
        </el-table-column>
        <el-table-column label="批次号" width="150">
          <template #default="{ row }">
            <el-input v-model="row.batchNo" placeholder="默认 DEFAULT" :disabled="readonly" />
          </template>
        </el-table-column>
        <el-table-column v-if="config.mode === 'inbound'" label="有效期" width="160">
          <template #default="{ row }">
            <el-date-picker v-model="row.expireDate" type="date" value-format="YYYY-MM-DD" :disabled="readonly"
                            placeholder="选择日期" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column v-if="!readonly" label="操作" width="70" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" @click="form.items.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="config.showPrice" class="amount-line">
        <span class="amount-label">合计金额</span>
        <span class="amount-value">{{ totalAmount }} 元</span>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formVisible = false">关闭</el-button>
          <template v-if="!readonly">
            <el-button type="primary" :loading="saving" @click="handleSave(false)">保存草稿</el-button>
            <el-button type="success" :loading="saving" @click="handleSave(true)">保存并提交</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="单据审核" width="480px">
      <el-form label-width="90px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.pass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.remark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleAudit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 打印区域：平时隐藏，点击打印时输出 -->
    <order-print-sheet :order="printData" :mode="mode" />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import OrderPrintSheet from '@/components/OrderPrintSheet.vue'
import {
  customerApi,
  inboundApi,
  materialApi,
  outboundApi,
  supplierApi,
  transferApi,
  warehouseApi
} from '@/api'

/**
 * 出入库与调拨单据页面。
 * 三类单据的页面结构、状态流转与审核交互完全一致，差异仅在参与方（供应商/客户/仓库）与明细字段，
 * 因此用一个组件通过 mode 参数区分，降低维护成本。
 */
const props = defineProps({
  mode: { type: String, default: 'inbound' }
})

const configs = {
  inbound: {
    mode: 'inbound',
    title: '入库管理',
    noLabel: '入库单号',
    noField: 'inboundNo',
    typeField: 'inboundType',
    partyLabel: '供应商',
    partyField: 'supplierId',
    partyNameField: 'supplierName',
    showPrice: true,
    api: inboundApi
  },
  outbound: {
    mode: 'outbound',
    title: '出库管理',
    noLabel: '出库单号',
    noField: 'outboundNo',
    typeField: 'outboundType',
    partyLabel: '客户',
    partyField: 'customerId',
    partyNameField: 'customerName',
    showPrice: true,
    api: outboundApi
  },
  transfer: {
    mode: 'transfer',
    title: '库存调拨',
    noLabel: '调拨单号',
    noField: 'transferNo',
    typeField: 'transferType',
    partyLabel: '参与方',
    partyField: 'fromWarehouseId',
    partyNameField: 'fromWarehouseName',
    showPrice: false,
    api: transferApi
  }
}

const config = configs[props.mode]

const typeOptionMap = {
  inbound: [
    { label: '采购入库', value: 1 },
    { label: '退货入库', value: 2 },
    { label: '其他入库', value: 3 }
  ],
  outbound: [
    { label: '销售出库', value: 1 },
    { label: '领用出库', value: 2 },
    { label: '其他出库', value: 3 }
  ],
  transfer: [{ label: '仓库调拨', value: 1 }]
}

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '待审核', value: 1 },
  { label: props.mode === 'transfer' ? '已完成' : '已过账', value: 2 },
  { label: '已驳回', value: 3 }
]

const typeOptions = typeOptionMap[props.mode]

const loading = ref(false)
const route = useRoute()
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouses = ref([])
const parties = ref([])
const materials = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: undefined, status: undefined })

const formVisible = ref(false)
const auditVisible = ref(false)
/** 打印用单据数据 */
const printData = ref(null)
const readonly = ref(false)
const formRef = ref()
const form = reactive({ items: [] })
const auditForm = reactive({ id: null, pass: true, remark: '' })

const formRules = {
  warehouseId: [{ required: props.mode !== 'transfer', message: '请选择仓库', trigger: 'change' }],
  fromWarehouseId: [{ required: props.mode === 'transfer', message: '请选择调出仓库', trigger: 'change' }],
  toWarehouseId: [{ required: props.mode === 'transfer', message: '请选择调入仓库', trigger: 'change' }]
}

const formTitle = computed(() => {
  const action = form.id ? (readonly.value ? '查看' : '编辑') : '新建'
  return `${action}${config.title}单据`
})

const totalAmount = computed(() =>
  form.items.reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.unitPrice || 0), 0).toFixed(2)
)

const itemAmount = (row) => (Number(row.quantity || 0) * Number(row.unitPrice || 0)).toFixed(2)

const typeLabel = (value) => typeOptions.find((item) => item.value === value)?.label || '-'

const statusTag = (status) => {
  const map = {
    0: { label: '草稿', type: 'info' },
    1: { label: '待审核', type: 'warning' },
    2: { label: props.mode === 'transfer' ? '已完成' : '已过账', type: 'success' },
    3: { label: '已驳回', type: 'danger' }
  }
  return map[status] || { label: '未知', type: 'info' }
}

const canEdit = (row) => row.status === 0 || row.status === 3
const canSubmit = (row) => row.status === 0 || row.status === 3

const loadData = async () => {
  loading.value = true
  try {
    const res = await config.api.page(query)
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
  Object.assign(query, { pageNum: 1, keyword: undefined, status: undefined })
  loadData()
}

const addItem = () => {
  form.items.push({ materialId: undefined, quantity: 1, unitPrice: 0, batchNo: 'DEFAULT' })
}

/** 选择物料后自动带出参考单价 */
const onMaterialChange = (row) => {
  const material = materials.value.find((item) => item.id === row.materialId)
  if (material && !row.unitPrice) {
    row.unitPrice = props.mode === 'inbound' ? material.purchasePrice : material.salePrice
  }
}

const openForm = async (row) => {
  Object.keys(form).forEach((key) => delete form[key])
  form.items = []
  if (row) {
    const res = await config.api.detail(row.id)
    Object.assign(form, res.data)
    form.items = (res.data.items || []).map((item) => ({ ...item }))
    readonly.value = !canEdit(row)
  } else {
    form[config.typeField] = 1
    form.remark = ''
    addItem()
    readonly.value = false
  }
  formVisible.value = true
}

const handleSave = async (submitAfterSave) => {
  if (!form.items.length) {
    ElMessage.warning('请至少添加一条明细')
    return
  }
  if (form.items.some((item) => !item.materialId || !item.quantity)) {
    ElMessage.warning('明细中的物料与数量不能为空')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    const res = form.id ? await config.api.update(payload) : await config.api.save(payload)
    const id = res.data
    if (submitAfterSave) {
      await config.api.submit(id)
      ElMessage.success('已保存并提交审核')
    } else {
      ElMessage.success('保存成功')
    }
    formVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleSubmit = async (row) => {
  await config.api.submit(row.id)
  ElMessage.success('已提交审核')
  loadData()
}

const openAudit = (row) => {
  auditForm.id = row.id
  auditForm.pass = true
  auditForm.remark = ''
  auditVisible.value = true
}

const handleAudit = async () => {
  saving.value = true
  try {
    await config.api.audit({ ...auditForm })
    ElMessage.success(auditForm.pass ? '审核通过，库存已更新' : '已驳回')
    auditVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该单据吗？', '提示', { type: 'warning' })
  await config.api.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

/**
 * 打印单据：先拉取完整单据（含明细）渲染到打印区域，再调用浏览器打印。
 * 打印完成后通过 afterprint 事件清理数据，避免残留。
 */
const handlePrint = async (row) => {
  const res = await config.api.detail(row.id)
  printData.value = res.data
  await nextTick()
  const clear = () => {
    printData.value = null
    window.removeEventListener('afterprint', clear)
  }
  window.addEventListener('afterprint', clear)
  window.print()
}

onMounted(async () => {
  const [warehouseRes, materialRes] = await Promise.all([warehouseApi.list(), materialApi.list()])
  warehouses.value = warehouseRes.data || []
  materials.value = materialRes.data || []
  if (props.mode === 'inbound') {
    parties.value = (await supplierApi.list()).data || []
  } else if (props.mode === 'outbound') {
    parties.value = (await customerApi.list()).data || []
  }
  // 支持从首页指标卡带状态跳转，例如「待审核入库单」直接筛出待审核的单据
  if (route.query.status !== undefined && route.query.status !== '') {
    query.status = Number(route.query.status)
  }
  loadData()
})
</script>

<style scoped>
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

/* 明细合计：右对齐，金额用等宽字体并做字重对比 */
.amount-line {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 10px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--ui-border);
}

.amount-label {
  font-size: 12px;
  letter-spacing: 0.02em;
  color: var(--ui-text-muted);
}

.amount-value {
  font-family: var(--ui-font-mono);
  font-size: 16px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--ui-text-strong);
}
</style>
