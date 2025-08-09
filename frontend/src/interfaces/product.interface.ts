export interface Product {
  id: number;
  name: string;
  image: string;
  price: number;
  quantity: number;
  status: ProductStatus;
  availableFrom: string;
  isActive: boolean;
  brand: Brand;
  details: ProductDetails;
  attributes: ProductAttribute[];
  categories: Category[];
}

export enum ProductStatus {
  IN_STOCK = 'IN_STOCK',
  OUT_STOCK = 'OUT_STOCK',
  DISCONTINUED = 'DISCONTINUED'
}

export interface Brand {
  id: number
  name: string
}

export interface ProductDetails {
  description: string;
};

export interface ProductAttribute {
  id: number;
  name: string;
  value: string;
}

export interface Category {
  id: number;
  name: string;
}

export interface SearchProductFilters {
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  brandId?: string;
  categoryIds?: number[];
  page: number;
  size: number;
}

export interface ListProductFilters {
  id?: number
  keyword?: string
  minPrice?: number | null
  maxPrice?: number | null
  status?: ProductStatus | ''
  availableFrom?: string
  isActive?: boolean | null
  brandId?: string | null
  categoryIds?: number[]
  page: number
  size: number
}

export interface CreateProductRequest {
  name: string;
  price: number;
  image: string;
  quantity: number;
  status: ProductStatus;
  availableFrom: string;
  isActive: boolean;
  brandId: number;
  details: ProductDetails;
  attributes: Omit<ProductAttribute, 'id'>[];
  categoryIds: number[];
}

export interface UpdateProductRequest {
  id: number;
  name: string;
  price: number;
  image: string;
  quantity: number;
  status: ProductStatus;
  availableFrom: string;
  isActive: boolean;
  brandId: number;
  details: ProductDetails;
  attributes: ProductAttribute[];
  categoryIds: number[];
}

export enum FileType {
  CSV = 'csv',
  EXCEL = 'excel'
}

export interface ImportProductRequest {
  file: File
  fileType: FileType
}

export interface ExportProductFilters {
  id?: number
  keyword?: string
  minPrice?: number | null
  maxPrice?: number | null
  status?: ProductStatus | ''
  availableFrom?: string
  isActive?: boolean | null
  brandId?: string | null
  categoryIds?: number[]
  fileType: FileType
}
