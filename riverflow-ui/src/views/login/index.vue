<template>
  <div class="login-page">
    <!-- 淡雅背景装饰 -->
    <div class="bg-decoration">
      <div class="deco-circle c1"></div>
      <div class="deco-circle c2"></div>
      <div class="deco-circle c3"></div>
      <div class="deco-dots"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="card-header">
        <div class="logo">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="10" fill="#f0f9ff" />
            <!-- 耳朵 -->
            <circle cx="12" cy="13" r="4.5" fill="#d97706" />
            <circle cx="28" cy="13" r="4.5" fill="#d97706" />
            <circle cx="12" cy="13" r="2.5" fill="#fcd34d" />
            <circle cx="28" cy="13" r="2.5" fill="#fcd34d" />
            <!-- 脸 -->
            <ellipse cx="20" cy="23" rx="11" ry="9" fill="#d97706" />
            <!-- 嘴部浅色区域 -->
            <ellipse cx="20" cy="25.5" rx="7" ry="5" fill="#fcd34d" />
            <!-- 眼睛 -->
            <circle cx="15.5" cy="21" r="1.8" fill="#1f2937" />
            <circle cx="24.5" cy="21" r="1.8" fill="#1f2937" />
            <circle cx="16.2" cy="20.3" r="0.6" fill="#ffffff" />
            <circle cx="25.2" cy="20.3" r="0.6" fill="#ffffff" />
            <!-- 大门牙 -->
            <rect x="18" y="26.5" width="1.8" height="2.8" rx="0.5" fill="#ffffff" />
            <rect x="20.2" y="26.5" width="1.8" height="2.8" rx="0.5" fill="#ffffff" />
            <!-- 鼻子 -->
            <ellipse cx="20" cy="24.5" rx="2.2" ry="1.6" fill="#92400e" />
          </svg>
        </div>
        <h1>RiverFlow</h1>
        <p class="subtitle">河狸流程编排平台</p>
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
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <p> 2024 RiverFlow · 河狸流程编排平台</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { login, getUserInfo } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
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
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;

  .deco-circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(100px);
    opacity: 0.4;
  }

  .c1 {
    width: 500px;
    height: 500px;
    background: #bae6fd;
    top: -150px;
    right: -100px;
  }

  .c2 {
    width: 400px;
    height: 400px;
    background: #bbf7d0;
    bottom: -120px;
    left: -80px;
  }

  .c3 {
    width: 300px;
    height: 300px;
    background: #fde68a;
    top: 40%;
    left: 60%;
    opacity: 0.25;
  }

  .deco-dots {
    position: absolute;
    inset: 0;
    background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
    background-size: 28px 28px;
    opacity: 0.35;
  }
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  margin: 40px 24px;
  padding: 48px 40px 36px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  box-shadow:
    0 1px 2px rgba(0, 0, 0, 0.02),
    0 4px 12px rgba(0, 0, 0, 0.03),
    0 16px 40px rgba(0, 0, 0, 0.04);
  animation: cardIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);

  .card-header {
    text-align: center;
    margin-bottom: 36px;

    .logo {
      display: inline-flex;
      margin-bottom: 20px;
    }

    h1 {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 6px;
      letter-spacing: -0.3px;
    }

    .subtitle {
      font-size: 14px;
      color: #94a3b8;
      margin: 0;
      font-weight: 400;
    }
  }

  .login-form {
    :deep(.el-input__wrapper) {
      border-radius: 12px;
      box-shadow: 0 0 0 1px #e2e8f0 inset;
      padding: 6px 14px;
      background: #ffffff;
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 0 0 1px #cbd5e1 inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px #38bdf8 inset, 0 0 0 3px rgba(56, 189, 248, 0.12);
      }
    }

    :deep(.el-input__inner) {
      height: 46px;
      font-size: 15px;
      color: #334155;
    }

    :deep(.el-input__icon) {
      color: #94a3b8;
    }

    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 500;
      border-radius: 12px;
      margin-top: 4px;
      background: #0ea5e9;
      border: none;
      transition: all 0.2s ease;
      letter-spacing: 2px;

      &:hover {
        background: #0284c7;
        transform: translateY(-1px);
        box-shadow: 0 8px 20px rgba(14, 165, 233, 0.25);
      }

      &:active {
        transform: translateY(0) scale(0.995);
      }
    }
  }

  .card-footer {
    margin-top: 32px;
    text-align: center;

    p {
      font-size: 12px;
      color: #cbd5e1;
      margin: 0;
    }
  }
}

/* 动画 */
@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
