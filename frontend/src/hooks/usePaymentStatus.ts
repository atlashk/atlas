import { useState, useCallback, useEffect, useRef } from 'react';

export type PaymentStatus = 
  | 'idle'
  | 'pending'
  | 'processing'
  | 'requires_action'
  | 'succeeded'
  | 'failed'
  | 'canceled';

export interface PaymentStatusState {
  status: PaymentStatus;
  paymentId: string | null;
  error: string | null;
  isPolling: boolean;
  lastUpdated: Date | null;
  metadata?: Record<string, unknown>;
}

export interface UsePaymentStatusOptions {
  paymentId?: string;
  pollInterval?: number;
  maxPollAttempts?: number;
  onStatusChange?: (status: PaymentStatus, metadata?: Record<string, unknown>) => void;
  onSuccess?: (metadata?: Record<string, unknown>) => void;
  onFailure?: (error: string, metadata?: Record<string, unknown>) => void;
  autoStartPolling?: boolean;
}

export interface UsePaymentStatusReturn {
  state: PaymentStatusState;
  actions: {
    startPolling: (paymentId: string) => void;
    stopPolling: () => void;
    updateStatus: (status: PaymentStatus, metadata?: Record<string, unknown>) => void;
    checkStatus: (paymentId: string) => Promise<void>;
    reset: () => void;
  };
  helpers: {
    isTerminalStatus: (status?: PaymentStatus) => boolean;
    isSuccessStatus: (status?: PaymentStatus) => boolean;
    isFailureStatus: (status?: PaymentStatus) => boolean;
    isPendingStatus: (status?: PaymentStatus) => boolean;
  };
}

export function usePaymentStatus(options: UsePaymentStatusOptions = {}): UsePaymentStatusReturn {
  const {
    paymentId: initialPaymentId,
    pollInterval = 2000,
    maxPollAttempts = 30,
    onStatusChange,
    onSuccess,
    onFailure,
    autoStartPolling = false,
  } = options;

  const [state, setState] = useState<PaymentStatusState>({
    status: 'idle',
    paymentId: initialPaymentId || null,
    error: null,
    isPolling: false,
    lastUpdated: null,
    metadata: undefined,
  });

  const pollIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const pollAttemptsRef = useRef(0);
  const abortControllerRef = useRef<AbortController | null>(null);

  const updateState = useCallback((updates: Partial<PaymentStatusState>) => {
    setState(prev => ({ ...prev, ...updates, lastUpdated: new Date() }));
  }, []);

  const isTerminalStatus = useCallback((status: PaymentStatus = state.status): boolean => {
    return ['succeeded', 'failed', 'canceled'].includes(status);
  }, [state.status]);

  const isSuccessStatus = useCallback((status: PaymentStatus = state.status): boolean => {
    return status === 'succeeded';
  }, [state.status]);

  const isFailureStatus = useCallback((status: PaymentStatus = state.status): boolean => {
    return ['failed', 'canceled'].includes(status);
  }, [state.status]);

  const isPendingStatus = useCallback((status: PaymentStatus = state.status): boolean => {
    return ['pending', 'processing', 'requires_action'].includes(status);
  }, [state.status]);

  const fetchPaymentStatus = useCallback(async (paymentId: string): Promise<PaymentStatus> => {
    // This would typically make an API call to check payment status
    // For now, we'll simulate the API call
    try {
      const response = await fetch(`/api/payments/${paymentId}/status`, {
        signal: abortControllerRef.current?.signal,
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch payment status: ${response.statusText}`);
      }
      
      const data = await response.json();
      return data.status as PaymentStatus;
    } catch (error) {
      if (error instanceof Error && error.name === 'AbortError') {
        throw error;
      }
      console.error('Error fetching payment status:', error);
      throw new Error('Failed to fetch payment status');
    }
  }, []);

  const checkStatus = useCallback(async (paymentId: string) => {
    try {
      updateState({ error: null });
      const status = await fetchPaymentStatus(paymentId);
      
      updateState({ 
        status, 
        paymentId,
        error: null 
      });
      
      onStatusChange?.(status, state.metadata);
      
      if (isSuccessStatus(status)) {
        onSuccess?.(state.metadata);
      } else if (isFailureStatus(status)) {
        onFailure?.('Payment failed', state.metadata);
      }
      
    } catch (error) {
      if (error instanceof Error && error.name === 'AbortError') {
        return; // Polling was stopped
      }
      
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      updateState({ error: errorMessage });
      onFailure?.(errorMessage, state.metadata);
    }
  }, [fetchPaymentStatus, updateState, onStatusChange, onSuccess, onFailure, state.metadata, isSuccessStatus, isFailureStatus]);

  const startPolling = useCallback((paymentId: string) => {
    if (pollIntervalRef.current) {
      clearInterval(pollIntervalRef.current);
    }
    
    abortControllerRef.current = new AbortController();
    pollAttemptsRef.current = 0;
    
    updateState({ 
      isPolling: true, 
      paymentId,
      error: null 
    });

    const poll = async () => {
      if (pollAttemptsRef.current >= maxPollAttempts) {
        stopPolling();
        updateState({ error: 'Polling timeout reached' });
        onFailure?.('Polling timeout reached', state.metadata);
        return;
      }

      pollAttemptsRef.current++;
      
      try {
        await checkStatus(paymentId);
        
        // Stop polling if we reached a terminal status
        if (isTerminalStatus(state.status)) {
          stopPolling();
        }
      } catch (error) {
        // Continue polling unless it's an abort error
        if (error instanceof Error && error.name === 'AbortError') {
          return;
        }
      }
    };

    // Initial check
    poll();
    
    // Set up interval for subsequent checks
    pollIntervalRef.current = setInterval(poll, pollInterval);
  }, [pollInterval, maxPollAttempts, updateState, checkStatus, isTerminalStatus, state.status, state.metadata, onFailure]);

  const stopPolling = useCallback(() => {
    if (pollIntervalRef.current) {
      clearInterval(pollIntervalRef.current);
      pollIntervalRef.current = null;
    }
    
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
    
    updateState({ isPolling: false });
    pollAttemptsRef.current = 0;
  }, [updateState]);

  const updateStatus = useCallback((status: PaymentStatus, metadata?: Record<string, unknown>) => {
    updateState({ status, metadata });
    onStatusChange?.(status, metadata);
    
    if (isSuccessStatus(status)) {
      onSuccess?.(metadata);
      stopPolling();
    } else if (isFailureStatus(status)) {
      onFailure?.('Payment failed', metadata);
      stopPolling();
    }
  }, [updateState, onStatusChange, onSuccess, onFailure, isSuccessStatus, isFailureStatus, stopPolling]);

  const reset = useCallback(() => {
    stopPolling();
    setState({
      status: 'idle',
      paymentId: null,
      error: null,
      isPolling: false,
      lastUpdated: null,
      metadata: undefined,
    });
  }, [stopPolling]);

  // Auto-start polling if enabled and paymentId is provided
  useEffect(() => {
    if (autoStartPolling && initialPaymentId && !state.isPolling) {
      startPolling(initialPaymentId);
    }
    
    return () => {
      stopPolling();
    };
  }, [autoStartPolling, initialPaymentId, state.isPolling, startPolling, stopPolling]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      stopPolling();
    };
  }, [stopPolling]);

  return {
    state,
    actions: {
      startPolling,
      stopPolling,
      updateStatus,
      checkStatus,
      reset,
    },
    helpers: {
      isTerminalStatus,
      isSuccessStatus,
      isFailureStatus,
      isPendingStatus,
    },
  };
}