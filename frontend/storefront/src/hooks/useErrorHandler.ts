import { useCallback, useState } from "react";
import { toast } from "sonner";

export interface ErrorState {
  message: string;
  code?: string;
  details?: string;
}

export function useErrorHandler() {
  const [error, setError] = useState<ErrorState | null>(null);

  const handleError = useCallback((error: Error | string, context?: string) => {
    let errorState: ErrorState;

    if (error instanceof Error) {
      errorState = {
        message: error.message,
        details: context,
      };
    } else if (typeof error === "string") {
      errorState = {
        message: error,
        details: context,
      };
    } else {
      errorState = {
        message: "An unexpected error occurred",
        details: context,
      };
    }

    setError(errorState);
    
    // Show toast notification for better user feedback
    toast.error(errorState.message, {
      description: errorState.details,
      duration: 5000,
    });

    // Log error for debugging
    console.error(`Error in ${context || "unknown context"}:`, error);
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const handleApiError = useCallback((error: Error | { response?: { status?: number; data?: { message?: string } }; message?: string } | string) => {
    let errorMessage = 'An unexpected error occurred';
    let statusCode: string | undefined;
    
    if (typeof error === 'object' && error !== null && 'response' in error && error.response?.data?.message) {
      errorMessage = error.response.data.message;
      statusCode = error.response.status?.toString();
    } else if (typeof error === 'object' && error !== null && 'message' in error && error.message) {
      errorMessage = error.message;
    } else if (typeof error === 'string') {
      errorMessage = error;
    }

    // Handle specific HTTP status codes
    if (typeof error === 'object' && error !== null && 'response' in error && error.response?.status) {
      switch (error.response.status) {
        case 400:
          errorMessage = 'Invalid request. Please check your input.';
          break;
        case 401:
          errorMessage = 'Authentication required. Please log in.';
          break;
        case 403:
          errorMessage = 'Access denied. You do not have permission.';
          break;
        case 404:
          errorMessage = 'Resource not found.';
          break;
        case 409:
          errorMessage = 'Conflict. The resource already exists.';
          break;
        case 422:
          errorMessage = 'Validation failed. Please check your input.';
          break;
        case 429:
          errorMessage = 'Too many requests. Please try again later.';
          break;
        case 500:
          errorMessage = 'Server error. Please try again later.';
          break;
        case 502:
        case 503:
        case 504:
          errorMessage = 'Service temporarily unavailable. Please try again later.';
          break;
        default:
          errorMessage = `Request failed with status ${error.response.status}`;
      }
    }

    const errorState: ErrorState = {
      message: errorMessage,
      code: statusCode,
    };

    setError(errorState);
    
    // Show toast notification
    toast.error(errorMessage);
  }, []);

  return {
    error,
    handleError,
    handleApiError,
    clearError,
  };
}