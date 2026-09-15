<template>
  <div class="login-page">
    <div class="bg-decoration"></div>
    <div class="login-wrapper">
      <div class="login-container">
        <div class="login-header">
          <div class="login-icon">
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3-8H9V7h6v2z"/>
            </svg>
          </div>
          <h2>工资管理系统</h2>
          <p>请登录您的账号</p>
        </div>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="0px">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入登录账号" @keyup.enter="handleLogin">
              <template #prefix>
                <svg width="16" height="16" fill="#94a3b8" viewBox="0 0 24 24" style="margin-right: 4px;">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入登录密码" @keyup.enter="handleLogin">
              <template #prefix>
                <svg width="16" height="16" fill="#94a3b8" viewBox="0 0 24 24" style="margin-right: 4px;">
                  <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="login-tips">
          <p>测试账号：admin / admin（管理员）</p>
          <p>hr / hr（HR） | user1 / user1（员工）</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form)
      if (res.code === 200) {
        sessionStorage.setItem('user', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        const roleCode = res.data.roleCode
        if (roleCode === 'admin') {
          router.push('/')
        } else if (roleCode === 'hr') {
          router.push('/hr')
        } else {
          router.push('/user')
        }
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (e) {
      ElMessage.error('登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  overflow: hidden;
  position: relative;
}
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}
.bg-decoration::before,
.bg-decoration::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.1);
}
.bg-decoration::before {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -100px;
  animation: float 20s ease-in-out infinite;
}
.bg-decoration::after {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
  animation: float 15s ease-in-out infinite reverse;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}
.login-wrapper {
  position: relative;
  z-index: 1;
}
.login-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.4);
  padding: 48px 40px;
  width: 420px;
  transition: transform 0.3s ease;
}
.login-container:hover {
  transform: translateY(-5px);
}
.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.login-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
}
.login-icon svg {
  width: 44px;
  height: 44px;
  fill: white;
}
.login-header h2 {
  color: #1e293b;
  font-size: 26px;
  margin-bottom: 8px;
  font-weight: 600;
}
.login-header p {
  color: #64748b;
  font-size: 14px;
}
.el-form-item {
  margin-bottom: 24px;
}
.el-form-item :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 500;
}
.el-input {
  --el-input-bg-color: #f8fafc;
  --el-input-border-color: #e2e8f0;
  --el-input-hover-border-color: #3b82f6;
  --el-input-focus-border-color: #3b82f6;
  border-radius: 12px;
}
.el-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: none !important;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 4px 16px;
}
.el-input :deep(.el-input__inner) {
  height: 40px;
}
.el-input :deep(.el-input__wrapper:hover) {
  border-color: #3b82f6;
}
.el-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  background: #fff;
}
.el-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}
.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(59, 130, 246, 0.5);
}
.el-button:active {
  transform: translateY(0);
}
.login-tips {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}
.login-tips p {
  color: #94a3b8;
  font-size: 13px;
  line-height: 1.8;
}
</style>
