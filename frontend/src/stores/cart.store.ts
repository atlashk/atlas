import { create } from "zustand";
import { cartApi } from "@/api/index.api";
import type { CartResponse } from "@/interfaces/cart.interface";
import { useUserStore } from "./user.store";

interface CartState {
  cart: CartResponse | null;
  isLoading: boolean;
  error: string | null;
  isIntentionallyCleared: boolean;
}

interface CartActions {
  // Core cart operations
  loadCart: () => Promise<void>;
  addToCart: (productId: number, quantity?: number) => Promise<boolean>;
  removeFromCart: (productId: number) => Promise<boolean>;
  updateQuantity: (productId: number, quantity: number) => Promise<boolean>;
  clearCart: () => Promise<boolean>;
  
  // Computed values
  getCartItemCount: () => number;
  getCartTotal: () => number;
  getItemTotal: (productId: number) => number;
  
  // Internal state management
  setCart: (cart: CartResponse | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  clearCartState: () => void;
  resetIntentionallyCleared: () => void;
}

type CartStore = CartState & CartActions;

const isUserAuthenticated = () => {
  const { isAuthenticated } = useUserStore.getState();
  return isAuthenticated();
};

export const useCartStore = create<CartStore>()((set, get) => ({
  // Initial state
  cart: null,
  isLoading: false,
  error: null,
  isIntentionallyCleared: false,

  // Core cart operations
  loadCart: async () => {
    if (!isUserAuthenticated()) {
      set({ cart: null, error: null });
      return;
    }

    // Don't load if cart was intentionally cleared (e.g., after payment)
    const { isIntentionallyCleared } = get();
    if (isIntentionallyCleared) {
      return;
    }

    // Prevent duplicate API calls
    const { isLoading } = get();
    if (isLoading) {
      return;
    }

    try {
      set({ isLoading: true, error: null });
      const response = await cartApi.getCart();
      
      if (response.success && response.data) {
        set({ cart: response.data, error: null, isIntentionallyCleared: false });
      } else {
        // Set error but don't clear cart if we had one before
        const errorMessage = response.errorMessage || "Failed to load cart";
        set({ error: errorMessage });
        console.warn('Failed to load cart:', errorMessage);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Unknown error";
      set({ error: errorMessage });
      console.error('Cart API error:', errorMessage);
    } finally {
      set({ isLoading: false });
    }
  },

  addToCart: async (productId: number, quantity = 1) => {
    if (!isUserAuthenticated()) {
      set({ error: "Please login to add items to cart" });
      return false;
    }

    try {
      set({ isLoading: true, error: null });
      const response = await cartApi.addCartItem({ productId, quantity });
      
      if (response.success && response.data) {
        set({ cart: response.data, isIntentionallyCleared: false });
        return true;
      } else {
        set({ error: response.errorMessage || "Failed to add item to cart" });
        return false;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Unknown error";
      set({ error: errorMessage });
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  removeFromCart: async (productId: number) => {
    if (!isUserAuthenticated()) {
      set({ error: "Please login to manage cart items" });
      return false;
    }

    try {
      set({ isLoading: true, error: null });
      const response = await cartApi.removeCartItem(productId);
      
      if (response.success && response.data) {
        set({ cart: response.data, isIntentionallyCleared: false });
        return true;
      } else {
        set({ error: response.errorMessage || "Failed to remove item from cart" });
        return false;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Unknown error";
      set({ error: errorMessage });
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  updateQuantity: async (productId: number, newQuantity: number) => {
    if (!isUserAuthenticated()) {
      set({ error: "Please login to manage cart items" });
      return false;
    }

    // If new quantity is <= 0, remove the item
    if (newQuantity <= 0) {
      return await get().removeFromCart(productId);
    }

    try {
      set({ isLoading: true, error: null });
      // Send final quantity to API
      const response = await cartApi.updateCartItem(productId, { quantity: newQuantity });
      
      if (response.success && response.data) {
        set({ cart: response.data, isIntentionallyCleared: false });
        return true;
      } else {
        set({ error: response.errorMessage || "Failed to update item quantity" });
        return false;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Unknown error";
      set({ error: errorMessage });
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  clearCart: async () => {
    if (!isUserAuthenticated()) {
      set({ error: "Please login to manage cart" });
      return false;
    }

    try {
      set({ isLoading: true, error: null });
      const response = await cartApi.clearCart();
      
      if (response.success && response.data) {
        set({ cart: response.data, isIntentionallyCleared: false });
        return true;
      } else {
        set({ error: response.errorMessage || "Failed to clear cart" });
        return false;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Unknown error";
      set({ error: errorMessage });
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  // Computed values
  getCartItemCount: () => {
    const { cart } = get();
    // Count distinct products (cart items), not total quantity
    if (!cart || !cart.cartItems) return 0;
    return cart.cartItems.length;
  },

  getCartTotal: () => {
    const { cart } = get();
    if (!cart) return 0;
    return cart.totalAmount;
  },

  getItemTotal: (productId: number) => {
    const { cart } = get();
    if (!cart) return 0;
    const item = cart.cartItems.find((cartItem) => cartItem.product.id === productId);
    return item ? item.product.price * item.quantity : 0;
  },

  // Internal state management
  setCart: (cart: CartResponse | null) => {
    set({ cart });
  },

  setLoading: (loading: boolean) => {
    set({ isLoading: loading });
  },

  setError: (error: string | null) => {
    set({ error });
  },

  clearError: () => {
    set({ error: null });
  },

  // Clear cart state locally (for logout)
  clearCartState: () => {
    set({ cart: null, error: null, isLoading: false, isIntentionallyCleared: true });
  },

  resetIntentionallyCleared: () => {
    set({ isIntentionallyCleared: false });
  },
}));
