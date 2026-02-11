import type {
  CheckoutRequest,
  CheckoutResponse,
  ListOrderFilters,
  Order,
  OrderStatusResponse
} from "@/interfaces/order.interface";
import type {
  AddCartItemRequest,
  CartResponse,
  UpdateCartItemRequest
} from "@/interfaces/cart.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class OrderFrontApi extends BaseApi {
  constructor() {
    super("/services/order/api/front");
  }

  async retrieveOrderList(filters: ListOrderFilters): Promise<ApiResponse<Order[]>> {
    const payload = {
      status: filters.status,
      startDate: filters.startDate,
      endDate: filters.endDate,
      page: filters.page || 1,
      size: filters.size || 20,
    };
    return this.post<Order[], typeof payload>("/orders", payload);
  }

  async checkout(
    data: CheckoutRequest
  ): Promise<ApiResponse<CheckoutResponse>> {
    return this.post<CheckoutResponse>("/orders/checkout", data);
  }

  async getOrderStatus(orderId: string): Promise<ApiResponse<OrderStatusResponse>> {
    return this.get<OrderStatusResponse>(`/orders/${orderId}/status`);
  }

  async retrieveCart(): Promise<ApiResponse<CartResponse>> {
    return this.get<CartResponse>("/carts");
  }

  async addCartItem(request: AddCartItemRequest): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>("/carts/items/add", request);
  }

  async updateCartItem(
    productId: number,
    request: UpdateCartItemRequest
  ): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>(`/carts/items/${productId}/update`, request);
  }

  async removeCartItem(productId: number): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>(`/carts/items/${productId}/remove`);
  }

  async clearCart(): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>("/carts/clear");
  }
}

export class OrderAdminApi extends BaseApi {
  constructor() {
    super("/services/order/api/admin");
  }

  async retrieveOrderList(filters: ListOrderFilters): Promise<ApiResponse<Order[]>> {
    const payload = {
      id: filters.orderId ? filters.orderId.toString() : undefined,
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

export const orderFrontApi = new OrderFrontApi();
export const orderAdminApi = new OrderAdminApi();
