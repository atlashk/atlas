import { orderApi } from "@/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { PaymentMethod, PlaceOrderItemRequest, OrderTrackingPayload } from "@/interfaces";
import { configStore } from "@/lib/config";
import { notificationService } from "@/services/notificationService";
import { PaymentFormProps, PaymentResult, paymentService } from "@/services/paymentService";
import { CartItem, useCartStore, useUserStore } from "@/stores";
import { formatCurrency } from "@/utils/formatter.util";
import { Minus, Plus, X } from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useEffect, useState } from "react";
import { toast } from "sonner";
import PaymentMethodSelector from "./PaymentMethodSelector";
import PaymentStatusModal from "./PaymentStatusModal";

const Cart: React.FC = () => {
  const [isProcessing, setIsProcessing] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod>(PaymentMethod.CARD);
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [clientSecret, setClientSecret] = useState<string>('');
  const [currentOrderId, setCurrentOrderIdState] = useState<number | null>(null);
  const [PaymentFormComponent, setPaymentFormComponent] = useState<React.ComponentType<PaymentFormProps> | null>(null);
  const [paymentStatusModal, setPaymentStatusModal] = useState({
    isOpen: false,
    isSuccess: false,
    message: '',
  });
  
  const {
    cart,
    getTotal,
    getItemTotal,
    removeFromCart,
    updateQuantity,
    clearCart,
    setCurrentOrderId,
  } = useCartStore();
  const { isAuthenticated } = useUserStore();
  const router = useRouter();

  const total = getTotal();

  // Initialize services and listen for order notifications
  useEffect(() => {
    const initializeServices = async () => {
      try {
        await paymentService.initialize();
      } catch (error) {
        console.error('Failed to initialize payment service:', error);
      }
    };

    initializeServices();
  }, []);

  // Listen for order tracking notifications using the notification service
  useEffect(() => {
    if (!currentOrderId) return;

    const handleOrderUpdate = async (payload: OrderTrackingPayload) => {
      try {
        if (payload.orderStatus === 'AWAITING_PAYMENT' && payload.paymentGatewayData?.clientSecret) {
          setClientSecret(payload.paymentGatewayData.clientSecret);
          
          // Get the appropriate payment form component based on configuration
          const config = configStore.getPaymentConfig();
          const PaymentForm = await paymentService.createPaymentForm(
            config.defaultGateway
          );
          setPaymentFormComponent(() => PaymentForm);
          setShowPaymentForm(true);
        } else if (payload.orderStatus === 'PAYMENT_SUCCEEDED') {
          setShowPaymentForm(false);
          setPaymentStatusModal({
            isOpen: true,
            isSuccess: true,
            message: 'Payment completed successfully! Your order is being processed.',
          });
        } else if (payload.orderStatus === 'PAYMENT_FAILED') {
          setShowPaymentForm(false);
          setPaymentStatusModal({
            isOpen: true,
            isSuccess: false,
            message: 'Payment failed. Please try again.',
          });
        }
      } catch (error) {
        console.error('Error handling order update:', error);
        toast.error('Failed to process order update');
      }
    };

    // Subscribe to order notifications using the notification service
    const subscription = notificationService.subscribeToOrder(currentOrderId, handleOrderUpdate);

    return () => {
      subscription.unsubscribe();
    };
  }, [currentOrderId]);

  const handleRemoveFromCart = (productId: number) => {
    removeFromCart(productId);
  };

  const handleIncreaseQuantity = (item: CartItem) => {
    updateQuantity(item.productId, item.quantity + 1);
  };

  const handleDecreaseQuantity = (item: CartItem) => {
    if (item.quantity <= 1) {
      removeFromCart(item.productId);
    } else {
      updateQuantity(item.productId, item.quantity - 1);
    }
  };

  const handlePlaceOrder = async () => {
    if (!cart.length || isProcessing) {
      return;
    }

    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    try {
      setIsProcessing(true);

      const orderItems: PlaceOrderItemRequest[] = cart.map(
        (cartItem: CartItem) => ({
          productId: cartItem.productId,
          quantity: cartItem.quantity,
        })
      );

      const response = await orderApi.placeOrder({ 
        orderItems,
        paymentMethod: selectedPaymentMethod 
      });

      if (response.success && response.data) {
        setCurrentOrderId(response.data.orderId);
        setCurrentOrderIdState(response.data.orderId);
        clearCart();
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
    setClientSecret('');
  };

  const handleCloseStatusModal = () => {
    setPaymentStatusModal({
      isOpen: false,
      isSuccess: false,
      message: '',
    });
  };

  return (
    <Card className="shopping-cart shadow-sm">
      <CardHeader>
        <CardTitle className="text-center">Your Cart</CardTitle>
      </CardHeader>
      <CardContent>
        {cart.length > 0 ? (
          <div className="space-y-3 mb-6">
            {cart.map((item: CartItem) => (
              <div
                key={item.productId}
                className="border-b border-gray-200 last:border-b-0 py-3"
              >
                <div className="flex items-center gap-3">
                  {/* Remove button */}
                  <Button
                    onClick={() => handleRemoveFromCart(item.productId)}
                    variant="ghost"
                    size="sm"
                    className="text-red-500 hover:text-red-700 p-1 h-auto"
                    disabled={isProcessing}
                  >
                    <X className="h-4 w-4" />
                  </Button>

                  {/* Product image */}
                  <Image
                    src={item.imageUrl || "/placeholder-image.jpg"}
                    alt={item.name}
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
                    <span className="font-semibold">{item.name}</span>
                  </div>

                  {/* Quantity controls */}
                  <div className="flex items-center gap-2">
                    <Button
                      onClick={() => handleDecreaseQuantity(item)}
                      variant="outline"
                      size="sm"
                      className="h-8 w-8 p-0"
                      disabled={isProcessing}
                    >
                      <Minus className="h-3 w-3" />
                    </Button>
                    <Input
                      type="number"
                      value={item.quantity}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                        updateQuantity(
                          item.productId,
                          parseInt(e.target.value) || 1
                        )
                      }
                      className="w-12 h-8 text-center text-sm"
                      min="1"
                      disabled={isProcessing}
                    />
                    <Button
                      onClick={() => handleIncreaseQuantity(item)}
                      variant="outline"
                      size="sm"
                      className="h-8 w-8 p-0"
                      disabled={isProcessing}
                    >
                      <Plus className="h-3 w-3" />
                    </Button>
                  </div>

                  {/* Price */}
                  <span className="font-bold">
                    ${formatCurrency(getItemTotal(item.productId))}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-center text-gray-500 py-8">Your cart is empty.</p>
        )}

        {cart.length > 0 && (
          <>
            <div className="flex justify-between items-center pt-4 border-t border-gray-200">
              <span className="font-bold text-lg">Total:</span>
              <span className="font-bold text-lg">${formatCurrency(total)}</span>
            </div>

            {/* Payment Method Selection */}
            <div className="mt-6">
              <PaymentMethodSelector
                selectedMethod={selectedPaymentMethod}
                onMethodChange={setSelectedPaymentMethod}
              />
            </div>

            <Button
              onClick={handlePlaceOrder}
              className="w-full mt-6"
              disabled={!cart.length || isProcessing}
            >
              {isProcessing ? "Processing..." : "Place Order"}
            </Button>
          </>
        )}
      </CardContent>

      {/* Dynamic Payment Form Modal */}
      {showPaymentForm && clientSecret && PaymentFormComponent && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="max-w-md w-full">
            <PaymentFormComponent
              clientSecret={clientSecret}
              orderId={currentOrderId?.toString() || ''}
              onPaymentResult={handlePaymentResult}
              onCancel={handleCancelPayment}
            />
          </div>
        </div>
      )}

      {/* Payment Status Modal */}
      <PaymentStatusModal
        isOpen={paymentStatusModal.isOpen}
        isSuccess={paymentStatusModal.isSuccess}
        message={paymentStatusModal.message}
        onClose={handleCloseStatusModal}
      />
    </Card>
  );
};

export default Cart;
