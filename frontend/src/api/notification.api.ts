import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";
import { InAppNotification, InAppServiceInfo } from "@/interfaces/notification.interface";

export class NotificationApi extends BaseApi {
  constructor() {
    super("/services/notification/api/notifications");
  }

  async getInAppServiceInfo(): Promise<ApiResponse<InAppServiceInfo>> {
    return this.get<InAppServiceInfo>("/inapp/service-info");
  }

  async listInAppNotifications(limit: number = 10): Promise<ApiResponse<InAppNotification[]>> {
    return this.get<InAppNotification[]>(`/inapp?limit=${limit}`);
  }

  async markAllAsRead(): Promise<ApiResponse<void>> {
    return this.post<void>("/inapp/mark-all-as-read");
  }
}

export const notificationApi = new NotificationApi();
