<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ABC 分类分析 -->
      <el-tab-pane label="ABC 分类分析" name="abc">
        <el-alert type="info" :closable="false" style="margin-bottom: 12px"
                  title="按照帕累托法则（二八定律），对物料按库存占用金额降序排列并计算累计占比：累计占比 70% 以内为 A 类（重点管理），70%~90% 为 B 类（常规管理），其余为 C 类（简化管理）。" />
        <div ref="abcChartRef" style="height: 320px"></div>
        <el-table :data="abcRows" border stripe style="margin-top: 12px">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" width="180" />
          <el-table-column prop="amount" label="库存金额(元)" width="140" align="right" />
          <el-table-column prop="amountRatio" label="金额占比(%)" width="130" align="right" />
          <el-table-column prop="cumulativeRatio" label="累计占比(%)" width="130" align="right" />
          <el-table-column label="ABC 分类" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.abcType === 'A' ? 'danger' : row.abcType === 'B' ? 'warning' : 'info'" size="small">
                {{ row.abcType }} 类
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 库存周转与呆滞分析 -->
      <el-tab-pane label="库存周转分析" name="turnover">
        <div class="search-bar" style="margin-bottom: 12px">
          <span>统计周期：</span>
          <el-radio-group v-model="turnoverDays" size="small" @change="loadTurnover">
            <el-radio-button :value="30">近 30 天</el-radio-button>
            <el-radio-button :value="60">近 60 天</el-radio-button>
            <el-radio-button :value="90">近 90 天</el-radio-button>
          </el-radio-group>
        </div>
        <el-table :data="turnoverRows" border stripe>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" width="180" />
          <el-table-column prop="currentQty" label="当前库存" width="130" align="right" />
          <el-table-column prop="outQty" label="周期内出库量" width="150" align="right" />
          <el-table-column prop="turnoverRate" label="周转率" width="120" align="right" />
          <el-table-column label="库存健康度" width="140" align="center">
            <template #default="{ row }">
              <el-tag :type="row.slowMoving ? 'warning' : 'success'" size="small">
                {{ row.slowMoving ? '呆滞物料' : '周转正常' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 库存金额分布 -->
      <el-tab-pane label="仓库库存分布" name="warehouse">
        <div ref="warehouseChartRef" style="height: 400px"></div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { stockApi } from '@/api'

const activeTab = ref('abc')
const abcRows = ref([])
const turnoverRows = ref([])
const turnoverDays = ref(30)
const abcChartRef = ref()
const warehouseChartRef = ref()
let abcChart = null
let warehouseChart = null

/** ABC 分类分析：柱状图展示金额，折线图展示累计占比 */
const loadAbc = async () => {
  const res = await stockApi.abc()
  abcRows.value = res.data || []
  abcChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['库存金额', '累计占比'] },
    grid: { left: 70, right: 70, top: 40, bottom: 60 },
    textStyle: { fontFamily: "'Helvetica Neue', 'PingFang SC', sans-serif", color: '#787774' },
    xAxis: { type: 'category', data: abcRows.value.map((item) => item.materialCode), axisLabel: { rotate: 30 } },
    yAxis: [
      { type: 'value', name: '金额(元)' },
      { type: 'value', name: '累计占比(%)', max: 100 }
    ],
    series: [
      { name: '库存金额', type: 'bar', data: abcRows.value.map((item) => item.amount), itemStyle: { color: '#2F3437' } },
      {
        name: '累计占比',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: abcRows.value.map((item) => item.cumulativeRatio),
        itemStyle: { color: '#9F2F2D' },
        lineStyle: { width: 2 }
      }
    ]
  })
}

/** 库存周转与呆滞分析 */
const loadTurnover = async () => {
  const res = await stockApi.turnover(turnoverDays.value)
  turnoverRows.value = res.data || []
}

/** 各仓库库存金额分布 */
const loadWarehouseAmount = async () => {
  const res = await stockApi.amountByWarehouse()
  const rows = res.data || []
  warehouseChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>金额：{c} 元（{d}%）' },
    legend: { bottom: 0 },
    // 单色阶渐变：用炭黑到浅暖灰区分不同仓库，保持整体单色气质
    color: ['#2F3437', '#6E7370', '#A8ABA6', '#C9C6BF', '#E2E1DD'],
    textStyle: { fontFamily: "'Helvetica Neue', 'PingFang SC', sans-serif", color: '#787774' },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: rows.map((item) => ({ name: item.warehouseName, value: item.amount })),
        label: { formatter: '{b}\n{c} 元' }
      }
    ]
  })
}

const handleResize = () => {
  abcChart?.resize()
  warehouseChart?.resize()
}

onMounted(async () => {
  abcChart = echarts.init(abcChartRef.value)
  warehouseChart = echarts.init(warehouseChartRef.value)
  window.addEventListener('resize', handleResize)
  await Promise.all([loadAbc(), loadTurnover(), loadWarehouseAmount()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  abcChart?.dispose()
  warehouseChart?.dispose()
})
</script>
