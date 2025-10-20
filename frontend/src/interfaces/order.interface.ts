import { OrderStatus, PaymentMethod } from "@/constants";
import type { PaymentStatus } from "./payment.interface";
import type { Product } from "./product.interface";
import type { User } from "./user.interface";

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

export interface OrderItem {
  product: Product;
  quantity: number;
}

export interface CheckoutRequest {
  paymentMethod: PaymentMethod;
}

export interface CheckoutResponse {
  orderId: number;
}

export interface OrderStatusResponse {
  status: OrderStatus;
  cancellationReason?: string;
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
