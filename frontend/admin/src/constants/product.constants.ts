export const PRODUCT_STOCK_STATUSES = [
  "IN_STOCK",
  "OUT_STOCK",
  "DISCONTINUED"
] as const;

export type ProductStockStatus = typeof PRODUCT_STOCK_STATUSES[number];
