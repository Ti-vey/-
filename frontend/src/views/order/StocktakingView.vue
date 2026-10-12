<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select v-model="query.warehouseId" placeholder="全部仓库" clearable style="width: 160px">
        <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="盘点中" :value="0" />
        <el-option label="已完成" :value="1" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">库存盘点</span>
        <div class="toolbar-actions">
          <span class="meta">共 {{ total }} 张盘点单</span>
          <el-select v-model="createForm.warehouseId" placeholder="选择盘点仓库" style="width: 180px; margin-right: 8px">
            <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
          <el-button type="primary" :icon="Plus" @click="handleCreate">新建盘点单</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无盘点单">
        <el-table-column prop="stocktakingNo" label="盘点单号" width="200" />
        <el-table-column prop="warehouseName" label="盘点仓库" width="130" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已完成' : '盘点中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="盘点人" width="110" />
        <el-table-column prop="profitQty" label="盘盈总数" width="110" align="right" />
        <el-table-column prop="lossQty" label="盘亏总数" width="110" align="right" />
        <el-table-column prop="stocktakingTime" label="完成时间" width="170" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看/录入</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="handleFinish(row)">完成盘点</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="盘点明细" width="900px" top="8vh">
      <el-alert v-if="detail.status === 0" title="请录入实盘数量后点击「保存实盘」" type="info" :closable="false"
                style="margin-bottom: 12px" />
      <el-table :data="detail.items" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="materialCode" label="物料编码" width="110" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="batchNo" label="批次" width="120" />
        <el-table-column prop="bookQty" label="账面数量" width="110" align="right" />
        <el-table-column label="实盘数量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.actualQty" :min="0" :precision="2" :controls="false"
                             :disabled="detail.status === 1" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column prop="unitName" label="单位" width="80" align="center" />
        <el-table-column label="盈亏" width="110" align="right">
          <template #default="{ row }">
            <span :style="{ color: diff(row) === 0 ? '#909399' : diff(row) > 0 ? '#67c23a' : '#f56c6c' }">
              {{ diff(row) > 0 ? '+' : '' }}{{ diff(row) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button v-if="detail.status === 0" type="primary" :loading="saving" @click="handleSaveActual">
            保存实盘
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { stocktakingApi, warehouseApi } from '@/api'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const warehouses = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined, warehouseId: undefined })
const createForm = reactive({ warehouseId: undefined })
const detailVisible = ref(false)
const detail = reactive({ items: [] })

const diff = (row) => Number((Number(row.actualQty || 0) - Number(row.bookQty || 0)).toFixed(2))

const loadData = async () => {
  loading.value = true
  try {
    const res = await stocktakingApi.page(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!createForm.warehouseId) {
    ElMessage.warning('请先选择要盘点的仓库')
    return
  }
  await stocktakingApi.create({ warehouseId: createForm.warehouseId, remark: '常规盘点' })
  ElMessage.success('盘点单已生成，请录入实盘数量')
  loadData()
}

const openDetail = async (row) => {
  const res = await stocktakingApi.detail(row.id)
  Object.keys(detail).forEach((key) => delete detail[key])
  Object.assign(detail, res.data)
  detailVisible.value = true
}

const handleSaveActual = async () => {
  saving.value = true
  try {
    await stocktakingApi.saveActual(detail.id, { items: detail.items, remark: detail.remark })
    ElMessage.success('实盘数量已保存')
    detailVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleFinish = async (row) => {
  await stocktakingApi.finish(row.id)
  ElMessage.success('盘点完成，盈亏已调整库存')
  loadData()
}

onMounted(async () => {
  const res = await warehouseApi.list()
  warehouses.value = res.data || []
  loadData()
})
</script>
