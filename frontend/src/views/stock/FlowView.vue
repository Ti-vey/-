<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select v-model="query.flowType" placeholder="全部流水类型" clearable style="width: 180px">
        <el-option v-for="item in flowTypes" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.warehouseId" placeholder="全部仓库" clearable style="width: 160px">
        <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">库存流水台账</span>
        <span class="meta">共 {{ total }} 条流水</span>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无流水记录">
        <el-table-column prop="flowNo" label="流水号" width="200" />
        <el-table-column prop="materialName" label="物料" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="flowTag(row.flowType).type" size="small">{{ flowTag(row.flowType).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次" width="120" />
        <el-table-column prop="changeQty" label="变动数量" width="110" align="right">
          <template #default="{ row }">
            <span :style="{ color: Number(row.changeQty) >= 0 ? '#67c23a' : '#f56c6c' }">
              {{ Number(row.changeQty) >= 0 ? '+' : '' }}{{ row.changeQty }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQty" label="变动前" width="100" align="right" />
        <el-table-column prop="afterQty" label="变动后" width="100" align="right" />
        <el-table-column prop="refNo" label="关联单据" width="170" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="createTime" label="发生时间" width="170" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                       @size-change="loadData" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { stockApi, warehouseApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const warehouses = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, flowType: undefined, warehouseId: undefined })

const flowTypes = [
  { label: '入库', value: 1 },
  { label: '出库', value: 2 },
  { label: '调拨入库', value: 3 },
  { label: '调拨出库', value: 4 },
  { label: '盘盈', value: 5 },
  { label: '盘亏', value: 6 }
]

const flowTag = (type) => {
  const map = {
    1: { label: '入库', type: 'success' },
    2: { label: '出库', type: 'danger' },
    3: { label: '调拨入', type: 'primary' },
    4: { label: '调拨出', type: 'warning' },
    5: { label: '盘盈', type: 'success' },
    6: { label: '盘亏', type: 'danger' }
  }
  return map[type] || { label: '未知', type: 'info' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await stockApi.flowPage(query)
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
  Object.assign(query, { pageNum: 1, flowType: undefined, warehouseId: undefined })
  loadData()
}

onMounted(async () => {
  const res = await warehouseApi.list()
  warehouses.value = res.data || []
  loadData()
})
</script>
