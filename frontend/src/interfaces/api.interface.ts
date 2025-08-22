export interface ApiResponse<T> {
  success: boolean;
  data: T;
  metadata?: Metadata;
  errorCode?: string;
  errorMessage?: string;
}

export interface Metadata {
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalRecords: number;
}
