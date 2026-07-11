<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">403</h1>
      <p class="error-title">抱歉，您没有权限访问该页面</p>
      <p class="error-desc">请联系管理员分配相应菜单权限</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped lang="scss">
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;

  .error-content {
    text-align: center;
    padding: 48px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);

    .error-code {
      font-size: 96px;
      font-weight: 700;
      color: #2563eb;
      margin: 0 0 16px;
      line-height: 1;
    }

    .error-title {
      font-size: 20px;
      color: #1f2937;
      margin: 0 0 8px;
    }

    .error-desc {
      font-size: 14px;
      color: #6b7280;
      margin: 0 0 24px;
    }
  }
}
</style>
