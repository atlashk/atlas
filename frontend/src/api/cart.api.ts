import type {
  AddCartItemRequest,
  CartResponse,
  UpdateCartItemRequest,
} from "@/interfaces/cart.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class CartApi extends BaseApi {
  constructor() {
    super("/api/user-svc/carts");
  }

  async getCart(): Promise<ApiResponse<CartResponse>> {
    return this.get<CartResponse>("");
  }

  async addCartItem(request: AddCartItemRequest): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>("/items/add", request);
  }

  async updateCartItem(
    productId: number,
    request: UpdateCartItemRequest
  ): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>(`/items/${productId}/update`, request);
  }

  async removeCartItem(productId: number): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>(`/items/${productId}/remove`);
  }

  async clearCart(): Promise<ApiResponse<CartResponse>> {
    return this.post<CartResponse>("/clear");
  }
}

export const cartApi = new CartApi();
