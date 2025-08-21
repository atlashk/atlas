export interface PaginationMetadata {
  currentPage: number
  pageSize: number
  totalPages: number
  totalRecords: number
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  metadata?: PaginationMetadata
  errorCode?: string
  errorMessage?: string
}
