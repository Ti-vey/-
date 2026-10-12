<template>
  <div class="page-container">
    <el-alert type="success" :closable="false" style="margin-bottom: 12px">
      <template #title>
        智能补货建议：结合 ABC 分类、安全库存模型（平均日耗 × 补货提前期）与经济订货批量 EOQ 自动计算，
        点击「生成补货建议」即可按最新库存与近 30 天消耗数据重新测算。
      </template>
    </el-alert>

    <div class="search-bar">
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px">
        <el-option label="待处理" :value="0" />
        <el-option label="已生成采购" :value="1" />
        <el-option label="已忽略" :value="2" />
      </el-select>
      <el-select v-model="query.urgency" placeholder="全部紧急程度" clearable style="width: 160px">
        <el-option label="紧急" :value="1" />
        <el-option label="一般" :value="2" />
        <el-option label="低" :value="3" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" :icon="MagicStick" :loading="generating" @click="handleGenerate">
        生成补货建议
      </el-button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">补货计划</span>
        <span class="meta">共 {{ total }} 条建议</span>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无补货建议，请先点击「生成补货建议」">
        <el-table-column prop="replenishNo" label="计划编号" width="200" />
        <el-table-column prop="materialCode" label="物料编码" width="110" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="abcType" label="ABC" width="70" align="center" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="currentQty" label="当前库存" width="110" align="right" />
        <el-table-column prop="safetyStock" label="安全库存" width="110" align="right" />
        <el-table-column prop="avgDailyUsage" label="平均日耗" width="110" align="right" />
        <el-table-column prop="leadTime" label="提前期(天)" width="100" align="center" />
        <el-table-column prop="eoqQty" label="EOQ" width="100" align="right" />
        <el-table-column prop="suggestQty" label="建议补货量" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">{{ row.suggestQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="紧急程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="urgencyTag(row.urgency).type" size="small">{{ urgencyTag(row.urgency).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.status).type">{{ statusTag(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary"
                       @click="changeStatus(row, 1, '已生成采购计划')">生成采购</el-button>
            <el-button v-if="row.status === 0" link type="info"
                       @click="changeStatus(row, 2, '人工判断暂不补货')">忽略</el-button>
          </template>
        </el-table-column>
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
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { replenishmentApi } from '@/api'

const loading = ref(false)
const generating = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined, urgency: undefined })

const urgencyTag = (urgency) => {
  const map = {
    1: { label: '紧急', type: 'danger' },
    2: { label: '一般', type: 'warning' },
    3: { label: '低', type: 'info' }
  }
  return map[urgency] || { label: '-', type: 'info' }
}

const statusTag = (status) => {
  const map = {
    0: { label: '待处理', type: 'warning' },
    1: { label: '已生成采购', type: 'success' },
    2: { label: '已忽略', type: 'info' }
  }
  return map[status] || { label: '-', type: 'info' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await replenishmentApi.page(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  generating.value = true
  try {
    const res = await replenishmentApi.generate()
    ElMessage.success(res.msg)
    query.pageNum = 1
    loadData()
  } finally {
    generating.value = false
  }
}

const changeStatus = async (row, status, remark) => {
  await replenishmentApi.changeStatus(row.id, status, remark)
  ElMessage.success('操作成功')
  loadData()
}

onMounted(loadData)
</script>
