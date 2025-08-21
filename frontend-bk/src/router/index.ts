import AuthLayout from '@/layouts/AuthLayout.vue';
import DefaultLayout from '@/layouts/DefaultLayout.vue';
import { useUserStore } from '@/stores/user.store';
import { performanceMonitor } from '@/utils/performance';
import { createRouter, createWebHistory } from 'vue-router';
import { toast } from 'vue3-toastify';

const routes = [
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        path: '',
        name: 'storeFront',
        component: () => import('@/pages/front/StoreFront.vue'),
      },
      {
        path: '/admin/dashboard',
        name: 'adminDashboard',
        component: () => import('@/pages/admin/Dashboard.vue'),
      },
      {
        path: '/admin/user',
        name: 'adminUserList',
        component: () => import('@/pages/admin/UserList.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: '/admin/product',
        name: 'adminProductList',
        component: () => import('@/pages/admin/ProductList.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: '/admin/product/:id',
        name: 'adminProductInfo',
        component: () => import('@/pages/admin/ProductInfo.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: '/admin/product/add',
        name: 'adminProductAdd',
        component: () => import('@/pages/admin/ProductAdd.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: '/admin/product/:id/edit',
        name: 'adminProductEdit',
        component: () => import('@/pages/admin/ProductEdit.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: '/admin/order',
        name: 'adminOrderList',
        component: () => import('@/pages/admin/OrderList.vue'),
        meta: { requiresAdmin: true }
      },
    ],
  },
  {
    path: '/auth',
    component: AuthLayout,
    children: [
      {
        path: '/login',
        name: 'login',
        component: () => import('@/pages/auth/Login.vue'),
      },
      {
        path: '/register',
        name: 'register',
        component: () => import('@/pages/auth/Register.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const startTime = performance.now()
  const userStore = useUserStore()

  // Handle authentication/authorization
  try {
    // Always fetch profile if we have a token but no profile
    // This ensures profile is loaded even on public route refreshes
    if (userStore.isAuthenticated && !userStore.profile) {
      await userStore.fetchProfile()
    }

    // Early return for public routes that don't need authentication
    // But only after we've tried to fetch profile if authenticated
    if (!to.meta.requiresAuth && !to.meta.requiresAdmin && to.path !== '/login') {
      // Handle admin auto-redirect from home page
      if (to.path === '/' && userStore.isAuthenticated && userStore.profile?.role === 'ADMIN') {
        performanceMonitor.trackNavigationGuard(performance.now() - startTime)
        return next({ name: 'adminDashboard' })
      }

      performanceMonitor.trackNavigationGuard(performance.now() - startTime)
      return next()
    }

    // Check authentication requirements
    const needsAuth = to.meta.requiresAuth || to.meta.requiresAdmin
    const isAuthenticated = userStore.isAuthenticated

    // Redirect unauthenticated users from protected routes
    if (needsAuth && !isAuthenticated) {
      performanceMonitor.trackNavigationGuard(performance.now() - startTime)
      return next('/login')
    }

    // Redirect non-admin users from admin routes
    if (to.meta.requiresAdmin && userStore.profile?.role !== 'ADMIN') {
      performanceMonitor.trackNavigationGuard(performance.now() - startTime)
      return next('/')
    }

    // Redirect authenticated users from login page
    if (to.path === '/login' && isAuthenticated) {
      performanceMonitor.trackNavigationGuard(performance.now() - startTime)
      return next('/')
    }

    performanceMonitor.trackNavigationGuard(performance.now() - startTime)
    return next()
  } catch (error) {
    console.error('Navigation error:', error)
    // Clear authentication state and redirect to login
    await userStore.logout()
    toast.error('Authentication failed. Please log in again.')
    performanceMonitor.trackNavigationGuard(performance.now() - startTime)
    return next('/login')
  }
})

export default router
