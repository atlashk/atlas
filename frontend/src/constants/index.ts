export const ROLES = [
  "ADMIN",
  "USER"
] as const;

export type Role = typeof ROLES[number];

export const PRODUCT_STATUSES = [
  "IN_STOCK",
  "OUT_STOCK",
  "DISCONTINUED"
] as const;

export type ProductStatus = typeof PRODUCT_STATUSES[number];

export const ORDER_STATUSES = [
  "PROCESSING",
  "CONFIRMED",
  "CANCELED"
] as const;

export type OrderStatus = typeof ORDER_STATUSES[number];
