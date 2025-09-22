import { useState, useEffect, useCallback } from 'react';
import { 
  LoadingStateManager, 
  LoadingOperation, 
  globalLoadingManager,
  type UseLoadingStateReturn 
} from '@/utils/payment-loading.util';

export interface UseLoadingStateOptions {
  manager?: LoadingStateManager;
  autoCleanup?: boolean;
  cleanupDelay?: number;
}

export function useLoadingState(options: UseLoadingStateOptions = {}): UseLoadingStateReturn {
  const { 
    manager = globalLoadingManager, 
    autoCleanup = true, 
    cleanupDelay = 5000 
  } = options;
  
  const [operations, setOperations] = useState<LoadingOperation[]>([]);

  // Subscribe to loading state changes
  useEffect(() => {
    const unsubscribe = manager.subscribe((newOperations) => {
      setOperations(newOperations);
    });

    // Initialize with current operations
    setOperations(manager.getAllOperations());

    return unsubscribe;
  }, [manager]);

  // Auto cleanup completed/failed operations
  useEffect(() => {
    if (!autoCleanup) return;

    const cleanup = () => {
      const now = Date.now();
      operations.forEach(operation => {
        const { state, startTime } = operation.config;
        
        if ((state === 'success' || state === 'error') && startTime) {
          const elapsed = now - startTime.getTime();
          if (elapsed > cleanupDelay) {
            manager.removeOperation(operation.id);
          }
        }
      });
    };

    const interval = setInterval(cleanup, 1000);
    return () => clearInterval(interval);
  }, [operations, autoCleanup, cleanupDelay, manager]);

  const isLoading = operations.some(op => op.config.state === 'loading');

  const isOperationLoading = useCallback((id: string): boolean => {
    return manager.isOperationLoading(id);
  }, [manager]);

  const getOperation = useCallback((id: string): LoadingOperation | undefined => {
    return manager.getOperation(id);
  }, [manager]);

  const startOperation = useCallback((id: string, name: string, message?: string): void => {
    manager.startOperation(id, name, message);
  }, [manager]);

  const updateProgress = useCallback((id: string, progress: number, message?: string): void => {
    manager.updateProgress(id, progress, message);
  }, [manager]);

  const completeOperation = useCallback((id: string, message?: string): void => {
    manager.completeOperation(id, message);
  }, [manager]);

  const failOperation = useCallback((id: string, message?: string): void => {
    manager.failOperation(id, message);
  }, [manager]);

  const removeOperation = useCallback((id: string): void => {
    manager.removeOperation(id);
  }, [manager]);

  const clear = useCallback((): void => {
    manager.clear();
  }, [manager]);

  return {
    operations,
    isLoading,
    isOperationLoading,
    getOperation,
    startOperation,
    updateProgress,
    completeOperation,
    failOperation,
    removeOperation,
    clear,
  };
}