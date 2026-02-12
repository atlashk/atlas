import { ProductStockStatus } from "@/constants";

export interface Product {
  id: string;
  name: string;
  image: string;
  price: number;
  stockStatus?: ProductStockStatus;
  quantity?: number;
  availableFrom?: string;
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
  id?: string;
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  stockStatus?: ProductStockStatus;
  availableFrom?: string;
  isActive?: boolean;
  brandId?: string;
  categoryIds?: number[];
  page: number;
  size: number;
}

export interface CreateProductRequest {
  name: string;
  price: number;
  stockStatus: ProductStockStatus;
  quantity: number;
  availableFrom: string;
  isActive: boolean;
  brandId: number;
  details: ProductDetails;
  attributes: Omit<ProductAttribute, "id">[];
  categoryIds: number[];
}

export interface UpdateProductRequest {
  id: string;
  name: string;
  price: number;
  stockStatus: ProductStockStatus;
  quantity: number;
  availableFrom: string;
  isActive: boolean;
  brandId: number;
  details: ProductDetails;
  attributes: ProductAttribute[];
  categoryIds: number[];
}

export enum FileType {
  CSV = "csv",
  EXCEL = "excel",
  PDF = "pdf",
}

export interface ImportProductRequest {
  file: File;
  fileType: FileType;
}

export interface ExportProductFilters {
  id?: string;
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  stockStatus?: string;
  availableFrom?: string;
  isActive?: boolean;
  brandId?: string;
  categoryIds?: number[];
  fileType: FileType;
}
