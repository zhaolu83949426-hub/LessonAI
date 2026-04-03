import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue')
      },
      {
        path: 'templates',
        name: 'TemplateList',
        component: () => import('@/views/template/list.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue')
  },
  {
    path: '/session/new',
    name: 'SessionNew',
    component: () => import('@/views/session/new.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/session/:id',
    name: 'SessionDetail',
    component: () => import('@/views/session/detail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/templates/:id/edit',
    component: () => import('@/views/template/edit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/session/:id/record',
    name: 'RecordEdit',
    component: () => import('@/views/record/edit.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
