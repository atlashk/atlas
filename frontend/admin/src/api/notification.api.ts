import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";
import { InAppNotification, InAppServiceInfo } from "@/interfaces/notification.interface";

export class NotificationApi extends BaseApi {
  constructor() {
    super("/services/notification/api");
  }

  async retrieveInAppServiceInfo(): Promise<ApiResponse<InAppServiceInfo>> {
    return this.get<InAppServiceInfo>("/notifications/inapp/service-info");
  }

  async retrieveInAppNotification(limit: number = 10): Promise<ApiResponse<InAppNotification[]>> {
    return this.get<InAppNotification[]>(`/notifications/inapp?limit=${limit}`);
  }

  async markAllAsRead(): Promise<ApiResponse<void>> {
    return this.post<void>("/notifications/inapp/mark-all-as-read");
  }
}

export const notificationApi = new NotificationApi();
