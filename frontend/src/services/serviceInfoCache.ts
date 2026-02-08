import { notificationApi } from "@/api/notification.api";
import { InAppServiceInfo } from "@/interfaces/notification.interface";

interface CachedServiceInfo {
  data: InAppServiceInfo;
  timestamp: number;
}

class ServiceInfoCache {
  private cache: CachedServiceInfo | null = null;
  private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
  private fetchPromise: Promise<InAppServiceInfo> | null = null;

  async getServiceInfo(): Promise<InAppServiceInfo> {
    // Nếu đang có request đang xử lý, return promise đó để tránh duplicate calls
    if (this.fetchPromise) {
      return this.fetchPromise;
    }

    // Kiểm tra cache còn hạn không
    if (this.cache && this.isCacheValid()) {
      return this.cache.data;
    }

    // Tạo promise mới để fetch data
    this.fetchPromise = this.fetchServiceInfo();
    
    try {
      const data = await this.fetchPromise;
      this.cache = {
        data,
        timestamp: Date.now()
      };
      return data;
    } finally {
      // Clear promise sau khi hoàn thành
      this.fetchPromise = null;
    }
  }

  private async fetchServiceInfo(): Promise<InAppServiceInfo> {
    const response = await notificationApi.retrieveInAppServiceInfo();
    if (!response.success || !response.data) {
      throw new Error(response.errorMessage || 'Failed to fetch service info');
    }
    return response.data;
  }

  private isCacheValid(): boolean {
    if (!this.cache) return false;
    const now = Date.now();
    return now - this.cache.timestamp < this.CACHE_DURATION;
  }

  // Clear cache khi cần (ví dụ: khi logout)
  clearCache(): void {
    this.cache = null;
    this.fetchPromise = null;
  }
}

// Export singleton instance
export const serviceInfoCache = new ServiceInfoCache();
