import type { Product } from "./product.interface";

export interface CartResponse {
  id: number;
  cartItems: CartItemResponse[];
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CartItemResponse {
  product: Product;
  quantity: number;
}

export interface AddCartItemRequest {
  productId: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}
