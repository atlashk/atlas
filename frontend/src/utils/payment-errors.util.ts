// Payment error types and codes
export enum PaymentErrorCode {
  // Generic errors
  UNKNOWN_ERROR = 'UNKNOWN_ERROR',
  NETWORK_ERROR = 'NETWORK_ERROR',
  TIMEOUT_ERROR = 'TIMEOUT_ERROR',
  
  // Validation errors
  INVALID_PAYMENT_METHOD = 'INVALID_PAYMENT_METHOD',
  INVALID_AMOUNT = 'INVALID_AMOUNT',
  INVALID_CURRENCY = 'INVALID_CURRENCY',
  MISSING_REQUIRED_FIELD = 'MISSING_REQUIRED_FIELD',
  
  // Payment processing errors
  PAYMENT_DECLINED = 'PAYMENT_DECLINED',
  INSUFFICIENT_FUNDS = 'INSUFFICIENT_FUNDS',
  CARD_EXPIRED = 'CARD_EXPIRED',
  INVALID_CARD = 'INVALID_CARD',
  PROCESSING_ERROR = 'PROCESSING_ERROR',
  
  // Authentication errors
  AUTHENTICATION_FAILED = 'AUTHENTICATION_FAILED',
  AUTHORIZATION_FAILED = 'AUTHORIZATION_FAILED',
  
  // Service errors
  SERVICE_UNAVAILABLE = 'SERVICE_UNAVAILABLE',
  RATE_LIMIT_EXCEEDED = 'RATE_LIMIT_EXCEEDED',
  CONFIGURATION_ERROR = 'CONFIGURATION_ERROR',
  
  // Stripe specific errors
  STRIPE_INITIALIZATION_FAILED = 'STRIPE_INITIALIZATION_FAILED',
  STRIPE_ELEMENTS_NOT_READY = 'STRIPE_ELEMENTS_NOT_READY',
  STRIPE_PAYMENT_FAILED = 'STRIPE_PAYMENT_FAILED',
}

export interface PaymentError {
  code: PaymentErrorCode;
  message: string;
  details?: Record<string, any>;
  timestamp: Date;
  retryable: boolean;
  userMessage: string;
}

export class PaymentErrorHandler {
  private static readonly ERROR_MESSAGES: Record<PaymentErrorCode, string> = {
    [PaymentErrorCode.UNKNOWN_ERROR]: 'An unexpected error occurred',
    [PaymentErrorCode.NETWORK_ERROR]: 'Network connection failed',
    [PaymentErrorCode.TIMEOUT_ERROR]: 'Request timed out',
    
    [PaymentErrorCode.INVALID_PAYMENT_METHOD]: 'Invalid payment method selected',
    [PaymentErrorCode.INVALID_AMOUNT]: 'Invalid payment amount',
    [PaymentErrorCode.INVALID_CURRENCY]: 'Invalid currency specified',
    [PaymentErrorCode.MISSING_REQUIRED_FIELD]: 'Required field is missing',
    
    [PaymentErrorCode.PAYMENT_DECLINED]: 'Payment was declined',
    [PaymentErrorCode.INSUFFICIENT_FUNDS]: 'Insufficient funds',
    [PaymentErrorCode.CARD_EXPIRED]: 'Card has expired',
    [PaymentErrorCode.INVALID_CARD]: 'Invalid card information',
    [PaymentErrorCode.PROCESSING_ERROR]: 'Payment processing failed',
    
    [PaymentErrorCode.AUTHENTICATION_FAILED]: 'Authentication failed',
    [PaymentErrorCode.AUTHORIZATION_FAILED]: 'Authorization failed',
    
    [PaymentErrorCode.SERVICE_UNAVAILABLE]: 'Payment service is temporarily unavailable',
    [PaymentErrorCode.RATE_LIMIT_EXCEEDED]: 'Too many requests, please try again later',
    [PaymentErrorCode.CONFIGURATION_ERROR]: 'Payment configuration error',
    
    [PaymentErrorCode.STRIPE_INITIALIZATION_FAILED]: 'Failed to initialize Stripe',
    [PaymentErrorCode.STRIPE_ELEMENTS_NOT_READY]: 'Stripe elements not ready',
    [PaymentErrorCode.STRIPE_PAYMENT_FAILED]: 'Stripe payment processing failed',
  };

  private static readonly USER_MESSAGES: Record<PaymentErrorCode, string> = {
    [PaymentErrorCode.UNKNOWN_ERROR]: 'Something went wrong. Please try again.',
    [PaymentErrorCode.NETWORK_ERROR]: 'Please check your internet connection and try again.',
    [PaymentErrorCode.TIMEOUT_ERROR]: 'The request took too long. Please try again.',
    
    [PaymentErrorCode.INVALID_PAYMENT_METHOD]: 'Please select a valid payment method.',
    [PaymentErrorCode.INVALID_AMOUNT]: 'Please enter a valid amount.',
    [PaymentErrorCode.INVALID_CURRENCY]: 'The selected currency is not supported.',
    [PaymentErrorCode.MISSING_REQUIRED_FIELD]: 'Please fill in all required fields.',
    
    [PaymentErrorCode.PAYMENT_DECLINED]: 'Your payment was declined. Please try a different payment method.',
    [PaymentErrorCode.INSUFFICIENT_FUNDS]: 'Insufficient funds. Please try a different payment method.',
    [PaymentErrorCode.CARD_EXPIRED]: 'Your card has expired. Please use a different card.',
    [PaymentErrorCode.INVALID_CARD]: 'Please check your card information and try again.',
    [PaymentErrorCode.PROCESSING_ERROR]: 'Payment could not be processed. Please try again.',
    
    [PaymentErrorCode.AUTHENTICATION_FAILED]: 'Authentication failed. Please log in and try again.',
    [PaymentErrorCode.AUTHORIZATION_FAILED]: 'You are not authorized to perform this action.',
    
    [PaymentErrorCode.SERVICE_UNAVAILABLE]: 'Payment service is temporarily unavailable. Please try again later.',
    [PaymentErrorCode.RATE_LIMIT_EXCEEDED]: 'Too many attempts. Please wait a moment and try again.',
    [PaymentErrorCode.CONFIGURATION_ERROR]: 'Payment system configuration error. Please contact support.',
    
    [PaymentErrorCode.STRIPE_INITIALIZATION_FAILED]: 'Payment system initialization failed. Please refresh and try again.',
    [PaymentErrorCode.STRIPE_ELEMENTS_NOT_READY]: 'Payment form is not ready. Please wait a moment.',
    [PaymentErrorCode.STRIPE_PAYMENT_FAILED]: 'Payment processing failed. Please try again.',
  };

  private static readonly RETRYABLE_ERRORS: Set<PaymentErrorCode> = new Set([
    PaymentErrorCode.NETWORK_ERROR,
    PaymentErrorCode.TIMEOUT_ERROR,
    PaymentErrorCode.SERVICE_UNAVAILABLE,
    PaymentErrorCode.PROCESSING_ERROR,
    PaymentErrorCode.STRIPE_INITIALIZATION_FAILED,
  ]);

  static createError(
    code: PaymentErrorCode,
    details?: Record<string, any>,
    customMessage?: string
  ): PaymentError {
    return {
      code,
      message: customMessage || this.ERROR_MESSAGES[code],
      details,
      timestamp: new Date(),
      retryable: this.RETRYABLE_ERRORS.has(code),
      userMessage: this.USER_MESSAGES[code],
    };
  }

  static fromError(error: unknown, fallbackCode = PaymentErrorCode.UNKNOWN_ERROR): PaymentError {
    if (error instanceof Error) {
      // Try to map common error messages to specific codes
      const code = this.mapErrorMessageToCode(error.message) || fallbackCode;
      return this.createError(code, { originalError: error.message });
    }

    if (typeof error === 'string') {
      const code = this.mapErrorMessageToCode(error) || fallbackCode;
      return this.createError(code, { originalError: error });
    }

    return this.createError(fallbackCode, { originalError: String(error) });
  }

  static fromStripeError(stripeError: any): PaymentError {
    const { type, code, message } = stripeError;
    
    // Map Stripe error types to our error codes
    switch (type) {
      case 'card_error':
        switch (code) {
          case 'card_declined':
            return this.createError(PaymentErrorCode.PAYMENT_DECLINED, { stripeError });
          case 'insufficient_funds':
            return this.createError(PaymentErrorCode.INSUFFICIENT_FUNDS, { stripeError });
          case 'expired_card':
            return this.createError(PaymentErrorCode.CARD_EXPIRED, { stripeError });
          case 'invalid_number':
          case 'invalid_expiry_month':
          case 'invalid_expiry_year':
          case 'invalid_cvc':
            return this.createError(PaymentErrorCode.INVALID_CARD, { stripeError });
          default:
            return this.createError(PaymentErrorCode.STRIPE_PAYMENT_FAILED, { stripeError });
        }
      
      case 'validation_error':
        return this.createError(PaymentErrorCode.MISSING_REQUIRED_FIELD, { stripeError });
      
      case 'api_error':
        return this.createError(PaymentErrorCode.SERVICE_UNAVAILABLE, { stripeError });
      
      case 'rate_limit_error':
        return this.createError(PaymentErrorCode.RATE_LIMIT_EXCEEDED, { stripeError });
      
      default:
        return this.createError(PaymentErrorCode.STRIPE_PAYMENT_FAILED, { stripeError }, message);
    }
  }

  private static mapErrorMessageToCode(message: string): PaymentErrorCode | null {
    const lowerMessage = message.toLowerCase();
    
    if (lowerMessage.includes('network') || lowerMessage.includes('connection')) {
      return PaymentErrorCode.NETWORK_ERROR;
    }
    
    if (lowerMessage.includes('timeout') || lowerMessage.includes('timed out')) {
      return PaymentErrorCode.TIMEOUT_ERROR;
    }
    
    if (lowerMessage.includes('declined')) {
      return PaymentErrorCode.PAYMENT_DECLINED;
    }
    
    if (lowerMessage.includes('insufficient')) {
      return PaymentErrorCode.INSUFFICIENT_FUNDS;
    }
    
    if (lowerMessage.includes('expired')) {
      return PaymentErrorCode.CARD_EXPIRED;
    }
    
    if (lowerMessage.includes('invalid card') || lowerMessage.includes('invalid number')) {
      return PaymentErrorCode.INVALID_CARD;
    }
    
    if (lowerMessage.includes('authentication')) {
      return PaymentErrorCode.AUTHENTICATION_FAILED;
    }
    
    if (lowerMessage.includes('authorization')) {
      return PaymentErrorCode.AUTHORIZATION_FAILED;
    }
    
    if (lowerMessage.includes('rate limit')) {
      return PaymentErrorCode.RATE_LIMIT_EXCEEDED;
    }
    
    if (lowerMessage.includes('service unavailable') || lowerMessage.includes('server error')) {
      return PaymentErrorCode.SERVICE_UNAVAILABLE;
    }
    
    return null;
  }

  static isRetryable(error: PaymentError): boolean {
    return error.retryable;
  }

  static shouldShowToUser(error: PaymentError): boolean {
    // Don't show technical errors to users
    const technicalErrors = [
      PaymentErrorCode.CONFIGURATION_ERROR,
      PaymentErrorCode.STRIPE_INITIALIZATION_FAILED,
      PaymentErrorCode.STRIPE_ELEMENTS_NOT_READY,
    ];
    
    return !technicalErrors.includes(error.code);
  }

  static formatForLogging(error: PaymentError): string {
    return JSON.stringify({
      code: error.code,
      message: error.message,
      timestamp: error.timestamp.toISOString(),
      details: error.details,
    }, null, 2);
  }
}