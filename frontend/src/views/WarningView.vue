<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select v-model="query.warningType" placeholder="全部预警类型" clearable style="width: 180px">
        <el-option label="低于安全库存" :value="1" />
        <el-option label="呆滞物料" :value="2" />
        <el-option label="临近保质期" :value="3" />
      </el-select>
      <el-select v-model="query.handled" placeholder="全部处理状态" clearable style="width: 160px">
        <el-option label="未处理" :value="0" />
        <el-option label="已处理" :value="1" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" :icon="Warning" :loading="generating" @click="handleGenerate">
        执行预警扫描
      </el-button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <span class="title">库存预警记录</span>
        <span class="meta">共 {{ total }} 条预警</span>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无预警，请先点击「执行预警扫描」">
        <el-table-column label="预警类型" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.warningType).type" size="small">{{ typeTag(row.warningType).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.warningLevel).type" size="small" effect="dark">
              {{ levelTag(row.warningLevel).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="materialCode" label="物料编码" width="110" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="currentQty" label="当前库存" width="110" align="right" />
        <el-table-column prop="thresholdValue" label="阈值" width="100" align="right" />
        <el-table-column prop="warningMsg" label="预警说明" min-width="280" show-overflow-tooltip />
        <el-table-column prop="createTime" label="产生时间" width="170" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.handled === 1 ? 'success' : 'danger'" size="small">
              {{ row.handled === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.handled === 0" link type="primary" @click="handleHandle(row)">标记处理</el-button>
            <span v-else style="color: #909399">-</span>
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
import { Warning } from '@element-plus/icons-vue'
import { warningApi } from '@/api'

const loading = ref(false)
const generating = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, warningType: undefined, handled: undefined })

const typeTag = (type) => {
  const map = {
    1: { label: '低于安全库存', type: 'danger' },
    2: { label: '呆滞物料', type: 'warning' },
    3: { label: '临近保质期', type: 'primary' }
  }
  return map[type] || { label: '-', type: 'info' }
}

const levelTag = (level) => {
  const map = {
    1: { label: '严重', type: 'danger' },
    2: { label: '警告', type: 'warning' },
    3: { label: '提示', type: 'info' }
  }
  return map[level] || { label: '-', type: 'info' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await warningApi.page(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  generating.value = true
  try {
    const res = await warningApi.generate()
    ElMessage.success(res.msg)
    query.pageNum = 1
    loadData()
  } finally {
    generating.value = false
  }
}

const handleHandle = async (row) => {
  await warningApi.handle(row.id, '已通知采购处理')
  ElMessage.success('已标记为处理完成')
  loadData()
}

onMounted(loadData)
</script>
