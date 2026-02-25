import type {
  AddCartItemRequest,
  CartResponse,
  CheckoutRequest,
  CheckoutResponse,
  Order,
  OrderStatusResponse,
  RetrieveOrderListFilter,
  UpdateCartItemRequest
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

  async retrieveOrderList(filters: RetrieveOrderListFilter): Promise<ApiResponse<Order[]>> {
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

  async retrieveOrderStatus(orderId: string): Promise<ApiResponse<OrderStatusResponse>> {
    return this.get<OrderStatusResponse>(`/orders/${orderId}/status`);
  }

  async retrieveReferenceData(type: string): Promise<ApiResponse<Record<string, string>>> {
    return this.get<Record<string, string>>(`/reference-data?type=${type}`);
  }

  async retrieveOrderStatuses(): Promise<ApiResponse<Record<string, string>>> {
    return this.retrieveReferenceData('ORDER_STATUS');
  }
}

export const orderApi = new OrderApi();
