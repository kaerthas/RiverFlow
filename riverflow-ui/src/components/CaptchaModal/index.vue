<template>
  <Transition name="captcha-fade" @after-enter="onAfterEnter">
    <div v-if="visible" class="captcha-overlay" @click.self="handleClose">
      <div class="captcha-card" role="dialog" aria-modal="true">
        <div class="captcha-glow" aria-hidden="true" />

        <div class="captcha-actions">
          <button class="captcha-action-btn" type="button" aria-label="刷新验证码" @click="reloadCaptcha">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M1 7C1 3.686 3.686 1 7 1s6 2.686 6 6-2.686 6-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              <path d="M7 4v3l2 1" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
          <button class="captcha-action-btn" type="button" aria-label="关闭" @click="handleClose">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M1 1L13 13M13 1L1 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </button>
        </div>

        <div class="captcha-hint">
          <span class="captcha-dot" />
          <span class="captcha-hint-text">安全验证</span>
        </div>

        <div class="captcha-box">
          <div id="captcha-box" ref="captchaBox" class="captcha-target" />
          <div v-if="status !== 'ready'" class="captcha-state">
            <template v-if="status === 'loading'">
              <div class="captcha-loading-spinner" />
              <span>加载中...</span>
            </template>
            <template v-else>
              <span>验证码加载失败</span>
              <button type="button" @click="initCaptcha">重试</button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['verify-success', 'verify-fail', 'close'])

const captchaBox = ref(null)
const status = ref('loading')
let currentCaptcha = null
let isInitializing = false
let mutationObserver = null
let readyTimer = null

const baseApi = import.meta.env.VITE_APP_BASE_API || '/api'

function resetWatching() {
  if (mutationObserver) {
    mutationObserver.disconnect()
    mutationObserver = null
  }
  if (readyTimer) {
    clearTimeout(readyTimer)
    readyTimer = null
  }
}

function setError(message) {
  console.error('[CaptchaModal]', message)
  status.value = 'error'
  resetWatching()
  emit('verify-fail')
}

function observeCaptchaRender(el) {
  // 监听 TAC 实际渲染出验证码 DOM（#tianai-captcha）
  const checkRendered = () => {
    const slider = el.querySelector('#tianai-captcha')
    if (slider) {
      status.value = 'ready'
      resetWatching()
      return true
    }
    return false
  }

  if (checkRendered()) return

  mutationObserver = new MutationObserver(() => {
    checkRendered()
  })
  mutationObserver.observe(el, { childList: true, subtree: true })

  // 兜底：3 秒后仍未渲染成功则视为失败
  readyTimer = setTimeout(() => {
    if (status.value !== 'ready') {
      setError('验证码渲染超时，请检查接口返回与 TAC SDK 状态')
    }
  }, 3000)
}

function patchTacRequests(config) {
  // 拦截 generate 请求，用于调试与错误兜底
  const originalGenerate = config.requestCaptchaData.bind(config)
  config.requestCaptchaData = function () {
    console.log('[CaptchaModal] requesting captcha data...')
    return originalGenerate().then((res) => {
      console.log('[CaptchaModal] captcha data response:', res)
      if (!res || res.code !== 200) {
        setError(`验证码生成失败: ${res?.msg || '未知错误'}`)
      }
      return res
    }).catch((err) => {
      setError(`验证码网络请求失败: ${err?.message || err}`)
      throw err
    })
  }

  // 拦截 check 请求，用于调试
  const originalCheck = config.validCaptcha.bind(config)
  config.validCaptcha = function (id, data, captchaInstance, tacInstance) {
    console.log('[CaptchaModal] validating captcha...')
    return originalCheck(id, data, captchaInstance, tacInstance).then((res) => {
      console.log('[CaptchaModal] captcha validate response:', res)
      return res
    }).catch((err) => {
      console.error('[CaptchaModal] captcha validate failed:', err)
      throw err
    })
  }
}

function waitForStableBox() {
  return new Promise((resolve, reject) => {
    let rafId = null
    let timeoutId = null
    let attempts = 0

    const cleanup = () => {
      if (rafId) cancelAnimationFrame(rafId)
      if (timeoutId) clearTimeout(timeoutId)
    }

    const check = () => {
      const el = captchaBox.value
      if (!el) {
        cleanup()
        return reject(new Error('验证码挂载元素不存在'))
      }

      const rect = el.getBoundingClientRect()
      const hasSize = rect.width > 0 && rect.height > 0

      if (hasSize && attempts >= 2) {
        cleanup()
        return resolve(el)
      }

      attempts++
      rafId = requestAnimationFrame(check)
    }

    timeoutId = setTimeout(() => {
      cleanup()
      reject(new Error('等待验证码容器尺寸稳定超时'))
    }, 3000)

    rafId = requestAnimationFrame(check)
  })
}

function initCaptcha() {
  if (isInitializing) return
  isInitializing = true
  resetWatching()
  status.value = 'loading'

  if (typeof window.TAC === 'undefined') {
    setError('TAC 验证码 SDK 未加载')
    isInitializing = false
    return
  }

  waitForStableBox()
    .then((el) => {
      try {
        const captchaConfig = {
          requestCaptchaDataUrl: `${baseApi}/captcha/generate`,
          validCaptchaUrl: `${baseApi}/captcha/check`,
          bindEl: el,
          validSuccess: (res, captchaInstance, tacInstance) => {
            const captchaToken = res?.data?.id
            if (captchaToken) {
              tacInstance.destroyWindow()
              emit('verify-success', captchaToken)
            } else {
              console.warn('[CaptchaModal] 验证成功但未返回 token')
              tacInstance.reloadCaptcha()
              emit('verify-fail')
            }
          },
          validFail: (res, captchaInstance, tacInstance) => {
            console.warn('[CaptchaModal] 验证失败:', res)
            tacInstance.reloadCaptcha()
            emit('verify-fail')
          },
          btnRefreshFun: (btnEl, tacInstance) => {
            tacInstance.reloadCaptcha()
          },
          btnCloseFun: (btnEl, tacInstance) => {
            tacInstance.destroyWindow()
            handleClose()
          }
        }

        currentCaptcha = new window.TAC(captchaConfig, {})
        patchTacRequests(currentCaptcha.config)
        observeCaptchaRender(el)
        currentCaptcha.init()
      } catch (e) {
        setError(`验证码初始化失败: ${e?.message || e}`)
      } finally {
        isInitializing = false
      }
    })
    .catch((err) => {
      setError(err?.message || '验证码容器未就绪')
      isInitializing = false
    })
}

function onAfterEnter() {
  if (!currentCaptcha) initCaptcha()
}

function handleClose() {
  if (currentCaptcha) {
    currentCaptcha.destroyWindow?.()
    currentCaptcha = null
  }
  resetWatching()
  emit('close')
}

function reloadCaptcha() {
  if (currentCaptcha?.reloadCaptcha) {
    status.value = 'loading'
    currentCaptcha.reloadCaptcha()
    observeCaptchaRender(captchaBox.value)
  }
}

watch(
  () => props.visible,
  (newVal) => {
    if (!newVal) {
      if (currentCaptcha) {
        currentCaptcha.destroyWindow?.()
        currentCaptcha = null
      }
      resetWatching()
      isInitializing = false
      status.value = 'loading'
    }
  },
  { immediate: false }
)

onUnmounted(() => {
  if (currentCaptcha) {
    currentCaptcha.destroyWindow?.()
    currentCaptcha = null
  }
  resetWatching()
})

defineExpose({
  reloadCaptcha,
  destroy: handleClose
})
</script>

<style scoped lang="scss">
.captcha-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.32);
  backdrop-filter: blur(8px);
}

.captcha-card {
  position: relative;
  width: auto;
  max-width: 92vw;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow:
    0 1px 2px rgba(0, 0, 0, 0.02),
    0 8px 24px rgba(0, 0, 0, 0.04),
    0 24px 60px rgba(0, 0, 0, 0.08);
  padding: 18px 18px 16px;
  overflow: hidden;
}

.captcha-glow {
  position: absolute;
  top: -60%;
  left: -20%;
  width: 140%;
  height: 120%;
  background: radial-gradient(circle at 50% 0%, rgba(56, 189, 248, 0.12), transparent 60%);
  pointer-events: none;
}

.captcha-hint {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 14px;
  user-select: none;
}

.captcha-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.12);
}

.captcha-hint-text {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  letter-spacing: 0.5px;
}

.captcha-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 6px;
}

.captcha-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    background: rgba(241, 245, 249, 0.8);
    color: #64748b;
  }

  &:active {
    transform: scale(0.92);
  }

  &:last-child:hover {
    transform: rotate(90deg);
  }
}

.captcha-box {
  position: relative;
  width: 318px;
  height: 318px;
}

.captcha-target {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-state {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
  font-size: 13px;
  background: #f8fafc;
  border-radius: 12px;
}

.captcha-loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #e2e8f0;
  border-top-color: #0ea5e9;
  border-radius: 50%;
  animation: captcha-spin 0.8s linear infinite;
}

@keyframes captcha-spin {
  to {
    transform: rotate(360deg);
  }
}

.captcha-state button {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  background: #0ea5e9;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #0284c7;
  }
}

/* 覆盖 TAC 默认样式，使其融入弹窗 */
.captcha-box {
  :deep(#tianai-captcha-parent) {
    background: transparent !important;
    border-radius: 12px !important;
    box-shadow: none !important;
    border: none !important;
    overflow: hidden !important;
    padding: 0 !important;
  }

  :deep(#tianai-captcha-box) {
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.slider-tip) {
    color: #334155;
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 10px;
  }

  :deep(.slider-move) {
    margin-top: 12px;
  }

  :deep(.slider-move-track) {
    background: #f1f5f9;
    border-color: #e2e8f0;
    color: #64748b;
    border-radius: 8px;
  }

  :deep(.slider-move .slider-move-btn) {
    top: -7px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  :deep(.slider-bottom) {
    display: none;
  }
}

/* 过渡动画 */
.captcha-fade-enter-active,
.captcha-fade-leave-active {
  transition: opacity 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.captcha-fade-enter-active .captcha-card,
.captcha-fade-leave-active .captcha-card {
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.captcha-fade-enter-from,
.captcha-fade-leave-to {
  opacity: 0;
}

.captcha-fade-enter-from .captcha-card,
.captcha-fade-leave-to .captcha-card {
  opacity: 0;
  transform: scale(0.96) translateY(8px);
}
</style>
