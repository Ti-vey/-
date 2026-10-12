<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-weight: 600">查看维度：</span>
      <el-radio-group v-model="viewMode" @change="handleModeChange">
        <el-radio-button :value="'detail'">库存明细</el-radio-button>
        <el-radio-button :value="'material'">按物料汇总</el-radio-button>
        <el-radio-button :value="'warehouse'">按仓库汇总</el-radio-button>
      </el-radio-group>
      <template v-if="viewMode === 'detail'">
        <el-input v-model="query.keyword" placeholder="物料名称/编码" clearable style="width: 200px"
                  @keyup.enter="handleSearch" />
        <el-select v-model="query.warehouseId" placeholder="全部仓库" clearable style="width: 160px">
          <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
        </el-select>
        <el-checkbox v-model="query.onlyWarning">只看低于安全库存</el-checkbox>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </template>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">{{ viewTitle }}</span>
        <span class="meta">库存金额合计 {{ displayTotalAmount.toLocaleString() }} 元</span>
      </div>

      <!-- 维度一：库存明细 -->
      <el-table v-if="viewMode === 'detail'" v-loading="loading" :data="tableData" border stripe
                empty-text="暂无库存记录">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物料编码" width="110" />
        <el-table-column prop="materialName" label="物料名称" width="160" />
        <el-table-column prop="spec" label="规格" width="130" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="batchNo" label="批次号" width="130" />
        <el-table-column prop="quantity" label="库存数量" width="110" align="right">
          <template #default="{ row }">
            <span :style="{ color: isLow(row) ? '#f56c6c' : '#303133', fontWeight: isLow(row) ? 600 : 400 }">
              {{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="unitName" label="单位" width="80" align="center" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="right" />
        <el-table-column prop="avgCost" label="平均成本" width="110" align="right" />
        <el-table-column prop="amount" label="库存金额" width="120" align="right" />
        <el-table-column prop="expireDate" label="有效期" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="isLow(row)" type="danger" size="small">低于安全库存</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 维度二：按物料汇总（跨仓库合并） -->
      <el-table v-else-if="viewMode === 'material'" v-loading="loading" :data="materialSummary" border stripe
                show-summary :summary-method="materialSummaryMethod" empty-text="暂无库存记录">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="180" />
        <el-table-column prop="spec" label="规格" width="140" />
        <el-table-column prop="unitName" label="单位" width="80" align="center" />
        <el-table-column prop="warehouseCount" label="分布仓库数" width="110" align="center" />
        <el-table-column prop="quantity" label="库存总量" width="120" align="right" />
        <el-table-column prop="safetyStock" label="安全库存" width="110" align="right" />
        <el-table-column prop="amount" label="库存金额(元)" width="130" align="right" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="Number(row.safetyStock) > 0 && Number(row.quantity) <= Number(row.safetyStock)"
                    type="danger" size="small">低于安全库存</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 维度三：按仓库汇总 -->
      <el-table v-else v-loading="loading" :data="warehouseSummary" border stripe
                show-summary :summary-method="warehouseSummaryMethod" empty-text="暂无库存记录">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="warehouseName" label="仓库名称" width="200" />
        <el-table-column prop="materialCount" label="物料种类数" width="130" align="center" />
        <el-table-column prop="quantity" label="库存总量" width="140" align="right" />
        <el-table-column prop="amount" label="库存金额(元)" width="150" align="right" />
        <el-table-column label="金额占比" min-width="200">
          <template #default="{ row }">
            <el-progress :percentage="amountPercent(row)" :stroke-width="14" />
          </template>
        </el-table-column>
      </el-table>

      <div v-if="viewMode === 'detail'" class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                       @size-change="loadData" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { stockApi, warehouseApi } from '@/api'

const route = useRoute()
const loading = ref(false)
/** 查看维度：detail 明细 / material 按物料汇总 / warehouse 按仓库汇总 */
const viewMode = ref('detail')
const tableData = ref([])
const materialSummary = ref([])
const warehouseSummary = ref([])
const warehouses = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: undefined, warehouseId: undefined, onlyWarning: false })

const viewTitle = computed(() => {
  const map = { detail: '实时库存明细', material: '按物料汇总（跨仓库合并）', warehouse: '按仓库汇总' }
  return map[viewMode.value]
})

/** 明细视图按当前页合计，汇总视图按全量合计 */
const displayTotalAmount = computed(() => {
  if (viewMode.value === 'material') {
    return materialSummary.value.reduce((sum, row) => sum + Number(row.amount || 0), 0)
  }
  if (viewMode.value === 'warehouse') {
    return warehouseSummary.value.reduce((sum, row) => sum + Number(row.amount || 0), 0)
  }
  return tableData.value.reduce((sum, row) => sum + Number(row.amount || 0), 0)
})

/** 仓库金额占比，用于进度条展示 */
const amountPercent = (row) => {
  const total = warehouseSummary.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  if (!total) {
    return 0
  }
  return Number(((Number(row.amount || 0) / total) * 100).toFixed(1))
}

/** 物料汇总表的合计行 */
const materialSummaryMethod = ({ columns, data }) => {
  return columns.map((column, index) => {
    if (index === 0) {
      return '合计'
    }
    if (column.property === 'quantity') {
      return data.reduce((sum, row) => sum + Number(row.quantity || 0), 0).toFixed(2)
    }
    if (column.property === 'amount') {
      return data.reduce((sum, row) => sum + Number(row.amount || 0), 0).toFixed(2)
    }
    return ''
  })
}

/** 仓库汇总表的合计行 */
const warehouseSummaryMethod = ({ columns, data }) => {
  return columns.map((column, index) => {
    if (index === 0) {
      return '合计'
    }
    if (column.property === 'quantity') {
      return data.reduce((sum, row) => sum + Number(row.quantity || 0), 0).toFixed(2)
    }
    if (column.property === 'amount') {
      return data.reduce((sum, row) => sum + Number(row.amount || 0), 0).toFixed(2)
    }
    return ''
  })
}

const isLow = (row) => Number(row.quantity) <= Number(row.safetyStock || 0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await stockApi.page(query)
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
  Object.assign(query, { pageNum: 1, keyword: undefined, warehouseId: undefined, onlyWarning: false })
  loadData()
}

/** 加载汇总视图数据 */
const loadSummary = async () => {
  loading.value = true
  try {
    if (viewMode.value === 'material') {
      materialSummary.value = (await stockApi.summaryByMaterial()).data || []
    } else if (viewMode.value === 'warehouse') {
      warehouseSummary.value = (await stockApi.summaryByWarehouse()).data || []
    }
  } finally {
    loading.value = false
  }
}

const handleModeChange = () => {
  if (viewMode.value === 'detail') {
    loadData()
  } else {
    loadSummary()
  }
}

onMounted(async () => {
  const res = await warehouseApi.list()
  warehouses.value = res.data || []
  // 支持从首页「低库存提醒」带参数跳转过来
  if (route.query.onlyWarning === '1') {
    query.onlyWarning = true
  }
  loadData()
})
</script>
