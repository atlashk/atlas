import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { CartItemResponse } from "@/interfaces/cart.interface";
import { useCartStore, useUserStore } from "@/stores";
import { formatCurrency } from "@/utils/formatter.util";
import { Minus, Plus, X } from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useEffect, useState } from "react";
import { toast } from "sonner";

const Cart: React.FC = () => {
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const {
    cart,
    loadCart,
    getCartTotal,
    getItemTotal,
    removeFromCart,
    updateQuantity,
    isLoading,
    error,
  } = useCartStore();
  const { isAuthenticated } = useUserStore();
  const router = useRouter();

  const total = getCartTotal();

  // Load cart data on component mount
  useEffect(() => {
    const loadData = async () => {
      if (!isAuthenticated()) return;

      try {
        await loadCart();
      } catch (error) {
        console.error('Failed to load cart data:', error);
        toast.error('Failed to load cart data');
      }
    };

    loadData();
  }, [isAuthenticated, loadCart]);



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
    if (!cart?.cartItems.length || isCheckingOut) {
      return;
    }

    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    try {
      setIsCheckingOut(true);
      
      // Redirect to checkout page
      router.push("/checkout");
    } catch (error) {
      console.error("Checkout failed:", error);
      toast.error("Failed to proceed to checkout. Please try again.");
    } finally {
      setIsCheckingOut(false);
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
                    disabled={isCheckingOut || isLoading}
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
                      disabled={isCheckingOut || isLoading}
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
                      disabled={isCheckingOut || isLoading}
                    />
                    <Button
                      onClick={() => handleIncreaseQuantity(item)}
                      variant="outline"
                      size="sm"
                      className="h-8 w-8 p-0"
                      disabled={isCheckingOut || isLoading}
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

            <Button
              onClick={handleCheckout}
              className="w-full mt-6"
              disabled={!cart?.cartItems.length || isCheckingOut || isLoading}
            >
              {isCheckingOut ? "Redirecting..." : "Proceed to Checkout"}
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default Cart;
