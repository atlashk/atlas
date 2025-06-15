import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user.store'
import { UserService } from '@/services/api/user.service'

export const authGuard = async (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) => {
  const userStore = useUserStore()

  try {
    // If we have a token but no profile, fetch it
    if (userStore.isAuthenticated && !userStore.profile) {
      await userStore.fetchProfile()
    }

    // Handle route protection
    if (to.meta.requiresAuth && !userStore.isAuthenticated) {
      return next('/login')
    }

    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      return next('/')
    }

    // Redirect authenticated users from login
    if (to.path === '/login' && userStore.isAuthenticated) {
      return next('/')
    }

    // Auto-redirect admin to dashboard
    if (to.path === '/' && userStore.isAdmin) {
      return next('/admin/dashboard')
    }

    return next()
  } catch (error) {
    console.error('Navigation guard error:', error)
    userStore.logout()
    return next('/login')
  }
} 