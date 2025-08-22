import type { ApiResponse } from "@/interfaces/api.interface";
import type {
  GetOrderStatusResponse,
  ListOrderFilters,
  Order,
  PlaceOrderRequest,
  PlaceOrderResponse,
} from "@/interfaces/order.interface";
import { BaseService } from "./base.service";

export class OrderService extends BaseService {
  constructor() {
    super("/api");
  }

  // Front operations
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

    return this.get<Order[]>(`/front/orders?${queryParams.toString()}`);
  }

  async placeOrder(
    data: PlaceOrderRequest
  ): Promise<ApiResponse<PlaceOrderResponse>> {
    return this.post<PlaceOrderResponse>("/front/orders/place", data);
  }

  async getOrderStatus(
    orderId: number
  ): Promise<ApiResponse<GetOrderStatusResponse>> {
    return this.get<GetOrderStatusResponse>(`/front/orders/${orderId}/status`);
  }

  // Admin operations - keeping these as they might be different from front service
  async adminListOrders(
    filters: ListOrderFilters
  ): Promise<ApiResponse<Order[]>> {
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

    return this.get<Order[]>(`/admin/orders?${queryParams.toString()}`);
  }
}

export const orderService = new OrderService();
