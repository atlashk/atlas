import type { ListOrderFilters, Order } from "@/interfaces/order.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./baseApi";

export class OrderAdminApi extends BaseApi {
  constructor() {
    super("/api/order-svc/admin");
  }

  async listOrder(filters: ListOrderFilters): Promise<ApiResponse<Order[]>> {
    const queryParams = new URLSearchParams();  
    if (filters.status) {
      queryParams.append("status", filters.status);
    }
    if (filters.userId) {
      queryParams.append("userId", filters.userId.toString());
    }
    if (filters.productId) {
      queryParams.append("productId", filters.productId.toString());
    }
    if (filters.startDate) {
      queryParams.append("startDate", filters.startDate);
    }
    if (filters.endDate) {
      queryParams.append("endDate", filters.endDate);
    }
    queryParams.append("page", (filters.page || 1).toString());
    queryParams.append("size", (filters.size || 20).toString());

    return this.get<Order[]>(`/orders?${queryParams.toString()}`);
  }

  async countOrder(): Promise<ApiResponse<number>> {
    return this.get<number>("/orders/statistics/count");
  }

  async getTotalRevenue(): Promise<ApiResponse<number>> {
    return this.get<number>("/orders/statistics/total-revenue");
  }
}

export const orderAdminApi = new OrderAdminApi();
