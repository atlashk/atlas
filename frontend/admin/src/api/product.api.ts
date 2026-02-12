import {
  FileType,
  type Brand,
  type Category,
  type CreateProductRequest,
  type ExportProductFilters,
  type ListProductFilters,
  type Product,
  type UpdateProductRequest
} from "@/interfaces/product.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class ProductFrontApi extends BaseApi {
  constructor() {
    super("/services/product/api/front");
  }

  async retrieveAllBrand(): Promise<ApiResponse<Brand[]>> {
    return this.get<Brand[]>("/brands");
  }

  async retrieveAllCategory(): Promise<ApiResponse<Category[]>> {
    return this.get<Category[]>("/categories");
  }
}

export class ProductAdminApi extends BaseApi {
  constructor() {
    super("/services/product/api/admin");
  }

  async retrieveProductList(
    filters: ListProductFilters
  ): Promise<ApiResponse<Product[]>> {
    return this.post<Product[], ListProductFilters>("/products/list", filters);
  }

  async retrieveProduct(id: string): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/products/${id}`);
  }

  async createProduct(
    data: CreateProductRequest,
    imageFile?: File
  ): Promise<ApiResponse<string>> {
    const formData = new FormData();
    formData.append(
      "request",
      new Blob([JSON.stringify(data)], { type: "application/json" })
    );
    if (imageFile) {
      formData.append("image", imageFile);
    }
    return this.post<string>("/products", formData);
  }

  async updateProduct(
    data: UpdateProductRequest,
    imageFile?: File
  ): Promise<ApiResponse<void>> {
    const formData = new FormData();
    formData.append(
      "request",
      new Blob([JSON.stringify(data)], { type: "application/json" })
    );
    if (imageFile) {
      formData.append("image", imageFile);
    }
    return this.put<void>(`/products/${data.id}`, formData);
  }

  async deleteProduct(id: string): Promise<ApiResponse<void>> {
    return this.delete<void>(`/products/${id}`);
  }

  async importProduct(
    file: File,
    fileType: FileType
  ): Promise<ApiResponse<void>> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("file_type", fileType);

    return this.post<void>(`/products/import`, formData);
  }

  async exportProduct(filters: ExportProductFilters): Promise<void> {
    const response = await this.postBlob("/products/export", filters);

    const blob = new Blob([response.data], {
      type: response.headers["content-type"] || "application/octet-stream",
    });

    const contentDisposition = response.headers["content-disposition"];

    const extensionMap: Record<FileType, string> = {
      [FileType.CSV]: "csv",
      [FileType.EXCEL]: "xlsx",
      [FileType.PDF]: "pdf",
    };

    let filename: string;

    if (contentDisposition) {
      let filenameMatch = contentDisposition.match(/filename\*=([^;\n]+)/i);
      if (filenameMatch && filenameMatch[1]) {
        try {
          const encodedFilename = filenameMatch[1].replace(/^UTF-8''/, "");
          filename = decodeURIComponent(encodedFilename);
        } catch {
          filename = filenameMatch[1];
        }
      } else {
        filenameMatch = contentDisposition.match(/filename=([^;\n]+)/i);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1].replace(/^["']|["']$/g, "");
        } else {
          filename = `export-product-${new Date()
            .toISOString()
            .slice(0, 19)
            .replace(/[:-]/g, "")}.${extensionMap[filters.fileType]}`;
        }
      }
    } else {
      filename = `export-product-${new Date()
        .toISOString()
        .slice(0, 19)
        .replace(/[:-]/g, "")}.${extensionMap[filters.fileType]}`;
    }

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  async countProduct(): Promise<ApiResponse<number>> {
    return this.get<number>("/products/statistics/count");
  }
}

export const productFrontApi = new ProductFrontApi();
export const productAdminApi = new ProductAdminApi();
