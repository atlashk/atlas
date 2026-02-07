import {
  FileType,
  type CreateProductRequest,
  type ExportProductFilters,
  type ListProductFilters,
  type Product,
  type UpdateProductRequest,
} from "@/interfaces/product.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class ProductAdminApi extends BaseApi {
  constructor() {
    super("/services/product/api/admin");
  }

  async listProduct(
    filters: ListProductFilters
  ): Promise<ApiResponse<Product[]>> {
    return this.post<Product[], ListProductFilters>("/products", filters);
  }

  async getProduct(productId: number): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/products/${productId}`);
  }

  async createProduct(
    data: CreateProductRequest,
    imageFile: File
  ): Promise<ApiResponse<number>> {
    const formData = new FormData();
    formData.append(
      "request",
      new Blob([JSON.stringify(data)], { type: "application/json" })
    );
    formData.append("image", imageFile);
    return this.post<number>("/products", formData);
  }

  async updateProduct(
    data: UpdateProductRequest,
    imageFile?: File
  ): Promise<ApiResponse<Product>> {
    const formData = new FormData();
    formData.append(
      "request",
      new Blob([JSON.stringify(data)], { type: "application/json" })
    );
    if (imageFile) {
      formData.append("image", imageFile);
    }
    return this.put<Product>(`/products/${data.id}`, formData);
  }

  async deleteProduct(productId: number): Promise<ApiResponse<void>> {
    return this.delete<void>(`/products/${productId}`);
  }

  async importProduct(
    file: File,
    fileType: FileType
  ): Promise<ApiResponse<void>> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("file_type", fileType);

    return this.post(`/products/import`, formData);
  }

  async exportProduct(filters: ExportProductFilters): Promise<void> {
    const response = await this.postBlob("/products/export", filters);

    // Create blob and download
    const blob = new Blob([response.data], {
      type: response.headers["content-type"] || "application/octet-stream",
    });

    // Always prioritize filename from Content-Disposition header
    // Note: Axios normalizes header names to lowercase
    const contentDisposition = response.headers["content-disposition"];

    // Map frontend FileType to actual file extensions for fallback
    const extensionMap: Record<FileType, string> = {
      [FileType.CSV]: "csv",
      [FileType.EXCEL]: "xlsx",
      [FileType.PDF]: "pdf",
    };

    let filename: string;

    if (contentDisposition) {
      // Handle both filename= and filename*= formats (RFC 6266)
      // First try filename*= (encoded format)
      let filenameMatch = contentDisposition.match(/filename\*=([^;\n]+)/i);
      if (filenameMatch && filenameMatch[1]) {
        try {
          // Handle UTF-8''filename format
          const encodedFilename = filenameMatch[1].replace(/^UTF-8''/, "");
          filename = decodeURIComponent(encodedFilename);
        } catch {
          filename = filenameMatch[1];
        }
      } else {
        // Try regular filename= format
        filenameMatch = contentDisposition.match(/filename=([^;\n]+)/i);
        if (filenameMatch && filenameMatch[1]) {
          // Remove quotes if present
          filename = filenameMatch[1].replace(/^["']|["']$/g, "");
        } else {
          // Fallback to default filename if Content-Disposition exists but no filename found
          filename = `export-product-${new Date()
            .toISOString()
            .slice(0, 19)
            .replace(/[:-]/g, "")}.${extensionMap[filters.fileType]}`;
        }
      }
    } else {
      // Only use default filename when Content-Disposition header is completely missing
      filename = `export-product-${new Date()
        .toISOString()
        .slice(0, 19)
        .replace(/[:-]/g, "")}.${extensionMap[filters.fileType]}`;
    }

    // Create download link
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();

    // Cleanup
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  async countProduct(): Promise<ApiResponse<number>> {
    return this.get<number>("/products/statistics/count");
  }
}

export const productAdminApi = new ProductAdminApi();
