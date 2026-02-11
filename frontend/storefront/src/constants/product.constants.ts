export const PRODUCT_STATUSES = [
  "IN_STOCK",
  "OUT_STOCK",
  "DISCONTINUED"
] as const;

export type ProductStatus = typeof PRODUCT_STATUSES[number];
