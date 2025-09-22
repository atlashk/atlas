// Loading state types and utilities
export enum LoadingState {
  IDLE = 'idle',
  LOADING = 'loading',
  SUCCESS = 'success',
  ERROR = 'error',
}

export interface LoadingStateConfig {
  state: LoadingState;
  message?: string;
  progress?: number;
  startTime?: Date;
  duration?: number;
}

export interface LoadingOperation {
  id: string;
  name: string;
  config: LoadingStateConfig;
}

export class LoadingStateManager {
  private operations: Map<string, LoadingOperation> = new Map();
  private listeners: Set<(operations: LoadingOperation[]) => void> = new Set();

  // Start a loading operation
  startOperation(id: string, name: string, message?: string): void {
    const operation: LoadingOperation = {
      id,
      name,
      config: {
        state: LoadingState.LOADING,
        message,
        startTime: new Date(),
      },
    };

    this.operations.set(id, operation);
    this.notifyListeners();
  }

  // Update operation progress
  updateProgress(id: string, progress: number, message?: string): void {
    const operation = this.operations.get(id);
    if (operation) {
      operation.config.progress = Math.max(0, Math.min(100, progress));
      if (message) {
        operation.config.message = message;
      }
      this.notifyListeners();
    }
  }

  // Complete operation successfully
  completeOperation(id: string, message?: string): void {
    const operation = this.operations.get(id);
    if (operation) {
      operation.config.state = LoadingState.SUCCESS;
      operation.config.progress = 100;
      if (message) {
        operation.config.message = message;
      }
      if (operation.config.startTime) {
        operation.config.duration = Date.now() - operation.config.startTime.getTime();
      }
      this.notifyListeners();
      
      // Auto-remove successful operations after a delay
      setTimeout(() => {
        this.removeOperation(id);
      }, 2000);
    }
  }

  // Fail operation with error
  failOperation(id: string, message?: string): void {
    const operation = this.operations.get(id);
    if (operation) {
      operation.config.state = LoadingState.ERROR;
      if (message) {
        operation.config.message = message;
      }
      if (operation.config.startTime) {
        operation.config.duration = Date.now() - operation.config.startTime.getTime();
      }
      this.notifyListeners();
    }
  }

  // Remove operation
  removeOperation(id: string): void {
    if (this.operations.delete(id)) {
      this.notifyListeners();
    }
  }

  // Get operation by id
  getOperation(id: string): LoadingOperation | undefined {
    return this.operations.get(id);
  }

  // Get all operations
  getAllOperations(): LoadingOperation[] {
    return Array.from(this.operations.values());
  }

  // Get operations by state
  getOperationsByState(state: LoadingState): LoadingOperation[] {
    return this.getAllOperations().filter(op => op.config.state === state);
  }

  // Check if any operation is loading
  isAnyLoading(): boolean {
    return this.getOperationsByState(LoadingState.LOADING).length > 0;
  }

  // Check if specific operation is loading
  isOperationLoading(id: string): boolean {
    const operation = this.getOperation(id);
    return operation?.config.state === LoadingState.LOADING;
  }

  // Subscribe to changes
  subscribe(listener: (operations: LoadingOperation[]) => void): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  // Clear all operations
  clear(): void {
    this.operations.clear();
    this.notifyListeners();
  }

  private notifyListeners(): void {
    const operations = this.getAllOperations();
    this.listeners.forEach(listener => listener(operations));
  }
}

// Global loading state manager instance
export const globalLoadingManager = new LoadingStateManager();

// Payment-specific loading operation IDs
export const PaymentLoadingOperations = {
  INITIALIZE_PAYMENT: 'payment.initialize',
  PROCESS_PAYMENT: 'payment.process',
  CONFIRM_PAYMENT: 'payment.confirm',
  CREATE_PAYMENT_METHOD: 'payment.create_method',
  VALIDATE_PAYMENT: 'payment.validate',
  FETCH_PAYMENT_STATUS: 'payment.fetch_status',
  STRIPE_INITIALIZE: 'stripe.initialize',
  STRIPE_LOAD_ELEMENTS: 'stripe.load_elements',
} as const;

// Payment loading messages
export const PaymentLoadingMessages = {
  INITIALIZING: 'Initializing payment...',
  PROCESSING: 'Processing payment...',
  CONFIRMING: 'Confirming payment...',
  VALIDATING: 'Validating payment information...',
  CREATING_METHOD: 'Creating payment method...',
  FETCHING_STATUS: 'Checking payment status...',
  STRIPE_LOADING: 'Loading Stripe...',
  STRIPE_ELEMENTS: 'Preparing payment form...',
  COMPLETING: 'Completing payment...',
  SUCCESS: 'Payment completed successfully!',
  FAILED: 'Payment failed',
} as const;

// Utility functions for common loading patterns
export class PaymentLoadingUtils {
  static async withLoading<T>(
    operationId: string,
    operationName: string,
    operation: () => Promise<T>,
    options: {
      startMessage?: string;
      successMessage?: string;
      errorMessage?: string;
      manager?: LoadingStateManager;
    } = {}
  ): Promise<T> {
    const {
      startMessage = PaymentLoadingMessages.PROCESSING,
      successMessage = PaymentLoadingMessages.SUCCESS,
      errorMessage = PaymentLoadingMessages.FAILED,
      manager = globalLoadingManager,
    } = options;

    try {
      manager.startOperation(operationId, operationName, startMessage);
      const result = await operation();
      manager.completeOperation(operationId, successMessage);
      return result;
    } catch (error) {
      const message = error instanceof Error ? error.message : errorMessage;
      manager.failOperation(operationId, message);
      throw error;
    }
  }

  static async withProgress<T>(
    operationId: string,
    operationName: string,
    operation: (updateProgress: (progress: number, message?: string) => void) => Promise<T>,
    options: {
      startMessage?: string;
      successMessage?: string;
      errorMessage?: string;
      manager?: LoadingStateManager;
    } = {}
  ): Promise<T> {
    const {
      startMessage = PaymentLoadingMessages.PROCESSING,
      successMessage = PaymentLoadingMessages.SUCCESS,
      errorMessage = PaymentLoadingMessages.FAILED,
      manager = globalLoadingManager,
    } = options;

    try {
      manager.startOperation(operationId, operationName, startMessage);
      
      const updateProgress = (progress: number, message?: string) => {
        manager.updateProgress(operationId, progress, message);
      };

      const result = await operation(updateProgress);
      manager.completeOperation(operationId, successMessage);
      return result;
    } catch (error) {
      const message = error instanceof Error ? error.message : errorMessage;
      manager.failOperation(operationId, message);
      throw error;
    }
  }

  static createPaymentOperation(type: keyof typeof PaymentLoadingOperations): string {
    return `${PaymentLoadingOperations[type]}_${Date.now()}`;
  }

  static getLoadingMessage(operation: string): string {
    switch (operation) {
      case PaymentLoadingOperations.INITIALIZE_PAYMENT:
        return PaymentLoadingMessages.INITIALIZING;
      case PaymentLoadingOperations.PROCESS_PAYMENT:
        return PaymentLoadingMessages.PROCESSING;
      case PaymentLoadingOperations.CONFIRM_PAYMENT:
        return PaymentLoadingMessages.CONFIRMING;
      case PaymentLoadingOperations.CREATE_PAYMENT_METHOD:
        return PaymentLoadingMessages.CREATING_METHOD;
      case PaymentLoadingOperations.VALIDATE_PAYMENT:
        return PaymentLoadingMessages.VALIDATING;
      case PaymentLoadingOperations.FETCH_PAYMENT_STATUS:
        return PaymentLoadingMessages.FETCHING_STATUS;
      case PaymentLoadingOperations.STRIPE_INITIALIZE:
        return PaymentLoadingMessages.STRIPE_LOADING;
      case PaymentLoadingOperations.STRIPE_LOAD_ELEMENTS:
        return PaymentLoadingMessages.STRIPE_ELEMENTS;
      default:
        return PaymentLoadingMessages.PROCESSING;
    }
  }
}

// React hook for using loading state
export interface UseLoadingStateReturn {
  operations: LoadingOperation[];
  isLoading: boolean;
  isOperationLoading: (id: string) => boolean;
  getOperation: (id: string) => LoadingOperation | undefined;
  startOperation: (id: string, name: string, message?: string) => void;
  updateProgress: (id: string, progress: number, message?: string) => void;
  completeOperation: (id: string, message?: string) => void;
  failOperation: (id: string, message?: string) => void;
  removeOperation: (id: string) => void;
  clear: () => void;
}

// Note: This would typically be implemented as a React hook
// but since we're in a utility file, we'll export the interface
// The actual hook implementation would be in the hooks directory