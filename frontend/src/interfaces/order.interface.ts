import type { Product } from "./catalog.interface";

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
  productId: string;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export interface Order {
  id: string;
  user?: {
    id?: string | number;
    firstName?: string;
    lastName?: string;
    email?: string;
    phoneNumber?: string;
    role?: string;
  };
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

export interface RetrieveOrderListFilter {
  id?: string;
  orderId?: string;
  userId?: string;
  productId?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  size: number;
}
