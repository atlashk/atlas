import type { ApiResponse } from '@/interfaces/api.interface'
import type { LoginRequest } from '@/interfaces/auth.interface'
import type { RegisterRequest, User } from '@/interfaces/user.interface'
import { userService } from '@/services/api/user.service'
import { performanceMonitor } from '@/utils/performance'
import { defineStore } from 'pinia'

interface UserState {
  profile: User | null
  accessToken: string | null
  refreshToken: string | null
  loading: boolean
  error: string | null
  profileLoading: boolean // Track profile loading state separately
}

// Request deduplication for profile fetching
let profileFetchPromise: Promise<void> | null = null

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    profile: null,
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    loading: false,
    error: null,
    profileLoading: false
  }),

  getters: {
    isAuthenticated: (state): boolean => !!state.accessToken,
    isAdmin: (state): boolean => state.profile?.role === 'ADMIN',
    fullName: (state): string =>
      state.profile ? `${state.profile.firstName} ${state.profile.lastName}` : '',
    hasRole: (state) => (role: string): boolean => state.profile?.role === role,
    // Performance optimization: compute auth state once
    authState: (state) => ({
      isAuthenticated: !!state.accessToken,
      isAdmin: state.profile?.role === 'ADMIN',
      hasProfile: !!state.profile,
      loading: state.loading || state.profileLoading
    })
  },

  actions: {
    async login(credentials: LoginRequest): Promise<ApiResponse<any>> {
      this.loading = true
      this.error = null

      try {
        // Import authService dynamically to avoid circular dependency
        const { authService } = await import('@/services/api/auth.service')
        const response = await authService.login(credentials)

        if (response.success && response.data) {
          this.setTokens(response.data.accessToken, response.data.refreshToken)
          await this.fetchProfile()
        }

        return response
      } catch (error) {
        this.error = 'Login failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    async register(userData: RegisterRequest): Promise<ApiResponse<any>> {
      this.loading = true
      this.error = null

      try {
        return await userService.register(userData)
      } catch (error) {
        this.error = 'Registration failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProfile(): Promise<void> {
      if (!this.isAuthenticated) return

      // Deduplicate concurrent profile fetch requests
      if (profileFetchPromise) {
        return profileFetchPromise
      }

      this.profileLoading = true

      profileFetchPromise = this._fetchProfileInternal()

      try {
        await profileFetchPromise
      } finally {
        this.profileLoading = false
        profileFetchPromise = null
      }
    },

    async _fetchProfileInternal(): Promise<void> {
      performanceMonitor.trackProfileFetch()

      try {
        const response = await userService.getProfile()
        if (response.success && response.data) {
          this.profile = response.data
        } else {
          // If response is not successful, clear auth state
          this.clearAuthState()
        }
      } catch (error: any) {
        console.error('Failed to fetch profile:', error)
        // If it's an authentication error, clear the auth state
        if (error?.response?.status === 401 || error?.status === 401) {
          this.clearAuthState()
        }
        throw error // Re-throw to let navigation guard handle it
      }
    },

    setTokens(accessToken: string, refreshToken: string): void {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
    },

    async logout(): Promise<void> {
      this.loading = true

      try {
        const { authService } = await import('@/services/api/auth.service')
        await authService.logout()
      } catch (error) {
        console.error('Logout API error:', error)
      } finally {
        // Clear user data after API call (whether successful or not)
        this.clearAuthState()
        this.loading = false
      }
    },

    clearError(): void {
      this.error = null
    },

    clearAuthState(): void {
      this.profile = null
      this.accessToken = null
      this.refreshToken = null
      this.error = null
      this.profileLoading = false
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      // Clear any pending profile fetch
      profileFetchPromise = null
    }
  }
})
