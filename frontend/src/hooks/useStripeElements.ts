import { useState, useCallback, useEffect, useRef } from 'react';
import { loadStripe, Stripe, StripeElements, ConfirmPaymentData, StripeElementLocale } from '@stripe/stripe-js';

export interface StripeElementsState {
  stripe: Stripe | null;
  elements: StripeElements | null;
  isLoading: boolean;
  error: string | null;
  isReady: boolean;
}

export interface UseStripeElementsOptions {
  publishableKey: string;
  options?: {
    locale?: StripeElementLocale;
    appearance?: Record<string, unknown>;
    clientSecret?: string;
  };
  onReady?: (stripe: Stripe, elements: StripeElements) => void;
  onError?: (error: string) => void;
}

export interface UseStripeElementsReturn {
  state: StripeElementsState;
  actions: {
    confirmPayment: (data: ConfirmPaymentData) => Promise<unknown>;
    createPaymentMethod: (data: Record<string, unknown>) => Promise<unknown>;
    retrievePaymentIntent: (clientSecret: string) => Promise<unknown>;
    reset: () => void;
  };
  helpers: {
    isElementsReady: () => boolean;
    canProcessPayment: () => boolean;
  };
}

export function useStripeElements(options: UseStripeElementsOptions): UseStripeElementsReturn {
  const { publishableKey, options: stripeOptions, onReady, onError } = options;
  
  const [state, setState] = useState<StripeElementsState>({
    stripe: null,
    elements: null,
    isLoading: true,
    error: null,
    isReady: false,
  });

  const stripePromiseRef = useRef<Promise<Stripe | null> | null>(null);
  const initializationRef = useRef<boolean>(false);

  const updateState = useCallback((updates: Partial<StripeElementsState>) => {
    setState(prev => ({ ...prev, ...updates }));
  }, []);

  const initializeStripe = useCallback(async () => {
    if (initializationRef.current) return;
    initializationRef.current = true;

    try {
      updateState({ isLoading: true, error: null });

      // Create or reuse Stripe promise
      if (!stripePromiseRef.current) {
        stripePromiseRef.current = loadStripe(publishableKey);
      }

      const stripe = await stripePromiseRef.current;
      
      if (!stripe) {
        throw new Error('Failed to load Stripe');
      }

      // Create elements instance
      const elements = stripe.elements({
        locale: stripeOptions?.locale || 'en' as StripeElementLocale,
        appearance: stripeOptions?.appearance,
        clientSecret: stripeOptions?.clientSecret,
      });

      updateState({
        stripe,
        elements,
        isLoading: false,
        isReady: true,
        error: null,
      });

      onReady?.(stripe, elements);

    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to initialize Stripe';
      updateState({
        error: errorMessage,
        isLoading: false,
        isReady: false,
      });
      onError?.(errorMessage);
    } finally {
      initializationRef.current = false;
    }
  }, [publishableKey, stripeOptions, updateState, onReady, onError]);

  const confirmPayment = useCallback(async (data: ConfirmPaymentData) => {
    if (!state.stripe || !state.elements) {
      throw new Error('Stripe not initialized');
    }

    try {
      const result = await state.stripe.confirmPayment({
        elements: state.elements,
        confirmParams: data,
        redirect: 'if_required',
      });

      if (result.error) {
        throw new Error(result.error.message || 'Payment confirmation failed');
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Payment confirmation failed';
      updateState({ error: errorMessage });
      throw error;
    }
  }, [state.stripe, state.elements, updateState]);

  const createPaymentMethod = useCallback(async (data: Record<string, unknown>) => {
    if (!state.stripe || !state.elements) {
      throw new Error('Stripe not initialized');
    }

    try {
      const result = await state.stripe.createPaymentMethod({
        elements: state.elements,
        ...data,
      });

      if (result.error) {
        throw new Error(result.error.message || 'Payment method creation failed');
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Payment method creation failed';
      updateState({ error: errorMessage });
      throw error;
    }
  }, [state.stripe, state.elements, updateState]);

  const retrievePaymentIntent = useCallback(async (clientSecret: string) => {
    if (!state.stripe) {
      throw new Error('Stripe not initialized');
    }

    try {
      const result = await state.stripe.retrievePaymentIntent(clientSecret);

      if (result.error) {
        throw new Error(result.error.message || 'Failed to retrieve payment intent');
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to retrieve payment intent';
      updateState({ error: errorMessage });
      throw error;
    }
  }, [state.stripe, updateState]);

  const reset = useCallback(() => {
    setState({
      stripe: null,
      elements: null,
      isLoading: false,
      error: null,
      isReady: false,
    });
    initializationRef.current = false;
  }, []);

  const isElementsReady = useCallback(() => {
    return state.isReady && !!state.stripe && !!state.elements;
  }, [state.isReady, state.stripe, state.elements]);

  const canProcessPayment = useCallback(() => {
    return isElementsReady() && !state.isLoading && !state.error;
  }, [isElementsReady, state.isLoading, state.error]);

  // Initialize Stripe when component mounts or options change
  useEffect(() => {
    initializeStripe();
  }, [initializeStripe]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      // Stripe instances are automatically cleaned up
      // We just need to reset our state
      reset();
    };
  }, [reset]);

  return {
    state,
    actions: {
      confirmPayment,
      createPaymentMethod,
      retrievePaymentIntent,
      reset,
    },
    helpers: {
      isElementsReady,
      canProcessPayment,
    },
  };
}