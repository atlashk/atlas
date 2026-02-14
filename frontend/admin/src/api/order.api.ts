import type {
  RetrieveOrderFilter,
  Order
} from "@/interfaces/order.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class OrderAdminApi extends BaseApi {
  constructor() {
    super("/services/order/api/admin");
  }

  async retrieveOrderList(filters: RetrieveOrderFilter): Promise<ApiResponse<Order[]>> {
    const payload = {
      id: filters.id ? filters.id.toString() : undefined,
      userId: filters.userId ? filters.userId.toString() : undefined,
      productId: filters.productId ? filters.productId.toString() : undefined,
      status: filters.status,
      startDate: filters.startDate,
      endDate: filters.endDate,
      page: filters.page || 1,
      size: filters.size || 20,
    };
    return this.post<Order[], typeof payload>("/orders/list", payload);
  }

  async retrieveOrderCount(): Promise<ApiResponse<number>> {
    return this.get<number>("/orders/statistics/count");
  }

  async retrieveTotalRevenue(): Promise<ApiResponse<number>> {
    return this.get<number>("/orders/statistics/total-revenue");
  }

  async retrieveMonthlyOrderStatistics(): Promise<
    ApiResponse<{ year: number; month: number; totalRevenue: number }[]>
  > {
    return this.get<{ year: number; month: number; totalRevenue: number }[]>(
      "/orders/statistics/monthly"
    );
  }
}

export const orderAdminApi = new OrderAdminApi();
