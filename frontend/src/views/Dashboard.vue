<template>
  <div class="page-container">
    <!-- 概览指标卡 -->
    <el-row :gutter="16" class="ui-rise" :style="{ '--ui-index': 0 }">
      <el-col v-for="card in cards" :key="card.label" :span="6" style="margin-bottom: 16px">
        <router-link :to="card.to" class="stat-link">
          <div class="stat-card">
            <div class="label">{{ card.label }}</div>
            <div class="value" :style="{ color: card.color || 'var(--ui-text-strong)' }">{{ card.value }}</div>
            <el-icon class="stat-arrow"><TopRight /></el-icon>
          </div>
        </router-link>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="ui-rise" :style="{ '--ui-index': 1 }">
      <el-col :span="14">
        <div class="chart-card">
          <el-radio-group v-model="trendDays" size="small" style="margin-bottom: 12px" @change="loadTrend">
            <el-radio-button :value="7">近 7 天</el-radio-button>
            <el-radio-button :value="15">近 15 天</el-radio-button>
            <el-radio-button :value="30">近 30 天</el-radio-button>
          </el-radio-group>
          <div ref="trendRef" style="height: 320px"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="chart-card">
          <div style="margin-bottom: 12px; font-weight: 600">ABC 分类库存金额分布</div>
          <div ref="abcRef" style="height: 320px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="ui-rise" :style="{ '--ui-index': 2, marginTop: '16px' }">
      <el-col :span="24">
        <div class="chart-card">
          <div style="margin-bottom: 12px; font-weight: 600">库存金额 Top 5 物料</div>
          <div ref="topRef" style="height: 300px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 待办提醒：低库存物料与临期批次 -->
    <el-row :gutter="16" class="ui-rise" :style="{ '--ui-index': 3, marginTop: '16px' }">
      <el-col :span="12">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">
              <el-icon color="#f56c6c"><Warning /></el-icon>
              低库存物料提醒
              <el-tag v-if="lowStock.length" type="danger" size="small">{{ lowStock.length }}</el-tag>
            </span>
            <el-link type="primary" @click="router.push('/stock?onlyWarning=1')">查看全部</el-link>
          </div>
          <el-table v-if="lowStock.length" :data="lowStock" size="small" border>
            <el-table-column prop="materialName" label="物料" show-overflow-tooltip />
            <el-table-column prop="warehouseName" label="仓库" width="110" />
            <el-table-column prop="quantity" label="当前库存" width="90" align="right" />
            <el-table-column prop="safetyStock" label="安全库存" width="90" align="right" />
            <el-table-column label="缺口" width="80" align="right">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: 600">{{ row.gap }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无低库存物料" :image-size="60" />
        </div>
      </el-col>

      <el-col :span="12">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">
              <el-icon color="#e6a23c"><Clock /></el-icon>
              临期批次提醒（90 天内到期）
              <el-tag v-if="expiring.length" type="warning" size="small">{{ expiring.length }}</el-tag>
            </span>
            <el-link type="primary" @click="router.push('/warning')">查看全部</el-link>
          </div>
          <el-table v-if="expiring.length" :data="expiring" size="small" border>
            <el-table-column prop="materialName" label="物料" show-overflow-tooltip />
            <el-table-column prop="batchNo" label="批次号" width="120" />
            <el-table-column prop="quantity" label="数量" width="80" align="right" />
            <el-table-column prop="expireDate" label="到期日" width="110" />
            <el-table-column label="剩余天数" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.remainDays <= 30 ? 'danger' : 'warning'" size="small">
                  {{ row.remainDays }} 天
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无临期批次" :image-size="60" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { statApi } from '@/api'

const router = useRouter()
const cards = ref([])
const lowStock = ref([])
const expiring = ref([])
const trendDays = ref(7)
const trendRef = ref()
const abcRef = ref()
const topRef = ref()
let trendChart = null
let abcChart = null
let topChart = null

/** 加载概览指标 */
const loadDashboard = async () => {
  const res = await statApi.dashboard()
  const data = res.data
  cards.value = [
    // to 指向该指标对应的业务页面，点击卡片可直接跳转
    { label: '物料总数', value: data.materialCount, to: '/material' },
    { label: '仓库数量', value: data.warehouseCount, to: '/warehouse' },
    { label: '库存总金额（元）', value: Number(data.stockTotalAmount).toLocaleString(), to: '/report' },
    { label: '库存总数量', value: Number(data.stockTotalQty).toLocaleString(), to: '/stock' },
    { label: '低于安全库存', value: data.lowStockCount, color: '#9F2F2D', to: '/stock?onlyWarning=1' },
    { label: '未处理预警', value: data.unhandledWarningCount, color: '#956400', to: '/warning' },
    { label: '待处理补货建议', value: data.pendingReplenishCount, color: '#956400', to: '/replenishment' },
    { label: '待审核入库单', value: data.pendingInboundCount, to: '/inbound?status=1' }
  ]
  renderAbc(data.abcDistribution || [])
}

/** 渲染出入库趋势 */
const loadTrend = async () => {
  const res = await statApi.trend(trendDays.value)
  const rows = res.data || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库数量', '出库数量'] },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    textStyle: { fontFamily: "'Helvetica Neue', 'PingFang SC', sans-serif", color: '#787774' },
    xAxis: { type: 'category', data: rows.map((item) => item.date) },
    yAxis: { type: 'value', name: '数量' },
    series: [
      { name: '入库数量', type: 'bar', data: rows.map((item) => item.inboundQty), itemStyle: { color: '#1F6C9F' } },
      { name: '出库数量', type: 'bar', data: rows.map((item) => item.outboundQty), itemStyle: { color: '#9F2F2D' } }
    ]
  })
}

/** 渲染 ABC 分类饼图 */
const renderAbc = (rows) => {
  abcChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>金额：{c} 元（{d}%）' },
    legend: { bottom: 0 },
    // 单色阶：用炭黑到浅灰区分 A/B/C，避免出现多种彩色
    color: ['#2F3437', '#7A7F7C', '#C4C1BA'],
    textStyle: { fontFamily: "'Helvetica Neue', 'PingFang SC', sans-serif", color: '#787774' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        data: rows.map((item) => ({
          name: `${item.abcType} 类物料`,
          value: item.amount
        }))
      }
    ]
  })
}

/** 渲染库存金额 Top 物料 */
const loadTop = async () => {
  const res = await statApi.topMaterials(5)
  const rows = res.data || []
  topChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 140, right: 40, top: 20, bottom: 30 },
    textStyle: { fontFamily: "'Helvetica Neue', 'PingFang SC', sans-serif", color: '#787774' },
    xAxis: { type: 'value', name: '金额（元）' },
    yAxis: { type: 'category', data: rows.map((item) => item.materialName) },
    series: [
      {
        type: 'bar',
        data: rows.map((item) => item.amount),
        itemStyle: { color: '#2F3437' },
        label: { show: true, position: 'right' }
      }
    ]
  })
}

const handleResize = () => {
  trendChart?.resize()
  abcChart?.resize()
  topChart?.resize()
}

/** 加载首页待办提醒：低库存物料与临期批次 */
const loadReminders = async () => {
  const [lowRes, expRes] = await Promise.all([statApi.lowStock(5), statApi.expiring(5)])
  lowStock.value = lowRes.data || []
  expiring.value = expRes.data || []
}

onMounted(async () => {
  trendChart = echarts.init(trendRef.value)
  abcChart = echarts.init(abcRef.value)
  topChart = echarts.init(topRef.value)
  window.addEventListener('resize', handleResize)
  await Promise.all([loadDashboard(), loadTrend(), loadTop(), loadReminders()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  abcChart?.dispose()
  topChart?.dispose()
})
</script>
