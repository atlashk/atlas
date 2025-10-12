import { orderApi } from "@/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PaymentNextAction } from "@/interfaces";
import { PaymentMethod } from "@/constants/payment.constants";
import { CartResponse, CartItemResponse } from "@/interfaces/cart.interface";
import { PaymentFormProps, PaymentResult, paymentService } from "@/services/paymentService";
import { useCartStore, useUserStore } from "@/stores";
import { formatCurrency } from "@/utils/formatter.util";
import { Minus, Plus, X } from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useEffect, useState } from "react";
import { toast } from "sonner";

const Cart: React.FC = () => {
  const [isProcessing, setIsProcessing] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod>("CARD");
  const [paymentMethods, setPaymentMethods] = useState<string[]>([]);
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [currentSagaId, setCurrentSagaId] = useState<string | null>(null);
  const [PaymentFormComponent, setPaymentFormComponent] = useState<React.ComponentType<PaymentFormProps> | null>(null);
  const [paymentNextAction, setPaymentNextAction] = useState<PaymentNextAction | null>(null);
  const [paymentStatusModal, setPaymentStatusModal] = useState({
    isOpen: false,
    isSuccess: false,
    message: '',
  });
  const {
    cart,
    loadCart,
    getCartTotal,
    getItemTotal,
    removeFromCart,
    updateQuantity,
    clearCart,
    isLoading,
    error,
  } = useCartStore();
  const { isAuthenticated } = useUserStore();
  const router = useRouter();

  const total = getCartTotal();

  // Load cart data and payment methods on component mount
  useEffect(() => {
    const loadData = async () => {
      if (!isAuthenticated()) return;

      try {
        await loadCart();
        
        // Load payment methods
        const response = await fetch('/api/payment-methods');
        if (response.ok) {
          const result = await response.json();
          if (result.success && result.data) {
            setPaymentMethods(result.data);
            if (result.data.length > 0) {
              setSelectedPaymentMethod(result.data[0]);
            }
          }
        }
      } catch (error) {
        console.error('Failed to load data:', error);
        toast.error('Failed to load cart data');
      }
    };

    loadData();
  }, [isAuthenticated, loadCart]);

  // Poll payment status when we have a sagaId
  useEffect(() => {
    if (!currentSagaId) return;

    const pollPaymentStatus = async () => {
      try {
        const response = await fetch(`/api/payments/${currentSagaId}/tracking`);
        if (response.ok) {
          const result = await response.json();
          if (result.success && result.data) {
            const { status, nextAction } = result.data;
            
            if (status === 'AWAITING_PAYMENT' && nextAction) {
              setPaymentNextAction(nextAction);
              
              // Initialize payment handler and get payment form component
              await paymentService.initializeHandler(nextAction);
              const PaymentForm = await paymentService.createPaymentForm(nextAction);
              setPaymentFormComponent(() => PaymentForm);
              setShowPaymentForm(true);
            } else if (status === 'PAYMENT_SUCCEEDED') {
              setShowPaymentForm(false);
              setPaymentStatusModal({
                isOpen: true,
                isSuccess: true,
                message: 'Payment completed successfully! Your order is being processed.',
              });
              setCurrentSagaId(null); // Stop polling
            } else if (status === 'PAYMENT_FAILED') {
              setShowPaymentForm(false);
              setPaymentStatusModal({
                isOpen: true,
                isSuccess: false,
                message: 'Payment failed. Please try again.',
              });
              setCurrentSagaId(null); // Stop polling
            }
          }
        }
      } catch (error) {
        console.error('Error polling payment status:', error);
      }
    };

    // Poll every 2 seconds
    const interval = setInterval(pollPaymentStatus, 2000);
    
    // Initial poll
    pollPaymentStatus();

    return () => clearInterval(interval);
  }, [currentSagaId]);

  const handleRemoveFromCart = async (productId: number) => {
    const success = await removeFromCart(productId);
    if (success) {
      toast.success('Item removed from cart');
    } else {
      toast.error('Failed to remove item from cart');
    }
  };

  const handleIncreaseQuantity = async (item: CartItemResponse) => {
    const success = await updateQuantity(item.product.id, item.quantity + 1);
    if (!success) {
      toast.error('Failed to update quantity');
    }
  };

  const handleDecreaseQuantity = async (item: CartItemResponse) => {
    if (item.quantity <= 1) {
      await handleRemoveFromCart(item.product.id);
    } else {
      const success = await updateQuantity(item.product.id, item.quantity - 1);
      if (!success) {
        toast.error('Failed to update quantity');
      }
    }
  };

  const handleCheckout = async () => {
    if (!cart?.cartItems.length || isProcessing) {
      return;
    }

    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    try {
      setIsProcessing(true);

      const response = await orderApi.checkout({ 
        paymentMethod: selectedPaymentMethod as PaymentMethod
      });

      if (response.success && response.data) {
        setCurrentSagaId(response.data.sagaId.toString());
        
        // Clear cart via API
        await clearCart();
        
        toast.success("Order placed successfully! Preparing payment...");
      } else {
        toast.error(response.errorMessage || "Failed to place order");
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : "Unknown error";
      toast.error("Failed to place order: " + errorMessage);
    } finally {
      setIsProcessing(false);
    }
  };

  const handlePaymentResult = (result: PaymentResult) => {
    if (result.success) {
      setShowPaymentForm(false);
      setPaymentStatusModal({
        isOpen: true,
        isSuccess: true,
        message: 'Payment completed successfully! Your order is being processed.',
      });
    } else {
      setPaymentStatusModal({
        isOpen: true,
        isSuccess: false,
        message: result.error?.message || 'Payment failed. Please try again.',
      });
    }
  };

  const handleCancelPayment = () => {
    setShowPaymentForm(false);
    setPaymentNextAction(null);
    setCurrentSagaId(null);
  };

  const handleCloseStatusModal = () => {
    setPaymentStatusModal({
      isOpen: false,
      isSuccess: false,
      message: '',
    });
    
    // Redirect to payment success/failed page or home
    if (paymentStatusModal.isSuccess) {
      router.push('/payment-success');
    } else {
      router.push('/');
    }
  };

  return (
    <Card className="shopping-cart shadow-sm">
      <CardHeader>
        <CardTitle className="text-center">Your Cart</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto"></div>
            <p className="mt-2 text-gray-500">Loading cart...</p>
          </div>
        ) : error ? (
          <div className="text-center py-8">
            <p className="text-red-500">Error loading cart: {error}</p>
            <Button 
              onClick={() => window.location.reload()} 
              variant="outline" 
              className="mt-2"
            >
              Retry
            </Button>
          </div>
        ) : cart?.cartItems && cart.cartItems.length > 0 ? (
          <div className="space-y-3 mb-6">
            {cart.cartItems.map((item: CartItemResponse) => (
              <div
                key={item.product.id}
                className="border-b border-gray-200 last:border-b-0 py-3"
              >
                <div className="flex items-center gap-3">
                  {/* Remove button */}
                  <Button
                    onClick={() => handleRemoveFromCart(item.product.id)}
                    variant="ghost"
                    size="sm"
                    className="text-red-500 hover:text-red-700 p-1 h-auto"
                    disabled={isProcessing || isLoading}
                  >
                    <X className="h-4 w-4" />
                  </Button>

                  {/* Product image */}
                  <Image
                    src={item.product.image || "/placeholder-image.jpg"}
                    alt={item.product.name}
                    className="cart-item-image"
                    width={60}
                    height={60}
                    style={{
                      objectFit: "cover",
                      borderRadius: "4px",
                    }}
                  />

                  {/* Product name */}
                  <div className="flex-1">
                    <span className="font-semibold">{item.product.name}</span>
                  </div>

                  {/* Quantity controls */}
                  <div className="flex items-center gap-2">
                    <Button
                      onClick={() => handleDecreaseQuantity(item)}
                      variant="outline"
                      size="sm"
                      className="h-8 w-8 p-0"
                      disabled={isProcessing || isLoading}
                    >
                      <Minus className="h-3 w-3" />
                    </Button>
                    <Input
                      type="number"
                      value={item.quantity}
                      onChange={async (e: React.ChangeEvent<HTMLInputElement>) => {
                        const newQuantity = parseInt(e.target.value) || 1;
                        const success = await updateQuantity(item.product.id, newQuantity);
                        if (!success) {
                          toast.error('Failed to update quantity');
                        }
                      }}
                      className="w-12 h-8 text-center text-sm"
                      min="1"
                      disabled={isProcessing || isLoading}
                    />
                    <Button
                      onClick={() => handleIncreaseQuantity(item)}
                      variant="outline"
                      size="sm"
                      className="h-8 w-8 p-0"
                      disabled={isProcessing || isLoading}
                    >
                      <Plus className="h-3 w-3" />
                    </Button>
                  </div>

                  {/* Price */}
                  <span className="font-bold">
                    {formatCurrency(getItemTotal(item.product.id))}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-center text-gray-500 py-8">Your cart is empty.</p>
        )}

        {cart?.cartItems && cart.cartItems.length > 0 && (
          <>
            <div className="flex justify-between items-center pt-4 border-t border-gray-200">
              <span className="font-bold text-lg">Total:</span>
              <span className="font-bold text-lg">{formatCurrency(total)}</span>
            </div>

            {/* Payment Method Selection */}
            <div className="mt-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Payment Method
              </label>
              <Select value={selectedPaymentMethod} onValueChange={(value) => setSelectedPaymentMethod(value as PaymentMethod)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select payment method" />
                </SelectTrigger>
                <SelectContent>
                  {paymentMethods.map((method) => (
                    <SelectItem key={method} value={method}>
                      {method.charAt(0).toUpperCase() + method.slice(1).toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <Button
              onClick={handleCheckout}
              className="w-full mt-6"
              disabled={!cart?.cartItems.length || isProcessing || isLoading}
            >
              {isProcessing ? "Processing..." : "Place Order"}
            </Button>
          </>
        )}
      </CardContent>

      {/* Dynamic Payment Form Modal */}
      {showPaymentForm && paymentNextAction && PaymentFormComponent && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="max-w-md w-full">
            <PaymentFormComponent
              clientSecret={paymentNextAction.client_secret || ''}
              orderId={currentSagaId || ''}
              onPaymentResult={handlePaymentResult}
              onCancel={handleCancelPayment}
            />
          </div>
        </div>
      )}

      {/* Payment Status Modal */}
      {paymentStatusModal.isOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <div className="text-center">
              <div className={`mx-auto flex items-center justify-center h-12 w-12 rounded-full ${
                paymentStatusModal.isSuccess ? 'bg-green-100' : 'bg-red-100'
              }`}>
                {paymentStatusModal.isSuccess ? (
                  <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                ) : (
                  <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                )}
              </div>
              <div className="mt-3">
                <h3 className={`text-lg font-medium ${
                  paymentStatusModal.isSuccess ? 'text-green-900' : 'text-red-900'
                }`}>
                  {paymentStatusModal.isSuccess ? 'Payment Successful' : 'Payment Failed'}
                </h3>
                <div className="mt-2">
                  <p className="text-sm text-gray-500">
                    {paymentStatusModal.message}
                  </p>
                </div>
              </div>
            </div>
            <div className="mt-5">
              <Button
                onClick={handleCloseStatusModal}
                className="w-full"
              >
                Continue
              </Button>
            </div>
          </div>
        </div>
      )}
    </Card>
  );
};

export default Cart;
