export interface Product {
  id: string;
  name: string;
  type: string;
  image: string;
  price: number;
  publishedAt: string;
  inStock: boolean;
  brand: Brand;
  details: ProductDetails;
  attributes: ProductAttribute[];
  categories: Category[];
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

export interface RetrieveProductListFilters {
  id?: string;
  keyword?: string;
  type?: string;
  minPrice?: number;
  maxPrice?: number;
  startPublishedAt?: string;
  endPublishedAt?: string;
  inStock?: boolean;
  brandId?: string;
  categoryIds?: number[];
  page: number;
  size: number;
}

export interface CreateProductRequest {
  name: string;
  type: string;
  price: number;
  publishedAt: string;
  initialQuantity: number;
  brandId: number;
  details: ProductDetails;
  attributes: Omit<ProductAttribute, "id">[];
  categoryIds: number[];
}

export interface UpdateProductRequest {
  id: string;
  name: string;
  type: string;
  price: number;
  publishedAt: string;
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
  type?: string;
  minPrice?: number;
  maxPrice?: number;
  startPublishedAt?: string;
  endPublishedAt?: string;
  inStock?: boolean;
  brandId?: string;
  categoryIds?: number[];
  fileType: FileType;
}
