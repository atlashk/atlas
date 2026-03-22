import type {
  AddCartItemRequest,
  CartResponse,
  CheckoutRequest,
  CheckoutResponse,
  Order,
  OrderStatusResponse,
  RetrieveOrderListFilter,
  UpdateCartItemRequest,
} from "@/interfaces/order.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class OrderApi extends BaseApi {
  constructor() {
    super("/services/order/api");
  }

  async retrieveCart(): Promise<ApiResponse<CartResponse>> {
    return this.get<CartResponse>("/carts");
  }

  async addCartItem(
    request: AddCartItemRequest,
  ): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>("/carts/items/add", request);
  }

  async updateCartItem(
    productId: string,
    request: UpdateCartItemRequest,
  ): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>(`/carts/items/${productId}/update`, request);
  }

  async removeCartItem(productId: string): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>(`/carts/items/${productId}/remove`);
  }

  async clearCart(): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>("/carts/clear");
  }

  async retrieveOrderList(
    filter: RetrieveOrderListFilter,
  ): Promise<ApiResponse<Order[]>> {
    const payload = {
      status: filter.status,
      startDate: filter.startDate,
      endDate: filter.endDate,
      page: filter.page || 1,
      size: filter.size || 20,
    };
    return this.post<Order[], typeof payload>("/orders", payload);
  }

  async retrieveAdminOrderList(
    filters: RetrieveOrderListFilter,
  ): Promise<ApiResponse<Order[]>> {
    const payload = {
      id: filters.id ? filters.id.toString() : undefined,
      orderId: filters.orderId ? filters.orderId.toString() : undefined,
      userId: filters.userId ? filters.userId.toString() : undefined,
      productId: filters.productId ? filters.productId.toString() : undefined,
      status: filters.status,
      startDate: filters.startDate,
      endDate: filters.endDate,
      page: filters.page || 1,
      size: filters.size || 20,
    };
    return this.post<Order[], typeof payload>("/orders/admin/list", payload);
  }

  async checkout(
    data: CheckoutRequest,
  ): Promise<ApiResponse<CheckoutResponse>> {
    return this.post<CheckoutResponse>("/orders/checkout", data);
  }

  async retrieveOrderStatus(
    orderId: string,
  ): Promise<ApiResponse<OrderStatusResponse>> {
    return this.get<OrderStatusResponse>(`/orders/${orderId}/status`);
  }

  async retrieveReferenceData(
    type: string,
  ): Promise<ApiResponse<Record<string, string>>> {
    return this.get<Record<string, string>>(
      `/public/reference-data?type=${type}`,
    );
  }

  async retrieveOrderStatuses(): Promise<ApiResponse<Record<string, string>>> {
    return this.retrieveReferenceData("ORDER_STATUS");
  }

  async retrieveTotalOrderCount(): Promise<ApiResponse<number>> {
    return this.get<number>("/orders/admin/statistics/count");
  }

  async retrieveTotalRevenue(): Promise<ApiResponse<number>> {
    return this.get<number>("/orders/admin/statistics/total-revenue");
  }

  async retrieveMonthlyOrderStatistics(): Promise<
    ApiResponse<{ year: number; month: number; totalRevenue: number }[]>
  > {
    return this.get<{ year: number; month: number; totalRevenue: number }[]>(
      "/orders/admin/statistics/monthly",
    );
  }
}

export const orderApi = new OrderApi();
