<template>
  <div class="login-page" :class="{ 'is-dark': isDark }">
    <!-- 左侧品牌宣传区 -->
    <div class="brand-panel">
      <div class="brand-bg" aria-hidden="true" />

      <div class="brand-header">
        <div class="brand-logo">
          <svg width="22" height="22" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="10" fill="#ffffff" fill-opacity="0.12" />
            <circle cx="12" cy="13" r="4.5" fill="#ffffff" fill-opacity="0.9" />
            <circle cx="28" cy="13" r="4.5" fill="#ffffff" fill-opacity="0.9" />
            <ellipse cx="20" cy="23" rx="11" ry="9" fill="#ffffff" fill-opacity="0.9" />
            <circle cx="15.5" cy="21" r="1.8" fill="#2563eb" />
            <circle cx="24.5" cy="21" r="1.8" fill="#2563eb" />
            <rect x="18" y="26.5" width="1.8" height="2.8" rx="0.5" fill="#2563eb" />
            <rect x="20.2" y="26.5" width="1.8" height="2.8" rx="0.5" fill="#2563eb" />
          </svg>
        </div>
        <span class="brand-name">RiverFlow</span>
      </div>

      <div class="brand-content">
        <!--<div class="version-badge">
          <el-icon><Promotion /></el-icon>
          <span>全新版本 1.0 现已发布</span>
        </div>-->

        <h1 class="brand-title">
          接口流程编排平台
        </h1>
        <p class="brand-desc">
          让流程设计、调度、监控更简单，为您的团队提供高效、直观的流程编排体验。
        </p>

        <div class="feature-cards">
          <div class="feature-card">
            <el-icon><UserFilled /></el-icon>
            <span>团队协作管理</span>
          </div>
          <div class="feature-card">
            <el-icon><Box /></el-icon>
            <span>全链路流程守护</span>
          </div>
          <div class="feature-card">
            <el-icon><Lightning /></el-icon>
            <span>高性能调度引擎</span>
          </div>
        </div>
      </div>

      <div class="brand-footer">
        <span>© 2026 RiverFlow</span>
        <span class="dot">·</span>
        <a href="javascript:void(0)" @click.prevent="ElMessage.info('隐私政策页面即将上线')">隐私政策</a>
        <a href="javascript:void(0)" @click.prevent="ElMessage.info('服务条款页面即将上线')">服务条款</a>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="form-panel">
      <div class="form-header">
        <button
          class="theme-toggle"
          type="button"
          aria-label="切换主题"
          @click="toggleTheme"
        >
          <el-icon v-if="isDark"><Sunny /></el-icon>
          <el-icon v-else><Moon /></el-icon>
        </button>
      </div>

      <div class="form-card">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-subtitle">请输入您的账户信息以继续</p>

        <div class="login-tabs">
          <div class="login-tab active">
            <el-icon><User /></el-icon>
            <span>账户登录</span>
          </div>
          <div
            class="login-tab disabled"
            title="API 密钥授权功能即将上线"
          >
            <el-icon><Key /></el-icon>
            <span>API 密钥授权</span>
          </div>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
              clearable
            >
              <template #suffix>
                <button
                  type="button"
                  class="captcha-btn"
                  :class="{ verified: captchaVerified }"
                  aria-label="安全验证"
                  @click="openCaptcha"
                >
                  <svg
                    v-if="captchaVerified"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"
                      fill="currentColor"
                      fill-opacity="0.15"
                    />
                    <path d="M9 12l2 2 4-4" />
                  </svg>
                  <svg
                    v-else
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                </button>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe" size="default">记住我</el-checkbox>
            <a
              href="javascript:void(0)"
              class="forgot-link"
              @click.prevent="ElMessage.info('请联系管理员重置密码')"
            >
              忘记密码？
            </a>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              立即登录
              <el-icon class="btn-icon"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <p>2026 RiverFlow · 河狸流程编排平台</p>
        </div>
      </div>
    </div>

    <!-- 行为验证码弹窗 -->
    <CaptchaModal
      :visible="captchaVisible"
      @verify-success="onCaptchaSuccess"
      @verify-fail="onCaptchaFail"
      @close="captchaVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Key,
  Promotion,
  UserFilled,
  Box,
  Lightning,
  Sunny,
  Moon,
  ArrowRight
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { login, getUserInfo } from '@/api/auth'
import CaptchaModal from '@/components/CaptchaModal/index.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const captchaVisible = ref(false)
const isDark = ref(false)
const captchaVerified = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: 'admin',
  password: '',
  captchaToken: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function toggleTheme() {
  isDark.value = !isDark.value
}

function openCaptcha() {
  captchaVisible.value = true
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.captchaToken) {
    captchaVisible.value = true
    return
  }

  await doLogin(form.captchaToken)
}

async function doLogin(captchaToken) {
  loading.value = true
  try {
    const res = await login({
      username: form.username,
      password: form.password,
      captchaToken
    })
    userStore.setToken(res.token)
    userStore.setUserInfo({
      username: res.username,
      realName: res.realName
    })
    try {
      const userInfo = await getUserInfo()
      userStore.setUserInfo(userInfo)
    } catch (e) {
      // 使用登录返回的基础信息
    }
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    // 错误已在 request 拦截器中提示
    // 登录失败后再次登录需要重新验证验证码
    form.captchaToken = ''
    captchaVerified.value = false
  } finally {
    loading.value = false
  }
}

async function onCaptchaSuccess(captchaToken) {
  captchaVisible.value = false
  form.captchaToken = captchaToken
  captchaVerified.value = true
  await doLogin(captchaToken)
}

function onCaptchaFail() {
  ElMessage.error('验证码验证失败，请重试')
  form.captchaToken = ''
  captchaVerified.value = false
}
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  min-height: 100vh;
  background: #f7f8fa;
  overflow: hidden;
}

/* 左侧品牌区 */
.brand-panel {
  position: relative;
  flex: 1.4;
  display: flex;
  flex-direction: column;
  padding: 48px 56px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 50%, #1e3a8a 100%);
  overflow: hidden;
  min-width: 460px;
}

.brand-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 20% 25%, rgba(255, 255, 255, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 80% 75%, rgba(255, 255, 255, 0.08) 0%, transparent 40%);
}

.brand-header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

.brand-name {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.brand-content {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 560px;
  z-index: 1;
  padding: 40px 0;
}

.version-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 32px;

  .el-icon {
    font-size: 14px;
  }
}

.brand-title {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.12;
  margin: 0 0 24px;
  letter-spacing: -1.5px;
}

.brand-desc {
  font-size: 16px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 40px;
  max-width: 480px;
}

.feature-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.95);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    background: rgba(255, 255, 255, 0.16);
    transform: translateY(-2px);
  }

  .el-icon {
    font-size: 20px;
    color: rgba(255, 255, 255, 0.9);
  }
}

.brand-footer {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  z-index: 1;

  a {
    color: rgba(255, 255, 255, 0.7);
    text-decoration: none;
    transition: color 0.2s;

    &:hover {
      color: #fff;
    }
  }

  .dot {
    opacity: 0.5;
  }
}

/* 右侧表单区 */
.form-panel {
  --fp-bg: #ffffff;
  --fp-text-main: #1e293b;
  --fp-text-secondary: #64748b;
  --fp-text-muted: #94a3b8;
  --fp-border: #e2e8f0;
  --fp-border-hover: #cbd5e1;
  --fp-input-bg: #ffffff;
  --fp-tabs-bg: #f1f5f9;
  --fp-tab-active-bg: #ffffff;
  --fp-tab-active-text: #1e293b;
  --fp-tab-text: #64748b;

  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: var(--fp-bg);
  overflow: auto;
  transition: background 0.3s ease, color 0.3s ease;

  &.is-dark-panel {
    --fp-bg: #0f172a;
    --fp-text-main: #f8fafc;
    --fp-text-secondary: #94a3b8;
    --fp-text-muted: #64748b;
    --fp-border: #334155;
    --fp-border-hover: #475569;
    --fp-input-bg: #1e293b;
    --fp-tabs-bg: #1e293b;
    --fp-tab-active-bg: #334155;
    --fp-tab-active-text: #f8fafc;
    --fp-tab-text: #94a3b8;
  }
}

.form-header {
  position: absolute;
  top: 32px;
  right: 32px;
  z-index: 2;
}

.theme-toggle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid var(--fp-border);
  background: var(--fp-input-bg);
  color: var(--fp-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    border-color: var(--fp-border-hover);
    color: var(--fp-text-main);
    transform: rotate(15deg);
  }

  .el-icon {
    font-size: 18px;
  }
}

.form-card {
  width: 100%;
  max-width: 420px;
  animation: formIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.form-title {
  font-size: 32px;
  font-weight: 800;
  color: var(--fp-text-main);
  margin: 0 0 8px;
  letter-spacing: -0.8px;
  transition: color 0.3s ease;
}

.form-subtitle {
  font-size: 15px;
  color: var(--fp-text-secondary);
  margin: 0 0 32px;
  transition: color 0.3s ease;
}

.login-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 28px;
  padding: 4px;
  background: var(--fp-tabs-bg);
  border-radius: 12px;
  border: 1px solid var(--fp-border);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.login-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: var(--fp-tab-text);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;

  &.active {
    background: var(--fp-tab-active-bg);
    color: var(--fp-tab-active-text);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  }

  &:not(.active):hover {
    color: var(--fp-text-main);
    background: rgba(255, 255, 255, 0.5);
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.65;

    &:hover {
      background: transparent;
    }
  }

  .el-icon {
    font-size: 16px;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 0 0 1px var(--fp-border) inset;
    padding: 6px 14px;
    background: var(--fp-input-bg);
    transition: all 0.2s ease, background 0.3s ease;

    &:hover {
      box-shadow: 0 0 0 1px var(--fp-border-hover) inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #2563eb inset, 0 0 0 3px rgba(37, 99, 235, 0.12);
    }
  }

  :deep(.el-input__inner) {
    height: 46px;
    font-size: 15px;
    color: var(--fp-text-main);
    transition: color 0.3s ease;
  }

  :deep(.el-input__icon) {
    color: var(--fp-text-muted);
    transition: color 0.3s ease;
  }

  :deep(.el-input__suffix-inner) {
    color: var(--fp-text-muted);
  }

  .form-options {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;

    :deep(.el-checkbox__label) {
      color: var(--fp-text-secondary);
      font-size: 13px;
      transition: color 0.3s ease;
    }

    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background: #2563eb;
      border-color: #2563eb;
    }

    .forgot-link {
      font-size: 13px;
      color: var(--fp-text-secondary);
      text-decoration: none;
      transition: color 0.2s;

      &:hover {
        color: #2563eb;
      }
    }
  }

  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    background: #2563eb;
    border: none;
    transition: all 0.2s ease;

    &:hover {
      background: #1d4ed8;
      transform: translateY(-1px);
      box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25);
    }

    &:active {
      transform: translateY(0) scale(0.995);
    }

    .btn-icon {
      margin-left: 6px;
      font-size: 16px;
    }
  }
}

.captcha-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  margin-right: -4px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--fp-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0;

  &:hover {
    background: rgba(37, 99, 235, 0.08);
    color: #2563eb;
  }

  &.verified {
    color: #10b981;

    &:hover {
      background: rgba(16, 185, 129, 0.08);
      color: #059669;
    }
  }

  svg {
    flex-shrink: 0;
  }
}

.form-footer {
  margin-top: 32px;
  text-align: center;

  p {
    font-size: 12px;
    color: var(--fp-text-muted);
    margin: 0;
    transition: color 0.3s ease;
  }
}

/* 动画 */
@keyframes formIn {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 响应式：小屏幕隐藏左侧 */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    padding: 24px;
    min-height: 100vh;
  }

  .form-card {
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .form-title {
    font-size: 26px;
  }

  .login-tabs {
    .login-tab {
      padding: 10px 8px;
      font-size: 13px;
    }
  }
}
</style>
