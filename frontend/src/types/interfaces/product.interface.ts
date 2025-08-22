export interface Product {
  id: string;
  name: string;
  description: string;
  shortDescription?: string;
  sku: string;
  price: number;
  originalPrice?: number;
  discountPercentage?: number;
  currency: string;
  stock: number;
  minOrderQuantity: number;
  maxOrderQuantity?: number;
  weight?: number;
  dimensions?: ProductDimensions;
  images: ProductImage[];
  thumbnail?: string;
  category: ProductCategory;
  subcategory?: ProductSubcategory;
  brand?: ProductBrand;
  tags: string[];
  attributes: ProductAttribute[];
  variants?: ProductVariant[];
  reviews: ProductReview[];
  rating: ProductRating;
  isActive: boolean;
  isAvailable: boolean;
  isFeatured: boolean;
  isDigital: boolean;
  requiresShipping: boolean;
  shippingClass?: string;
  taxClass?: string;
  seoData?: ProductSEO;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
}

export interface ProductDimensions {
  length: number;
  width: number;
  height: number;
  unit: 'cm' | 'in' | 'm';
}

export interface ProductImage {
  id: string;
  url: string;
  alt: string;
  title?: string;
  isPrimary: boolean;
  sortOrder: number;
  size?: {
    width: number;
    height: number;
  };
}

export interface ProductCategory {
  id: string;
  name: string;
  slug: string;
  description?: string;
  image?: string;
  parentId?: string;
  level: number;
  sortOrder: number;
  isActive: boolean;
}

export interface ProductSubcategory extends ProductCategory {
  parentCategory: ProductCategory;
}

export interface ProductBrand {
  id: string;
  name: string;
  slug: string;
  description?: string;
  logo?: string;
  website?: string;
  isActive: boolean;
}

export interface ProductAttribute {
  id: string;
  name: string;
  value: string | number | boolean;
  type: 'text' | 'number' | 'boolean' | 'select' | 'multiselect';
  unit?: string;
  isFilterable: boolean;
  isRequired: boolean;
  sortOrder: number;
}

export interface ProductVariant {
  id: string;
  name: string;
  sku: string;
  price: number;
  stock: number;
  attributes: Record<string, string>;
  image?: string;
  isDefault: boolean;
  isActive: boolean;
}

export interface ProductReview {
  id: string;
  userId: string;
  userName: string;
  userAvatar?: string;
  rating: number;
  title: string;
  comment: string;
  pros?: string[];
  cons?: string[];
  isVerifiedPurchase: boolean;
  isRecommended: boolean;
  helpfulCount: number;
  images?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductRating {
  average: number;
  count: number;
  distribution: {
    1: number;
    2: number;
    3: number;
    4: number;
    5: number;
  };
}

export interface ProductSEO {
  metaTitle?: string;
  metaDescription?: string;
  metaKeywords?: string[];
  canonicalUrl?: string;
  ogTitle?: string;
  ogDescription?: string;
  ogImage?: string;
}

export interface ProductListItem {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  discountPercentage?: number;
  thumbnail: string;
  rating: {
    average: number;
    count: number;
  };
  isAvailable: boolean;
  isFeatured: boolean;
  category: {
    id: string;
    name: string;
  };
  brand?: {
    id: string;
    name: string;
  };
}

export interface ProductSearchFilters {
  query?: string;
  categoryId?: string;
  brandId?: string;
  minPrice?: number;
  maxPrice?: number;
  rating?: number;
  inStock?: boolean;
  isFeatured?: boolean;
  attributes?: Record<string, string | string[]>;
  sortBy?: ProductSortOption;
  sortOrder?: 'asc' | 'desc';
}

export type ProductSortOption =
  | 'name'
  | 'price'
  | 'rating'
  | 'popularity'
  | 'newest'
  | 'oldest'
  | 'discount';

export interface ProductSearchResult {
  products: ProductListItem[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
  filters: {
    categories: Array<{ id: string; name: string; count: number }>;
    brands: Array<{ id: string; name: string; count: number }>;
    priceRange: { min: number; max: number };
    attributes: Record<string, Array<{ value: string; count: number }>>;
  };
}

export interface CreateProductData {
  name: string;
  description: string;
  shortDescription?: string;
  price: number;
  originalPrice?: number;
  stock: number;
  categoryId: string;
  brandId?: string;
  images: File[];
  attributes: Omit<ProductAttribute, 'id'>[];
  tags: string[];
  isActive: boolean;
  isFeatured: boolean;
  seoData?: Omit<ProductSEO, 'canonicalUrl'>;
}

export interface UpdateProductData extends Partial<CreateProductData> {
  id: string;
}

// Type guards
export function isProduct(obj: any): obj is Product {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.name === 'string' &&
    typeof obj.price === 'number' &&
    typeof obj.stock === 'number' &&
    Array.isArray(obj.images) &&
    obj.category &&
    typeof obj.category === 'object'
  );
}

export function isProductListItem(obj: any): obj is ProductListItem {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.name === 'string' &&
    typeof obj.price === 'number' &&
    typeof obj.thumbnail === 'string'
  );
}

export function isProductCategory(obj: any): obj is ProductCategory {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.name === 'string' &&
    typeof obj.slug === 'string'
  );
}