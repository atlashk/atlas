import { OrderStatus, PaymentStatus } from "@/constants";
import type { Product } from "./product.interface";
import type { User } from "./iam.interface";

export interface Order {
  id: string;
  code: string;
  user?: User;
  address?: Address;
  orderItems: OrderItem[];
  amount: number;
  payment?: Payment;
  status: OrderStatus;
  cancellationReason?: string;
  createdAt: string; // Date usually handled as ISO string
}

export interface Payment {
  id?: string;
  transactionId?: string;
  amount?: number;
  currency?: string;
  method?: string;
  gateway?: string;
  status?: PaymentStatus;
  errorCode?: string;
  errorMessage?: string;
  cancellationReason?: string;
  // Alternative property names from API response
  paymentMethod?: string;
  paymentGateway?: string;
  paymentMethodDetails?: string;
}

export interface OrderItem {
  product: Product;
  quantity: number;
}

export interface Address {
  street: string;
  city: string;
  country: string;
  postalCode: string;
}

export interface CheckoutRequest {
  address: {
    street: string;
    city: string;
    country: string;
    postalCode: string;
  };
  paymentGatewayId: number;
}

export interface CheckoutResponse {
  orderId: string;
}

export interface OrderStatusResponse {
  status: OrderStatus;
  cancellationReason?: string;
}

export interface ListOrderFilters {
  id?: string;
  userId?: string;
  productId?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  size: number;
}
