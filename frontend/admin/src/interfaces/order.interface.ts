import type { Product } from "./catalog.interface";
import type { User } from "./identity.interface";

export interface Order {
  id: string;
  user?: User;
  address?: Address;
  orderItems: OrderItem[];
  amount: number;
  payment?: Payment;
  status: string;
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
  status?: string;
  errorCode?: string;
  errorMessage?: string;
  cancellationReason?: string;
  // Alternative property names from API response
  paymentGatewayName?: string;
  paymentMethod?: string;
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
  status: string;
  cancellationReason?: string;
}

export interface RetrieveOrderFilter {
  id?: string;
  userId?: string;
  productId?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  size: number;
}
