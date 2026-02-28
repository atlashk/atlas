"use client";

import { orderApi } from "@/api/order.api";
import { CheckoutForm } from "@/components/checkout/CheckoutForm";
import { CheckoutProgress } from "@/components/checkout/CheckoutProgress";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";
import { withAuth } from '@/hoc/withAuth';
import { useCheckoutState } from "@/hooks/useCheckoutState";
import { useErrorHandler } from "@/hooks/useErrorHandler";
import { useOrderStatusPolling } from "@/hooks/useOrderStatusPolling";
import { usePaymentGateways } from "@/hooks/usePaymentGateways";
import { usePaymentProcessing } from "@/hooks/usePaymentProcessing";
import type { CartItemResponse } from "@/interfaces/order.interface";
import { useCartStore } from "@/stores/cart.store";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";

function CheckoutPageContent() {
  const router = useRouter();
  const { handleApiError } = useErrorHandler();
  const {
    cart,
    loadCart,
    getCartTotal,
    isLoading: isCartLoading,
  } = useCartStore();

  const {
    orderId,
    isCheckingOut,
    paymentNextAction,
    isProcessingPayment,
    setOrderId,
    setIsCheckingOut,
    setPaymentNextAction,
    resetState,
  } = useCheckoutState();

  // Additional state for payment amount and currency
  const [paymentAmount, setPaymentAmount] = useState<number | null>(null);
  const [paymentCurrency, setPaymentCurrency] = useState<string | null>(null);

  const {
    availablePaymentGateways,
    selectedPaymentGateway,
    isLoading: isLoadingPaymentGateways,
    error: paymentGatewaysError,
    setSelectedPaymentGateway,
  } = usePaymentGateways();

  // Address state
  const [address, setAddress] = useState({
    street: "",
    city: "",
    country: "",
    postalCode: "",
  });
  const [addressErrors, setAddressErrors] = useState<Record<string, string>>({});

  // Ref to prevent multiple payment completion calls
  const paymentCompletedRef = useRef(false);

  const {
    isLoading: isProcessingPaymentAction,
    fetchPaymentNextAction,
    clearError: clearPaymentError,
  } = usePaymentProcessing();

  // Order status polling
  const {
    orderStatus,
    isLoading: isPollingOrderStatus,
    error: orderStatusError,
    needsPaymentProcessing,
    startPolling,
    stopPolling,
    reset: resetOrderStatus,
  } = useOrderStatusPolling(orderId);

  // Load cart data on component mount
  useEffect(() => {
    const loadData = async () => {
      // Reset payment completion flag on mount
      paymentCompletedRef.current = false;

      // Only load cart if we don't have it yet and not currently loading
       if (!cart && !isCartLoading) {
         try {
           await loadCart();
         } catch (error) {
           console.error("Failed to load cart data:", error);
           toast.error("Failed to load cart data");
         }
       }
     };

     loadData();
   }, [cart, isCartLoading, loadCart]);

  // Get actual cart total
  const cartTotal = getCartTotal();

  // Redirect if cart is empty (but not when we have an active order)
  useEffect(() => {
    if (!isCartLoading && cart && cart.cartItems.length === 0 && !orderId) {
      toast.error("Your cart is empty");
      router.push("/cart");
    }
  }, [cart, isCartLoading, router, orderId]);

  // Handle payment next action when order status indicates payment processing is needed
  useEffect(() => {
    const handlePaymentNextAction = async () => {
      if (needsPaymentProcessing && orderId && !paymentNextAction) {
        try {
          const response = await fetchPaymentNextAction(orderId);
          if (response && response.nextAction) {
            setPaymentNextAction(response.nextAction);
            setPaymentAmount(response.amount || null);
            setPaymentCurrency(response.currency || null);
          }
        } catch (error) {
          console.error("Failed to fetch payment next action:", error);
          toast.error("Failed to initialize payment processing");
        }
      }
    };

    handlePaymentNextAction();
  }, [
    needsPaymentProcessing,
    orderId,
    paymentNextAction,
    fetchPaymentNextAction,
    setPaymentNextAction,
  ]);

  const validateAddress = () => {
    const errors: Record<string, string> = {};
    
    if (!address.street.trim()) {
      errors.street = "Street address is required";
    }
    if (!address.city.trim()) {
      errors.city = "City is required";
    }
    if (!address.country.trim()) {
      errors.country = "Country is required";
    }
    if (!address.postalCode.trim()) {
      errors.postalCode = "Postal code is required";
    }
    
    setAddressErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleCheckout = async () => {
    if (!selectedPaymentGateway) {
      toast.error("Please select a payment gateway");
      return;
    }

    if (!validateAddress()) {
      toast.error("Please fill in all address fields");
      return;
    }

    setIsCheckingOut(true);
    clearPaymentError();

    try {
      const order = await orderApi.checkout({
        address,
        paymentGatewayId: selectedPaymentGateway.id,
      });

      setOrderId(order.data.orderId.toString());

      // Start polling order status instead of immediately calling next-action
      // The polling will handle calling next-action when appropriate
    } catch (error) {
      handleApiError(error as Error);
    } finally {
      setIsCheckingOut(false);
    }
  };

  const handlePaymentComplete = useCallback(() => {
    // Prevent multiple calls
    if (paymentCompletedRef.current) {
      return;
    }
    
    paymentCompletedRef.current = true;
    
    // Toast is now handled by CheckoutProgress when order status becomes FULFILLED
    // Clear cart state when payment is complete
    useCartStore.getState().clearCartState();
    // Don't redirect immediately - let OrderStatus component handle the UI and navigation
  }, []); // Remove handlePaymentComplete from dependency array

  const handlePaymentError = (error: string) => {
    toast.error(error);
    setPaymentNextAction(null);
    resetOrderStatus();
    resetState();
    // Reset payment completion flag for retry
    paymentCompletedRef.current = false;
  };

  // Show checkout progress if we have an order ID
  if (orderId) {
    return (
      <CheckoutProgress
        orderId={orderId}
        orderStatus={orderStatus}
        paymentNextAction={paymentNextAction}
        selectedPaymentGateway={selectedPaymentGateway}
        paymentAmount={paymentAmount}
        paymentCurrency={paymentCurrency}
        isProcessingPayment={isProcessingPayment || isProcessingPaymentAction}
        onPaymentComplete={handlePaymentComplete}
        onPaymentError={handlePaymentError}
        isLoading={isPollingOrderStatus}
        error={orderStatusError}
        startPolling={startPolling}
        stopPolling={stopPolling}
      />
    );
  }

  // Show checkout form
  return (
    <CheckoutForm
      cart={{
        items:
          cart?.cartItems?.map((item: CartItemResponse) => ({
            product: {
              id: item.product.id.toString(),
              name: item.product.name,
              price: item.product.price,
            },
            quantity: item.quantity,
          })) || [],
      }}
      cartTotal={cartTotal}
      paymentGateways={availablePaymentGateways}
      selectedPaymentGateway={selectedPaymentGateway}
      address={address}
      addressErrors={addressErrors}
      isCheckingOut={isCheckingOut}
      isLoadingPaymentGateways={isLoadingPaymentGateways}
      paymentGatewaysError={paymentGatewaysError}
      onPaymentGatewayChange={setSelectedPaymentGateway}
      onAddressChange={setAddress}
      onCheckout={handleCheckout}
    />
  );
}

function CheckoutPageWrapper() {
  return (
    <ErrorBoundary
      onError={(error, errorInfo) => {
        console.error("Checkout page error:", error, errorInfo);
        // You could send this to an error reporting service
      }}
    >
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4">
          <CheckoutPageContent />
        </div>
      </div>
    </ErrorBoundary>
  );
}

// Export with authentication HOC that requires USER role
export default withAuth(CheckoutPageWrapper, { requireAuth: true, allowedRoles: ['USER'] });
