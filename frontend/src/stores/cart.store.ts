import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  imageUrl?: string;
}

interface CartState {
  cart: CartItem[];
  currentOrderId: number | null;
}

interface CartActions {
  loadCart: () => void;
  saveCart: () => void;
  addToCart: (item: Omit<CartItem, "quantity">, quantity?: number) => void;
  removeFromCart: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  getItemTotal: (productId: number) => number;
  getTotal: () => number;
  setCurrentOrderId: (orderId: number | null) => void;
}

type CartStore = CartState & CartActions;

export const useCartStore = create<CartStore>()(
  persist(
    (set, get) => ({
      // Initial state
      cart: [],
      currentOrderId: null,

      // Actions
      loadCart: () => {
        // Cart is automatically loaded from localStorage via persist middleware
        // This method is kept for compatibility but doesn't need implementation
      },

      saveCart: () => {
        // Cart is automatically saved to localStorage via persist middleware
        // This method is kept for compatibility but doesn't need implementation
      },

      addToCart: (item: Omit<CartItem, "quantity">, quantity = 1) => {
        const { cart } = get();
        const existingItemIndex = cart.findIndex(
          (cartItem) => cartItem.productId === item.productId
        );

        if (existingItemIndex >= 0) {
          // Update quantity of existing item
          const updatedCart = [...cart];
          updatedCart[existingItemIndex].quantity += quantity;
          set({ cart: updatedCart });
        } else {
          // Add new item to cart
          const newItem: CartItem = {
            ...item,
            quantity,
          };
          set({ cart: [...cart, newItem] });
        }
      },

      removeFromCart: (productId: number) => {
        const { cart } = get();
        const updatedCart = cart.filter((item) => item.productId !== productId);
        set({ cart: updatedCart });
      },

      updateQuantity: (productId: number, quantity: number) => {
        const { cart } = get();
        if (quantity <= 0) {
          get().removeFromCart(productId);
          return;
        }

        const updatedCart = cart.map((item) =>
          item.productId === productId ? { ...item, quantity } : item
        );
        set({ cart: updatedCart });
      },

      clearCart: () => {
        set({ cart: [] });
      },

      getItemTotal: (productId: number) => {
        const { cart } = get();
        const item = cart.find((cartItem) => cartItem.productId === productId);
        return item ? item.price * item.quantity : 0;
      },

      getTotal: () => {
        const { cart } = get();
        return cart.reduce(
          (total, item) => total + item.price * item.quantity,
          0
        );
      },

      setCurrentOrderId: (orderId: number | null) => {
        set({ currentOrderId: orderId });
      },
    }),
    {
      name: "cart-store",
    }
  )
);
