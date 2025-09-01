import { orderApi } from "@/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { PlaceOrderItemRequest } from "@/interfaces";
import { CartItem, useCartStore, useUserStore } from "@/stores";
import { formatCurrency } from "@/utils/formatter.util";
import { Minus, Plus, X } from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useState } from "react";
import { toast } from "sonner";

const Cart: React.FC = () => {
  const [isProcessing, setIsProcessing] = useState(false);
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

      const response = await orderApi.placeOrder({ orderItems });

      if (response.success && response.data) {
        setCurrentOrderId(response.data.orderId);
        clearCart();
        toast.success("Order placed successfully!");
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
                      onChange={(e) =>
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
          <div className="flex justify-between items-center pt-4 border-t border-gray-200">
            <span className="font-bold text-lg">Total:</span>
            <span className="font-bold text-lg">${formatCurrency(total)}</span>
          </div>
        )}

        <Button
          onClick={handlePlaceOrder}
          className="w-full mt-6"
          disabled={!cart.length || isProcessing}
        >
          {isProcessing ? "Processing..." : "Place Order"}
        </Button>
      </CardContent>
    </Card>
  );
};

export default Cart;
