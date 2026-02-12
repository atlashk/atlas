export interface Product {
  id: number;
  name: string;
  image: string;
  price: number;
  isActive?: boolean;
  brand?: Brand;
  details?: ProductDetails;
  attributes?: ProductAttribute[];
  categories?: Category[];
}

export interface Brand {
  id: number;
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
  id: number;
  name: string;
}

export interface RetrieveProductListFilter {
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  brandId?: string;
  categoryIds?: number[];
  page: number;
  size: number;
  mode?: "DATABASE" | "FULL_TEXT_SEARCH";
}
