import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

const savedLocale = localStorage.getItem('app-locale')
const defaultLocale = savedLocale || 'zh-CN'

const i18n = createI18n({
  legacy: false,
  locale: defaultLocale,
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

export default i18n

export const SUPPORTED_LOCALES = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' }
]

export function setLocale(locale) {
  i18n.global.locale.value = locale
  localStorage.setItem('app-locale', locale)
  window.location.reload()
}
