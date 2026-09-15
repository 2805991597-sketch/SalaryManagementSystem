import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Admin from '../views/Admin.vue'

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/', name: 'Admin', component: Admin },
  { path: '/hr', name: 'HR', component: Admin },
  { path: '/user', name: 'User', component: Admin }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStr = sessionStorage.getItem('user')
  if (to.path !== '/login' && !userStr) {
    next('/login')
    return
  }
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      const role = user.roleCode
      // 按角色重定向到对应首页
      if (to.path === '/' && role !== 'admin') {
        next(role === 'hr' ? '/hr' : '/user')
        return
      }
      if (to.path === '/hr' && role !== 'hr') {
        next(role === 'admin' ? '/' : '/user')
        return
      }
      if (to.path === '/user' && role !== 'user') {
        next(role === 'admin' ? '/' : '/hr')
        return
      }
    } catch (e) {}
  }
  next()
})

export default router
