<template>
  <!-- 传送到 body 层：打印时直接隐藏应用主体，避免隐藏内容占位产生空白页 -->
  <Teleport to="body">
    <div v-if="order" class="print-sheet">
      <div class="print-title">{{ title }}</div>

    <table class="print-head">
      <tr>
        <td>单据编号：{{ order[config.noField] || '-' }}</td>
        <td>单据状态：{{ statusLabel }}</td>
      </tr>
      <tr v-if="mode === 'transfer'">
        <td>调出仓库：{{ order.fromWarehouseName || '-' }}</td>
        <td>调入仓库：{{ order.toWarehouseName || '-' }}</td>
      </tr>
      <tr v-else>
        <td>仓库：{{ order.warehouseName || '-' }}</td>
        <td>{{ config.partyLabel }}：{{ order[config.partyNameField] || '-' }}</td>
      </tr>
      <tr>
        <td>制单人：{{ order.handlerName || '-' }}</td>
        <td>制单时间：{{ order.createTime || '-' }}</td>
      </tr>
      <tr>
        <td>审核人：{{ order.auditUser || '-' }}</td>
        <td>审核时间：{{ order.auditTime || '-' }}</td>
      </tr>
    </table>

    <table class="print-items">
      <thead>
        <tr>
          <th style="width: 40px">序号</th>
          <th>物料编码</th>
          <th>物料名称</th>
          <th>规格</th>
          <th style="width: 50px">单位</th>
          <th style="width: 80px">数量</th>
          <th v-if="showPrice" style="width: 80px">单价(元)</th>
          <th v-if="showPrice" style="width: 90px">金额(元)</th>
          <th style="width: 110px">批次号</th>
          <th v-if="mode === 'inbound'" style="width: 100px">有效期</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in order.items || []" :key="index">
          <td class="text-center">{{ index + 1 }}</td>
          <td>{{ item.materialCode || '-' }}</td>
          <td>{{ item.materialName || '-' }}</td>
          <td>{{ item.spec || '-' }}</td>
          <td class="text-center">{{ item.unitName || '-' }}</td>
          <td class="text-right">{{ item.quantity }}</td>
          <td v-if="showPrice" class="text-right">{{ item.unitPrice }}</td>
          <td v-if="showPrice" class="text-right">{{ item.amount }}</td>
          <td>{{ item.batchNo || '-' }}</td>
          <td v-if="mode === 'inbound'">{{ item.expireDate || '-' }}</td>
        </tr>
      </tbody>
      <tfoot v-if="showPrice">
        <tr>
          <td :colspan="5" class="text-right">合计金额</td>
          <td class="text-right">{{ totalQuantity }}</td>
          <td></td>
          <td class="text-right">{{ order.totalAmount }}</td>
          <td :colspan="mode === 'inbound' ? 2 : 1"></td>
        </tr>
      </tfoot>
    </table>

    <div v-if="order.remark" class="print-remark">备注：{{ order.remark }}</div>

      <div class="print-footer">
        <div>制单人签字：____________________</div>
        <div>审核人签字：____________________</div>
        <div>打印时间：{{ printTime }}</div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 单据打印组件。
 *
 * 平时通过 CSS 隐藏（display:none），只有点击「打印」并触发浏览器打印时，
 * 借助 @media print 规则隐藏页面其余内容、只输出本组件的单据内容，实现单据打印归档。
 */
const props = defineProps({
  order: { type: Object, default: null },
  mode: { type: String, default: 'inbound' }
})

const configs = {
  inbound: {
    title: '入库单',
    noField: 'inboundNo',
    partyLabel: '供应商',
    partyNameField: 'supplierName',
    showPrice: true
  },
  outbound: {
    title: '出库单',
    noField: 'outboundNo',
    partyLabel: '客户',
    partyNameField: 'customerName',
    showPrice: true
  },
  transfer: {
    title: '库存调拨单',
    noField: 'transferNo',
    partyLabel: '参与方',
    partyNameField: 'fromWarehouseName',
    showPrice: false
  }
}

const config = computed(() => configs[props.mode])
const title = computed(() => config.value.title)
const showPrice = computed(() => config.value.showPrice)

const statusLabel = computed(() => {
  const map = {
    0: '草稿',
    1: '待审核',
    2: props.mode === 'transfer' ? '已完成' : '已过账',
    3: '已驳回'
  }
  return map[props.order?.status] || '-'
})

const totalQuantity = computed(() =>
  (props.order?.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0)
)

const printTime = new Date().toLocaleString('zh-CN')
</script>
