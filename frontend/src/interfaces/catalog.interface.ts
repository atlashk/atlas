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
  id: string | number;
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
  id: string | number;
  name: string;
}

export interface RetrieveProductListFilter {
  id?: string;
  keyword?: string;
  type?: string;
  minPrice?: number;
  maxPrice?: number;
  startPublishedDate?: string;
  endPublishedDate?: string;
  inStock?: boolean;
  brandId?: string;
  categoryIds?: Array<string | number>;
  mode?: string;
  page: number;
  size: number;
}

export interface CreateProductRequest {
  name: string;
  type: string;
  price: number;
  publishedAt: string;
  initialQuantity: number;
  brandId: string;
  details: ProductDetails;
  attributes: Omit<ProductAttribute, "id">[];
  categoryIds: string[];
}

export interface UpdateProductRequest {
  id: string;
  name: string;
  type: string;
  price: number;
  publishedAt: string;
  brandId: string;
  details: ProductDetails;
  attributes: ProductAttribute[];
  categoryIds: string[];
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

export interface ExportProductFilter {
  id?: string;
  keyword?: string;
  type?: string;
  minPrice?: number;
  maxPrice?: number;
  startPublishedDate?: string;
  endPublishedDate?: string;
  inStock?: boolean;
  brandId?: string;
  categoryIds?: string[];
  fileType: FileType;
}
