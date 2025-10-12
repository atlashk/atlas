import type {
  CheckoutRequest,
  CheckoutResponse,
  GetOrderStatusResponse,
  ListOrderFilters,
  Order
} from "@/interfaces/order.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./baseApi";

export class OrderApi extends BaseApi {
  constructor() {
    super("/api/order-svc");
  }

  async listOrder(filters: ListOrderFilters): Promise<ApiResponse<Order[]>> {
    const queryParams = new URLSearchParams();
    if (filters.orderId) {
      queryParams.append("orderId", filters.orderId.toString());
    }
    if (filters.status) {
      queryParams.append("status", filters.status);
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

  async checkout(
    data: CheckoutRequest
  ): Promise<ApiResponse<CheckoutResponse>> {
    return this.post<CheckoutResponse>("/orders/checkout", data);
  }

  async getOrderStatus(
    orderId: number
  ): Promise<ApiResponse<GetOrderStatusResponse>> {
    return this.get<GetOrderStatusResponse>(`/orders/${orderId}/status`);
  }
}

export const orderApi = new OrderApi();
