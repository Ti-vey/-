import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './styles/index.css'
// 设计令牌层：需在 index.css 之后引入，用于覆盖基础样式与 Element Plus 变量
import './styles/theme.css'

const app = createApp(App)

// 全局注册 Element Plus 图标组件
Object.entries(ElementPlusIconsVue).forEach(([key, component]) => {
  app.component(key, component)
})

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
