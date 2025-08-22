// Common types
export * from './common';

// Interface types
export * from './interfaces';

// Type guards and utilities
export const isString = (value: unknown): value is string => typeof value === 'string';
export const isNumber = (value: unknown): value is number => typeof value === 'number';
export const isBoolean = (value: unknown): value is boolean => typeof value === 'boolean';
export const isObject = (value: unknown): value is Record<string, unknown> => 
  typeof value === 'object' && value !== null && !Array.isArray(value);
export const isArray = (value: unknown): value is unknown[] => Array.isArray(value);

// Utility types for async operations
export type AsyncState<T> = {
  data: T | null;
  isLoading: boolean;
  error: string | null;
  lastUpdated: Date | null;
};

export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

export type FormState<T> = {
  values: T;
  errors: Partial<Record<keyof T, string>>;
  touched: Partial<Record<keyof T, boolean>>;
  isSubmitting: boolean;
  isValid: boolean;
};

// Enhanced type utilities
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

export type DeepRequired<T> = {
  [P in keyof T]-?: T[P] extends object ? DeepRequired<T[P]> : T[P];
};

export type Nullable<T> = T | null;
export type Optional<T> = T | undefined;
export type Maybe<T> = T | null | undefined;

// API response wrapper types
export type ApiResponse<T> = {
  data: T;
  message?: string;
  success: boolean;
  timestamp: string;
};

export type ApiError = {
  message: string;
  code?: string | number;
  details?: Record<string, any>;
  timestamp: string;
};

export type PaginatedResponse<T> = {
  data: T[];
  meta: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrev: boolean;
  };
};

// Event handler types
export type EventHandler<T = Event> = (event: T) => void;
export type AsyncEventHandler<T = Event> = (event: T) => Promise<void>;

// Component prop types
export type ComponentSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';
export type ComponentVariant = 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';
export type ComponentState = 'default' | 'loading' | 'disabled' | 'error';