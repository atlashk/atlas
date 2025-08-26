"use client";

import { useState, useEffect, useRef, useCallback, useMemo } from 'react';

// Core types
export interface LoadingState {
  loading: boolean;
  error: string | null;
  hasLoaded: boolean;
}

export interface PaginationState {
  page: number;
  size: number;
  totalRecords: number;
  totalPages: number;
}

export interface FilterState {
  [key: string]: unknown;
}

// Unified loader function type
export type DataLoader<T> = (
  page?: number,
  size?: number,
  filters?: FilterState
) => Promise<T | { data: T[]; totalRecords: number }>;

// Callback types
export type SuccessCallback<T> = (data: T) => void;
export type ErrorCallback = (error: string) => void;

// Simplified Configuration interface
export interface DataLoaderConfig<T> {
  // Base configuration
  autoLoad?: boolean;
  dependencies?: unknown[];
  onError?: ErrorCallback;
  onSuccess?: SuccessCallback<T>;
  
  // Unified loader configuration
  loadFunction: DataLoader<T>;
  
  // Pagination configuration
  initialPage?: number;
  initialSize?: number;
  pagination?: boolean; // Explicit flag to indicate paginated mode
}

// Type overloads for conditional return types
export function useDataLoader<T>(config: DataLoaderConfig<T> & { pagination: true }): {
  data: T | T[] | null;
  loading: boolean;
  error: string | null;
  hasLoaded: boolean;
  load: () => Promise<void>;
  execute: () => void;
  reset: () => void;
  pagination: PaginationState;
  goToPage: (page: number) => void;
  changePageSize: (size: number) => void;
  filters: FilterState;
  updateFilters: (newFilters: FilterState | ((prev: FilterState) => FilterState)) => void;
  clearFilters: () => void;
};

export function useDataLoader<T>(config: DataLoaderConfig<T> & { pagination?: false | undefined }): {
  data: T | T[] | null;
  loading: boolean;
  error: string | null;
  hasLoaded: boolean;
  load: () => Promise<void>;
  execute: () => void;
  reset: () => void;
};

// Main hook implementation
export function useDataLoader<T>(config: DataLoaderConfig<T>): any {
  const { 
    autoLoad = false, 
    dependencies = [], 
    onSuccess, 
    onError, 
    loadFunction,
    pagination = false,
    initialPage = 1,
    initialSize = 20
  } = config;

  // Core state
  const [data, setData] = useState<T | T[] | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [hasLoaded, setHasLoaded] = useState<boolean>(false);
  
  // Pagination state (only for paginated config)
  const [paginationState, setPaginationState] = useState<PaginationState>(() => ({
    page: initialPage,
    size: initialSize,
    totalRecords: 0,
    totalPages: 0
  }));
  
  // Filter state (only for paginated config with filtering)
  const [filters, setFilters] = useState<FilterState>({});

  // Refs for preventing race conditions and memory leaks
  const loadingRef = useRef(false);
  const mountedRef = useRef(true);
  const abortControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      abortControllerRef.current?.abort();
    };
  }, []);

  // Helper function to handle errors consistently
  const handleError = useCallback((err: unknown): string => {
    return err instanceof Error ? err.message : 'An error occurred';
  }, []);

  // Function refs to prevent dependency issues
  const loadFunctionRef = useRef(loadFunction);
  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  // Update refs when values change
  useEffect(() => {
    loadFunctionRef.current = loadFunction;
    onSuccessRef.current = onSuccess;
    onErrorRef.current = onError;
  });

  // Core load function with improved error handling and abort support
  const load = useCallback(async () => {
    if (loadingRef.current) return;
    
    // Cancel any ongoing request
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();
    
    loadingRef.current = true;
    setLoading(true);
    setError(null);

    try {
      let result;
      
      if (pagination) {
         // Call with pagination parameters
         const hasFilters = Object.keys(filters).length > 0;
         const filterData = hasFilters ? filters : undefined;
         result = await loadFunctionRef.current(
           paginationState.page,
           paginationState.size,
           filterData
         );
        
        // Handle paginated result
        if (result && typeof result === 'object' && 'data' in result && 'totalRecords' in result) {
          const paginatedResult = result as { data: T[]; totalRecords: number };
          
          if (mountedRef.current && !abortControllerRef.current?.signal.aborted) {
            const totalPages = Math.ceil(paginatedResult.totalRecords / paginationState.size);
            setPaginationState(prev => ({
              ...prev,
              totalRecords: paginatedResult.totalRecords,
              totalPages
            }));
            
            setData(paginatedResult.data as T);
            setHasLoaded(true);
            onSuccessRef.current?.(paginatedResult.data as T);
          }
        }
      } else {
        // Call without pagination parameters
        result = await loadFunctionRef.current();
        
        if (mountedRef.current && !abortControllerRef.current?.signal.aborted) {
          setData(result as T);
          setHasLoaded(true);
          onSuccessRef.current?.(result as T);
        }
      }
    } catch (err) {
      if (mountedRef.current && !abortControllerRef.current?.signal.aborted) {
        const errorMessage = handleError(err);
        setError(errorMessage);
        onErrorRef.current?.(errorMessage);
      }
    } finally {
      if (mountedRef.current) {
        setLoading(false);
        loadingRef.current = false;
      }
    }
  }, [pagination, paginationState.page, paginationState.size, filters]);

  // Pagination controls (only for paginated config)
  const goToPage = useCallback((page: number) => {
    if (!pagination) return;
    setPaginationState(prev => ({ ...prev, page }));
  }, [pagination]);

  const changePageSize = useCallback((size: number) => {
    if (!pagination) return;
    setPaginationState(prev => ({ ...prev, size, page: 1 }));
  }, [pagination]);

  // Filter controls (only for paginated config)
  const updateFilters = useCallback((newFilters: FilterState | ((prev: FilterState) => FilterState)) => {
    if (!pagination) return;
    if (typeof newFilters === 'function') {
      setFilters(newFilters);
    } else {
      setFilters(newFilters);
    }
    setPaginationState(prev => ({ ...prev, page: 1 }));
  }, [pagination]);

  const clearFilters = useCallback(() => {
    if (!pagination) return;
    setFilters({});
    setPaginationState(prev => ({ ...prev, page: 1 }));
  }, [pagination]);

  // Reset and execute
  const reset = useCallback(() => {
    abortControllerRef.current?.abort();
    setData(null);
    setError(null);
    setHasLoaded(false);
    
    if (pagination) {
       setPaginationState({
         page: initialPage,
         size: initialSize,
         totalRecords: 0,
         totalPages: 0
       });
       setFilters({});
     }
   }, [pagination, initialPage, initialSize]);

  const execute = useCallback(() => {
    reset();
    load();
  }, [reset, load]);

  // Auto load on mount or dependency change
  const memoizedDependencies = useMemo(() => dependencies, [dependencies]);
  useEffect(() => {
    if (autoLoad) {
      load();
    }
  }, [load, autoLoad, ...memoizedDependencies]);

  // Handle filter and pagination changes for paginated mode
  useEffect(() => {
    if (pagination && hasLoaded) {
      // Call load directly without including it in dependencies to avoid infinite loops
      const triggerLoad = async () => {
        if (loadingRef.current) return;
        
        // Cancel any ongoing request
        abortControllerRef.current?.abort();
        abortControllerRef.current = new AbortController();
        
        loadingRef.current = true;
        setLoading(true);
        setError(null);

        try {
          let result;
          
          if (pagination) {
            // Call with pagination parameters
            const hasFilters = Object.keys(filters).length > 0;
            const filterData = hasFilters ? filters : undefined;
            result = await loadFunctionRef.current(
              paginationState.page,
              paginationState.size,
              filterData
            );
           
            // Handle paginated result
            if (result && typeof result === 'object' && 'data' in result && 'totalRecords' in result) {
              const paginatedResult = result as { data: T[]; totalRecords: number };
              
              if (mountedRef.current && !abortControllerRef.current?.signal.aborted) {
                const totalPages = Math.ceil(paginatedResult.totalRecords / paginationState.size);
                setPaginationState(prev => ({
                  ...prev,
                  totalRecords: paginatedResult.totalRecords,
                  totalPages
                }));
                
                setData(paginatedResult.data as T);
                setHasLoaded(true);
                onSuccessRef.current?.(paginatedResult.data as T);
              }
            }
          }
        } catch (err) {
          if (mountedRef.current && !abortControllerRef.current?.signal.aborted) {
            const errorMessage = handleError(err);
            setError(errorMessage);
            onErrorRef.current?.(errorMessage);
          }
        } finally {
          if (mountedRef.current) {
            setLoading(false);
            loadingRef.current = false;
          }
        }
      };
      
      triggerLoad();
    }
  }, [pagination, hasLoaded, JSON.stringify(filters), paginationState.page, paginationState.size]);

  // Memoize filters to prevent unnecessary re-renders
  const memoizedFilters = useMemo(() => filters, [JSON.stringify(filters)]);

  // Return object with conditional properties based on config type
  const baseReturn = {
    data,
    loading,
    error,
    hasLoaded,
    load,
    execute,
    reset,
  };

  if (pagination) {
    const paginatedReturn = {
      ...baseReturn,
      pagination: paginationState,
      goToPage,
      changePageSize,
    };

    return {
       ...paginatedReturn,
       filters: memoizedFilters,
       updateFilters,
       clearFilters,
     };
  }

  return baseReturn;
}

// Export main hook as default
export default useDataLoader;
