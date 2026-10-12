import { createRouter, createWebHashHistory } from 'vue-router'

/**
 * 路由表：采用 Hash 模式，便于直接打开构建产物预览。
 * meta.title 用于侧边栏与面包屑，meta.icon 指定菜单图标。
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页看板', icon: 'DataBoard', group: '概览' }
      },
      {
        path: 'material',
        component: () => import('@/views/base/MaterialView.vue'),
        meta: { title: '物料档案', icon: 'Goods', group: '基础数据' }
      },
      {
        path: 'category',
        component: () => import('@/views/base/CategoryView.vue'),
        meta: { title: '物料分类', icon: 'Files', group: '基础数据' }
      },
      {
        path: 'unit',
        component: () => import('@/views/base/UnitView.vue'),
        meta: { title: '计量单位', icon: 'ScaleToOriginal', group: '基础数据' }
      },
      {
        path: 'warehouse',
        component: () => import('@/views/base/WarehouseView.vue'),
        meta: { title: '仓库管理', icon: 'OfficeBuilding', group: '基础数据' }
      },
      {
        path: 'location',
        component: () => import('@/views/base/LocationView.vue'),
        meta: { title: '库位管理', icon: 'Grid', group: '基础数据' }
      },
      {
        path: 'supplier',
        component: () => import('@/views/base/SupplierView.vue'),
        meta: { title: '供应商管理', icon: 'Van', group: '基础数据' }
      },
      {
        path: 'customer',
        component: () => import('@/views/base/CustomerView.vue'),
        meta: { title: '客户管理', icon: 'User', group: '基础数据' }
      },
      {
        path: 'inbound',
        component: () => import('@/views/order/InboundView.vue'),
        meta: { title: '入库管理', icon: 'Download', group: '库存业务' }
      },
      {
        path: 'outbound',
        component: () => import('@/views/order/OutboundView.vue'),
        meta: { title: '出库管理', icon: 'Upload', group: '库存业务' }
      },
      {
        path: 'transfer',
        component: () => import('@/views/order/TransferView.vue'),
        meta: { title: '库存调拨', icon: 'Switch', group: '库存业务' }
      },
      {
        path: 'stocktaking',
        component: () => import('@/views/order/StocktakingView.vue'),
        meta: { title: '库存盘点', icon: 'List', group: '库存业务' }
      },
      {
        path: 'stock',
        component: () => import('@/views/stock/StockView.vue'),
        meta: { title: '库存查询', icon: 'Box', group: '库存查询' }
      },
      {
        path: 'flow',
        component: () => import('@/views/stock/FlowView.vue'),
        meta: { title: '库存流水', icon: 'Tickets', group: '库存查询' }
      },
      {
        path: 'replenishment',
        component: () => import('@/views/ReplenishmentView.vue'),
        meta: { title: '智能补货', icon: 'MagicStick', group: '智能分析' }
      },
      {
        path: 'warning',
        component: () => import('@/views/WarningView.vue'),
        meta: { title: '库存预警', icon: 'Warning', group: '智能分析' }
      },
      {
        path: 'report',
        component: () => import('@/views/ReportView.vue'),
        meta: { title: '统计分析', icon: 'TrendCharts', group: '智能分析' }
      },
      {
        path: 'user',
        component: () => import('@/views/system/UserView.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', group: '系统管理' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 全局前置守卫：未登录一律跳转到登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    document.title = to.meta.title ? `${to.meta.title} - 仓库库存管理系统` : '仓库库存管理系统'
    next()
  }
})

export default router
