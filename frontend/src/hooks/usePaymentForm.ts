import { useState, useCallback, useEffect } from 'react';
import { paymentGatewayService } from '@/services/payment';
import type { PaymentMethod } from '@/services/payment';

export interface PaymentFormData {
  [key: string]: any;
}

export interface PaymentFormState {
  isValid: boolean;
  errors: Record<string, string>;
  isSubmitting: boolean;
  data: PaymentFormData;
}

export interface UsePaymentFormOptions {
  paymentMethod: PaymentMethod;
  onSubmit?: (data: PaymentFormData) => Promise<void>;
  onValidationChange?: (isValid: boolean) => void;
  validateOnChange?: boolean;
}

export interface UsePaymentFormReturn {
  state: PaymentFormState;
  actions: {
    updateField: (field: string, value: any) => void;
    updateData: (data: Partial<PaymentFormData>) => void;
    validateForm: () => boolean;
    submitForm: () => Promise<void>;
    resetForm: () => void;
    setErrors: (errors: Record<string, string>) => void;
    clearErrors: () => void;
  };
  helpers: {
    getFieldError: (field: string) => string | undefined;
    hasFieldError: (field: string) => boolean;
    isFieldValid: (field: string) => boolean;
  };
}

export function usePaymentForm(options: UsePaymentFormOptions): UsePaymentFormReturn {
  const { paymentMethod, onSubmit, onValidationChange, validateOnChange = true } = options;
  
  const [state, setState] = useState<PaymentFormState>({
    isValid: false,
    errors: {},
    isSubmitting: false,
    data: {},
  });

  const updateState = useCallback((updates: Partial<PaymentFormState>) => {
    setState(prev => ({ ...prev, ...updates }));
  }, []);

  const validateField = useCallback((field: string, value: any): string | null => {
    // Basic validation rules - can be extended based on payment method
    switch (field) {
      case 'cardNumber':
        if (!value || value.length < 13) {
          return 'Card number must be at least 13 digits';
        }
        break;
      case 'expiryDate':
        if (!value || !/^\d{2}\/\d{2}$/.test(value)) {
          return 'Expiry date must be in MM/YY format';
        }
        break;
      case 'cvv':
        if (!value || value.length < 3) {
          return 'CVV must be at least 3 digits';
        }
        break;
      case 'email':
        if (!value || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          return 'Please enter a valid email address';
        }
        break;
      case 'amount':
        if (!value || value <= 0) {
          return 'Amount must be greater than 0';
        }
        break;
      default:
        if (!value && field.includes('required')) {
          return `${field} is required`;
        }
    }
    return null;
  }, []);

  const validateForm = useCallback((): boolean => {
    const newErrors: Record<string, string> = {};
    let isValid = true;

    // Validate all fields in the current data
    Object.entries(state.data).forEach(([field, value]) => {
      const error = validateField(field, value);
      if (error) {
        newErrors[field] = error;
        isValid = false;
      }
    });

    // Check for required fields based on payment method
    const requiredFields = getRequiredFields(paymentMethod);
    requiredFields.forEach(field => {
      if (!state.data[field]) {
        newErrors[field] = `${field} is required`;
        isValid = false;
      }
    });

    updateState({ errors: newErrors, isValid });
    onValidationChange?.(isValid);
    
    return isValid;
  }, [state.data, paymentMethod, validateField, updateState, onValidationChange]);

  const getRequiredFields = useCallback((method: PaymentMethod): string[] => {
    switch (method) {
      case 'stripe':
        return ['cardNumber', 'expiryDate', 'cvv'];
      case 'qr_code':
        return ['amount'];
      case 'redirect':
        return ['email', 'amount'];
      case 'deep_link':
        return ['amount'];
      default:
        return [];
    }
  }, []);

  const updateField = useCallback((field: string, value: any) => {
    const newData = { ...state.data, [field]: value };
    let newErrors = { ...state.errors };

    // Validate field if validateOnChange is enabled
    if (validateOnChange) {
      const error = validateField(field, value);
      if (error) {
        newErrors[field] = error;
      } else {
        delete newErrors[field];
      }
    }

    const isValid = Object.keys(newErrors).length === 0 && 
                   getRequiredFields(paymentMethod).every(f => newData[f]);

    updateState({
      data: newData,
      errors: newErrors,
      isValid,
    });

    if (validateOnChange) {
      onValidationChange?.(isValid);
    }
  }, [state.data, state.errors, validateOnChange, validateField, paymentMethod, getRequiredFields, updateState, onValidationChange]);

  const updateData = useCallback((data: Partial<PaymentFormData>) => {
    const newData = { ...state.data, ...data };
    updateState({ data: newData });
    
    if (validateOnChange) {
      // Re-validate after data update
      setTimeout(() => validateForm(), 0);
    }
  }, [state.data, validateOnChange, updateState, validateForm]);

  const submitForm = useCallback(async () => {
    if (!validateForm()) {
      return;
    }

    try {
      updateState({ isSubmitting: true });
      await onSubmit?.(state.data);
    } catch (error) {
      // Error handling is done by the parent component
      console.error('Form submission error:', error);
    } finally {
      updateState({ isSubmitting: false });
    }
  }, [validateForm, onSubmit, state.data, updateState]);

  const resetForm = useCallback(() => {
    updateState({
      isValid: false,
      errors: {},
      isSubmitting: false,
      data: {},
    });
  }, [updateState]);

  const setErrors = useCallback((errors: Record<string, string>) => {
    updateState({ errors, isValid: Object.keys(errors).length === 0 });
  }, [updateState]);

  const clearErrors = useCallback(() => {
    updateState({ errors: {}, isValid: true });
  }, [updateState]);

  const getFieldError = useCallback((field: string): string | undefined => {
    return state.errors[field];
  }, [state.errors]);

  const hasFieldError = useCallback((field: string): boolean => {
    return !!state.errors[field];
  }, [state.errors]);

  const isFieldValid = useCallback((field: string): boolean => {
    return !state.errors[field] && !!state.data[field];
  }, [state.errors, state.data]);

  // Validate form when payment method changes
  useEffect(() => {
    if (validateOnChange && Object.keys(state.data).length > 0) {
      validateForm();
    }
  }, [paymentMethod, validateOnChange, state.data, validateForm]);

  return {
    state,
    actions: {
      updateField,
      updateData,
      validateForm,
      submitForm,
      resetForm,
      setErrors,
      clearErrors,
    },
    helpers: {
      getFieldError,
      hasFieldError,
      isFieldValid,
    },
  };
}