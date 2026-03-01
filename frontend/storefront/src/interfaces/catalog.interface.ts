export interface Product {
  id: string;
  name: string;
  type: string;
  image: string;
  price: number;
  publishedAt: string;
  inStock: boolean;
  brand?: Brand;
  details?: ProductDetails;
  attributes?: ProductAttribute[];
  categories?: Category[];
}

export interface Brand {
  id: string;
  name: string;
}

export interface ProductDetails {
  description: string;
}

export interface ProductAttribute {
  id?: number;
  name: string;
  value: string;
}

export interface Category {
  id: string;
  name: string;
}

export interface RetrieveProductListFilter {
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  brandId?: string;
  categoryIds?: string[];
  mode: string;
  page: number;
  size: number;
}
