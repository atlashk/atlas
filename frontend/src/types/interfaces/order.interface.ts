import type { Address, User } from './user.interface';
import type { ProductListItem } from './product.interface';

export interface Order {
  id: string;
  orderNumber: string;
  userId: string;
  user?: Pick<User, 'id' | 'firstName' | 'lastName' | 'email'>;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  shippingStatus: ShippingStatus;
  items: OrderItem[];
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  currency: string;
  shippingAddress: Address;
  billingAddress: Address;
  paymentMethod: PaymentMethod;
  shippingMethod: ShippingMethod;
  notes?: string;
  trackingNumber?: string;
  estimatedDelivery?: string;
  actualDelivery?: string;
  refunds?: OrderRefund[];
  timeline: OrderTimeline[];
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export type OrderStatus =
  | 'pending'
  | 'confirmed'
  | 'processing'
  | 'shipped'
  | 'delivered'
  | 'cancelled'
  | 'refunded'
  | 'returned';

export type PaymentStatus =
  | 'pending'
  | 'processing'
  | 'paid'
  | 'failed'
  | 'cancelled'
  | 'refunded'
  | 'partially_refunded';

export type ShippingStatus =
  | 'pending'
  | 'processing'
  | 'shipped'
  | 'in_transit'
  | 'out_for_delivery'
  | 'delivered'
  | 'failed'
  | 'returned';

export interface OrderItem {
  id: string;
  productId: string;
  product: ProductListItem;
  variantId?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  discount?: number;
  tax?: number;
  notes?: string;
}

export interface PaymentMethod {
  id: string;
  type: 'credit_card' | 'debit_card' | 'paypal' | 'bank_transfer' | 'cash_on_delivery' | 'digital_wallet';
  provider: string;
  last4?: string;
  expiryMonth?: number;
  expiryYear?: number;
  cardholderName?: string;
  isDefault: boolean;
}

export interface ShippingMethod {
  id: string;
  name: string;
  description?: string;
  price: number;
  estimatedDays: number;
  carrier: string;
  trackingSupported: boolean;
}

export interface OrderRefund {
  id: string;
  amount: number;
  reason: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  items?: Array<{
    orderItemId: string;
    quantity: number;
    amount: number;
  }>;
  createdAt: string;
  processedAt?: string;
}

export interface OrderTimeline {
  id: string;
  status: OrderStatus | PaymentStatus | ShippingStatus;
  title: string;
  description?: string;
  timestamp: string;
  metadata?: Record<string, any>;
}

export interface CreateOrderData {
  items: Array<{
    productId: string;
    variantId?: string;
    quantity: number;
  }>;
  shippingAddressId: string;
  billingAddressId: string;
  paymentMethodId: string;
  shippingMethodId: string;
  notes?: string;
  couponCode?: string;
}

export interface OrderSummary {
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  currency: string;
  itemCount: number;
}

export interface OrderListItem {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  total: number;
  currency: string;
  itemCount: number;
  createdAt: string;
  estimatedDelivery?: string;
}

export interface OrderSearchFilters {
  status?: OrderStatus[];
  paymentStatus?: PaymentStatus[];
  shippingStatus?: ShippingStatus[];
  dateFrom?: string;
  dateTo?: string;
  minAmount?: number;
  maxAmount?: number;
  search?: string;
  sortBy?: OrderSortOption;
  sortOrder?: 'asc' | 'desc';
}

export type OrderSortOption =
  | 'orderNumber'
  | 'total'
  | 'status'
  | 'createdAt'
  | 'updatedAt';

export interface OrderSearchResult {
  orders: OrderListItem[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
  summary: {
    totalOrders: number;
    totalAmount: number;
    statusCounts: Record<OrderStatus, number>;
  };
}

export interface CartItem {
  id: string;
  product: ProductListItem;
  variantId?: string;
  quantity: number;
  addedAt: string;
  notes?: string;
}

export interface Cart {
  id: string;
  userId?: string;
  items: CartItem[];
  summary: OrderSummary;
  lastUpdated: string;
  expiresAt?: string;
}

export interface CheckoutData {
  cart: Cart;
  shippingAddress: Address;
  billingAddress: Address;
  paymentMethod: PaymentMethod;
  shippingMethod: ShippingMethod;
  couponCode?: string;
  notes?: string;
}

export interface OrderTracking {
  orderId: string;
  trackingNumber: string;
  carrier: string;
  status: ShippingStatus;
  estimatedDelivery: string;
  actualDelivery?: string;
  events: TrackingEvent[];
}

export interface TrackingEvent {
  id: string;
  status: string;
  description: string;
  location?: string;
  timestamp: string;
}

// Type guards
export function isOrder(obj: any): obj is Order {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.orderNumber === 'string' &&
    typeof obj.status === 'string' &&
    Array.isArray(obj.items) &&
    typeof obj.total === 'number'
  );
}

export function isOrderItem(obj: any): obj is OrderItem {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.productId === 'string' &&
    typeof obj.quantity === 'number' &&
    typeof obj.unitPrice === 'number'
  );
}

export function isCartItem(obj: any): obj is CartItem {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    obj.product &&
    typeof obj.quantity === 'number'
  );
}

export function isValidOrderStatus(status: string): status is OrderStatus {
  return [
    'pending',
    'confirmed',
    'processing',
    'shipped',
    'delivered',
    'cancelled',
    'refunded',
    'returned',
  ].includes(status);
}

export function isValidPaymentStatus(status: string): status is PaymentStatus {
  return [
    'pending',
    'processing',
    'paid',
    'failed',
    'cancelled',
    'refunded',
    'partially_refunded',
  ].includes(status);
}

export function isValidShippingStatus(status: string): status is ShippingStatus {
  return [
    'pending',
    'processing',
    'shipped',
    'in_transit',
    'out_for_delivery',
    'delivered',
    'failed',
    'returned',
  ].includes(status);
}