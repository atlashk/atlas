// Performance monitoring utility for authentication operations
interface PerformanceMetrics {
  profileFetchCount: number
  tokenRefreshCount: number
  navigationGuardTime: number[]
  lastProfileFetch: number | null
}

class PerformanceMonitor {
  private metrics: PerformanceMetrics = {
    profileFetchCount: 0,
    tokenRefreshCount: 0,
    navigationGuardTime: [],
    lastProfileFetch: null
  }

  private isDevelopment = import.meta.env.DEV

  trackProfileFetch(): void {
    if (!this.isDevelopment) return

    this.metrics.profileFetchCount++
    this.metrics.lastProfileFetch = Date.now()

    if (this.metrics.profileFetchCount > 10) {
      console.warn(`High profile fetch count: ${this.metrics.profileFetchCount}. Consider optimizing.`)
    }
  }

  trackTokenRefresh(): void {
    if (!this.isDevelopment) return

    this.metrics.tokenRefreshCount++

    if (this.metrics.tokenRefreshCount > 5) {
      console.warn(`High token refresh count: ${this.metrics.tokenRefreshCount}. Check token expiry settings.`)
    }
  }

  trackNavigationGuard(duration: number): void {
    if (!this.isDevelopment) return

    this.metrics.navigationGuardTime.push(duration)

    // Keep only last 10 measurements
    if (this.metrics.navigationGuardTime.length > 10) {
      this.metrics.navigationGuardTime.shift()
    }

    const avgTime = this.metrics.navigationGuardTime.reduce((a, b) => a + b, 0) / this.metrics.navigationGuardTime.length

    if (avgTime > 1000) { // More than 1 second
      console.warn(`Slow navigation guard average: ${avgTime.toFixed(2)}ms`)
    }
  }

  getMetrics(): PerformanceMetrics {
    return { ...this.metrics }
  }

  reset(): void {
    this.metrics = {
      profileFetchCount: 0,
      tokenRefreshCount: 0,
      navigationGuardTime: [],
      lastProfileFetch: null
    }
  }
}

export const performanceMonitor = new PerformanceMonitor() 