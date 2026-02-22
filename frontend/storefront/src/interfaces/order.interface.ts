import { OrderStatus } from "@/constants";
import type { PaymentStatus } from "./payment.interface";
import type { Product } from "./product.interface";
import type { User } from "./identity.interface";

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

export interface Order {
  id: string;
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
  orderId: number;
}

export interface OrderStatusResponse {
  status: OrderStatus;
  cancellationReason?: string;
}

export interface RetrieveOrderListFilter {
  orderId?: number;
  userId?: number;
  productId?: number;
  status?: string;
  startDate?: string;
  endDate?: string;
  page: number
  size: number
}
