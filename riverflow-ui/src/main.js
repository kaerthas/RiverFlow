import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'

import App from './App.vue'
import i18n from './i18n'
import router from './router'
import './styles/index.scss'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Element Plus 语言包随 i18n 切换
const elementLocale = i18n.global.locale.value === 'en-US' ? en : zhCn

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(ElementPlus, { locale: elementLocale, zIndex: 3000 })

app.mount('#app')
