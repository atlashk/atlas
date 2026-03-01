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
  address: Address;
  orderItems: OrderItem[];
  amount: number;
  payment?: Payment;
  status: string;
  cancellationReason?: string;
  createdAt: string; // Date usually handled as ISO string
}

export interface Payment {
  paymentGatewayName: string;
  paymentMethod: string;
  paymentMethodDetails: string;
  transactionId: string;
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
  orderId?: string;
  userId?: string;
  productId?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page: number;
  size: number;
}
