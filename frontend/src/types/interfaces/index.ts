// User related interfaces
export * from './user.interface';

// Product related interfaces
export * from './product.interface';

// Order related interfaces
export * from './order.interface';

// Re-export commonly used types for convenience
export type {
  User,
  UserProfile,
  LoginCredentials,
  RegisterData,
  Address,
  AuthState,
} from './user.interface';

export type {
  Product,
  ProductListItem,
  ProductCategory,
  ProductSearchFilters,
  ProductSearchResult,
  CreateProductData,
  UpdateProductData,
} from './product.interface';

export type {
  Order,
  OrderItem,
  OrderStatus,
  PaymentStatus,
  ShippingStatus,
  CartItem,
  Cart,
  CreateOrderData,
  OrderSummary,
  CheckoutData,
} from './order.interface';