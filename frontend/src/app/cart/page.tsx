"use client";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Spinner } from "@/components/ui/spinner";
import { CartItemResponse } from "@/interfaces/cart.interface";
import { useCartStore } from "@/stores/cart.store";
import { withAuth } from '@/hoc/withAuth';
import { formatCurrency } from "@/utils/formatter.util";
import { ArrowLeft, CreditCard, Minus, Plus, ShoppingCart, Trash2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

function CartPage() {
  const router = useRouter();
  const { cart, loadCart, updateQuantity, removeFromCart, clearCart, isLoading: cartLoading, getCartTotal } = useCartStore();
  
  const [updating, setUpdating] = useState(false);
  const [checkingOut, setCheckingOut] = useState(false);

  // Load cart data on component mount
  useEffect(() => {
    if (!cart && !cartLoading) {
      loadCart();
    }
  }, [cart, cartLoading]);

  const handleUpdateQuantity = async (productId: number, newQuantity: number) => {
    try {
      setUpdating(true);
      await updateQuantity(productId, newQuantity);
    } catch (error) {
      console.error("Failed to update quantity:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleRemoveItem = async (productId: number) => {
    try {
      setUpdating(true);
      await removeFromCart(productId);
    } catch (error) {
      console.error("Failed to remove item:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleClearCart = async () => {
    try {
      setUpdating(true);
      await clearCart();
    } catch (error) {
      console.error("Failed to clear cart:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleCheckout = async () => {
    try {
      setCheckingOut(true);
      
      // Redirect to checkout page
      router.push("/checkout");
    } catch (error) {
      console.error("Checkout failed:", error);
      alert("Checkout failed. Please try again.");
    } finally {
      setCheckingOut(false);
    }
  };

  if (cartLoading) {
    return (
      <div className="container mx-auto px-4 py-8 pt-20">
        <div className="flex items-center justify-center min-h-[400px]">
          <Spinner className="text-blue-600" />
        </div>
      </div>
    );
  }

  const cartItems = cart?.cartItems || [];
  const totalAmount = getCartTotal();
  const isEmpty = cartItems.length === 0;

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        {/* Header */}
        <div className="flex items-center gap-3 mb-8">
          <div className="p-2 bg-primary/10 rounded-lg">
            <ShoppingCart className="h-6 w-6 text-primary" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">
            Shopping Cart
          </h1>
        </div>

        {isEmpty ? (
          <Card className="shadow-sm border-0 bg-white">
            <CardContent className="flex flex-col items-center justify-center py-8">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                Your cart is empty
              </h2>
              <Button onClick={() => router.push("/")} size="lg" className="px-8">
                <ArrowLeft className="h-4 w-4" />
                Continue Shopping
              </Button>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-6">
            {/* Cart Items */}
            <Card className="shadow-sm border-0 bg-white">
              <CardHeader className="flex flex-row items-center justify-between pb-4">
                <CardTitle className="text-lg font-semibold text-gray-900">
                  Items ({cartItems.length})
                </CardTitle>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleClearCart}
                  disabled={updating}
                  className="text-red-600 hover:text-red-700 border-red-200 hover:bg-red-50"
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  Clear all
                </Button>
              </CardHeader>
              <CardContent className="space-y-3 pt-0">
                {cartItems.map((item: CartItemResponse) => (
                  <div key={item.product.id} className="flex items-center gap-4 p-4 bg-gray-50 rounded-xl border border-gray-100 hover:shadow-sm transition-shadow">
                    <div className="flex-1 min-w-0">
                      <h3 className="font-semibold text-gray-900 truncate">{item.product.name}</h3>
                      <div className="flex items-center gap-2 mt-2">
                        <Badge variant="secondary" className="bg-blue-100 text-blue-800 hover:bg-blue-100">
                          {formatCurrency(item.product.price)}
                        </Badge>
                        {item.product.categories && item.product.categories.length > 0 && (
                          <Badge variant="outline" className="border-gray-300 text-gray-600">
                            {item.product.categories[0].name}
                          </Badge>
                        )}
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-1 bg-white rounded-lg border border-gray-200 p-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleUpdateQuantity(item.product.id, item.quantity - 1)}
                        disabled={updating}
                        className="h-8 w-8 p-0 hover:bg-gray-100"
                      >
                        <Minus className="h-3 w-3" />
                      </Button>
                      <span className="w-10 text-center font-semibold text-sm">
                        {item.quantity}
                      </span>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleUpdateQuantity(item.product.id, item.quantity + 1)}
                        disabled={updating}
                        className="h-8 w-8 p-0 hover:bg-gray-100"
                      >
                        <Plus className="h-3 w-3" />
                      </Button>
                    </div>
                    
                    <div className="flex items-center gap-3">
                      <p className="font-bold text-lg text-gray-900 min-w-0">
                        {formatCurrency(item.product.price * item.quantity)}
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleRemoveItem(item.product.id)}
                        disabled={updating}
                        className="text-red-600 hover:text-red-700 hover:bg-red-50 h-8 w-8 p-0"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>

             {/* Total and Checkout */}
             <Card className="shadow-sm border-0 bg-white sticky bottom-4">
               <CardContent className="p-6">
                 <div className="flex items-center justify-between mb-6">
                   <span className="text-xl font-semibold text-gray-700">Total:</span>
                   <span className="text-xl font-bold text-gray-900">{formatCurrency(totalAmount)}</span>
                 </div>
                 <div className="flex gap-3">
                   <Button
                     onClick={() => router.push("/")}
                     variant="outline"
                     className="flex-1"
                   >
                     <ArrowLeft className="h-5 w-5 mr-2" />
                     Continue Shopping
                   </Button>
                   <Button
                     onClick={handleCheckout}
                     disabled={checkingOut}
                     className="flex-1"
                   >
                     {checkingOut ? (
                       <div className="flex items-center gap-2">
                         <Spinner className="text-blue-600" />
                         Processing...
                       </div>
                     ) : (
                       <>
                         <CreditCard className="h-5 w-5 mr-2" />
                         Proceed to Checkout
                       </>
                     )}
                   </Button>
                 </div>
               </CardContent>
             </Card>
          </div>
        )}
      </div>
    </div>
  );
}

// Export with authentication HOC that requires USER role
export default withAuth(CartPage, { requireAuth: true, allowedRoles: ['USER'] });
