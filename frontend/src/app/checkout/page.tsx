"use client";

import { orderApi } from "@/api/order.api";
import { CheckoutForm } from "@/components/checkout/CheckoutForm";
import { CheckoutProgress } from "@/components/checkout/CheckoutProgress";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";
import { useCheckoutState } from "@/hooks/useCheckoutState";
import { useErrorHandler } from "@/hooks/useErrorHandler";
import { useOrderStatusPolling } from "@/hooks/useOrderStatusPolling";
import { usePaymentMethods } from "@/hooks/usePaymentMethods";
import { usePaymentProcessing } from "@/hooks/usePaymentProcessing";
import { useCartStore } from "@/stores/cart.store";
import { useUserStore } from "@/stores/user.store";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
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
  const { isAuthenticated } = useUserStore();

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
    availablePaymentMethods,
    selectedPaymentMethod,
    isLoading: isLoadingPaymentMethods,
    error: paymentMethodsError,
    setSelectedPaymentMethod,
    retry: retryPaymentMethods,
  } = usePaymentMethods();

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
      if (!isAuthenticated()) {
        router.push("/login");
        return;
      }

      try {
        await loadCart();
      } catch (error) {
        console.error("Failed to load cart data:", error);
        toast.error("Failed to load cart data");
      }
    };

    loadData();
  }, [isAuthenticated, loadCart, router]);

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

  const handleCheckout = async () => {
    if (!selectedPaymentMethod) {
      toast.error("Please select a payment method");
      return;
    }

    setIsCheckingOut(true);
    clearPaymentError();

    try {
      const order = await orderApi.checkout({
        paymentMethod: selectedPaymentMethod,
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

  const handlePaymentComplete = () => {
    toast.success("Payment completed successfully!");
    // Clear cart state when payment is complete
    useCartStore.getState().clearCartState();
    // Don't redirect immediately - let OrderStatus component handle the UI and navigation
  };

  const handlePaymentError = (error: string) => {
    toast.error(error);
    setPaymentNextAction(null);
    resetOrderStatus();
    resetState();
  };

  const handleRetry = () => {
    resetOrderStatus();
    setPaymentNextAction(null);
    clearPaymentError();
    // Only start polling if order is not in final state
    if (orderStatus?.status !== "FULFILLED" && orderStatus?.status !== "CANCELED") {
      startPolling();
    }
  };

  // Show checkout progress if we have an order ID
  if (orderId) {
    return (
      <CheckoutProgress
        orderId={orderId}
        orderStatus={orderStatus}
        paymentNextAction={paymentNextAction}
        paymentAmount={paymentAmount}
        paymentCurrency={paymentCurrency}
        isProcessingPayment={isProcessingPayment || isProcessingPaymentAction}
        onPaymentComplete={handlePaymentComplete}
        onPaymentError={handlePaymentError}
        isLoading={isPollingOrderStatus}
        error={orderStatusError}
        onRetry={handleRetry}
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
          cart?.cartItems?.map((item) => ({
            product: {
              id: item.product.id.toString(),
              name: item.product.name,
              price: item.product.price,
            },
            quantity: item.quantity,
          })) || [],
      }}
      cartTotal={cartTotal}
      paymentMethods={availablePaymentMethods}
      selectedPaymentMethod={selectedPaymentMethod}
      isCheckingOut={isCheckingOut}
      isLoadingPaymentMethods={isLoadingPaymentMethods}
      paymentMethodsError={paymentMethodsError}
      onPaymentMethodChange={setSelectedPaymentMethod}
      onCheckout={handleCheckout}
      onRetryPaymentMethods={retryPaymentMethods}
    />
  );
}

export default function CheckoutPage() {
  return (
    <ErrorBoundary
      onError={(error, errorInfo) => {
        console.error("Checkout page error:", error, errorInfo);
        // You could send this to an error reporting service
      }}
    >
      <div className="container mx-auto px-4 py-8">
        <CheckoutPageContent />
      </div>
    </ErrorBoundary>
  );
}
