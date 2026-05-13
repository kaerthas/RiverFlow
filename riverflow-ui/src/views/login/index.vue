<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="48" color="#1677FF"><ElementPlus /></el-icon>
        <h1>RiverFlow · 河狸</h1>
        <p>可视化流程编排平台</p>
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
    </div>
    <div class="login-footer">
      <p>© 2024 RiverFlow · 河狸流程编排平台</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'

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

function handleLogin() {
  formRef.value.validate((valid) => {
    if (!valid) return
    loading.value = true
    // TODO: 调用登录接口
    setTimeout(() => {
      userStore.setToken('mock-jwt-token-' + Date.now())
      ElMessage.success('登录成功')
      router.push('/')
      loading.value = false
    }, 800)
  })
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E6F4FF 0%, #F0F2F5 100%);
  position: relative;

  .login-box {
    width: 420px;
    padding: 40px;
    background: #FFFFFF;
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

    .login-header {
      text-align: center;
      margin-bottom: 32px;

      h1 {
        margin: 16px 0 8px;
        font-size: 24px;
        font-weight: 600;
        color: #262626;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #8C8C8C;
      }
    }

    .login-form {
      .login-btn {
        width: 100%;
        height: 44px;
        font-size: 16px;
        border-radius: 6px;
      }
    }
  }

  .login-footer {
    position: absolute;
    bottom: 24px;
    color: #8C8C8C;
    font-size: 13px;
  }
}
</style>
