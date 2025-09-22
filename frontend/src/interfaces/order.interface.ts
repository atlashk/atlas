import {OrderStatus} from "@/constants";
import type {Product} from "./product.interface";
import type {User} from "./user.interface";

export interface Order {
  id: number;
  code: string;
  user?: User;
  orderItems: OrderItem[];
  amount: number;
  payment?: Payment;
  status: OrderStatus;
  cancellationReason?: string;
  createdAt: string; // Date usually handled as ISO string
}

export interface Payment {
  id: number;
  transactionId: string;
  amount: number;
  currency: string;
  method: string;
  gateway: string;
  status: PaymentStatus;
  errorCode?: string;
  errorMessage?: string;
  cancellationReason?: string;
}

export enum PaymentStatus {
  CREATED = "CREATED",
  SUCCEEDED = "SUCCEEDED",
  CANCELED = "CANCELED",
  FAILED = "FAILED",
  UNKNOWN = "UNKNOWN"
}

export enum PaymentMethod {
  CARD = "CARD"
}

export interface OrderItem {
  product: Product;
  quantity: number;
}

export interface PlaceOrderRequest {
  orderItems: PlaceOrderItemRequest[];
  paymentMethod: PaymentMethod;
}

export interface PlaceOrderResponse {
  orderId: number;
  orderCode: string;
}

export interface PlaceOrderItemRequest {
  productId: number;
  quantity: number;
}

export interface ListOrderFilters {
  orderId?: number;
  userId?: number;
  productId?: number;
  status?: string;
  startDate?: string;
  endDate?: string;
  page: number
  size: number
}

export interface GetOrderStatusResponse {
  status: string;
  cancellationReason: string;
}
