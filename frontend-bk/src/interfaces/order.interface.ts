import type { Product } from "./product.interface";
import type { User } from "./user.interface";

export interface Order {
  id: number;
  code: string;
  user?: User;
  orderItems: OrderItem[];
  amount: number;
  status: OrderStatus;
  cancelReason?: string;
  createdAt: string; // Date usually handled as ISO string
}

export interface OrderItem {
  product: Product;
  quantity: number;
}

export enum OrderStatus {
  PROCESSING = 'PROCESSING',
  CONFIRMED = 'CONFIRMED',
  CANCELED = 'CANCELED',
}

export interface PlaceOrderRequest {
  orderItems: PlaceOrderItemRequest[];
}

export interface PlaceOrderItemRequest {
  productId: number;
  quantity: number;
}
export interface ListOrderFilters {
  orderId?: number;
  userId?: number;
  status: OrderStatus | '';
  startDate?: string;
  endDate?: string;
  page: number
  size: number
}

export interface GetOrderStatusResponse {
  status: OrderStatus;
  canceledReason: string;
}
