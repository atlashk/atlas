import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface CartItem {
  productId: string
  name: string
  price: number
  quantity: number
  imageUrl?: string
}

interface CartState {
  cart: CartItem[]
  currentOrderId: string | null
}

interface CartActions {
  loadCart: () => void
  saveCart: () => void
  addToCart: (item: Omit<CartItem, 'quantity'>, quantity?: number) => void
  removeFromCart: (productId: string) => void
  updateQuantity: (productId: string, quantity: number) => void
  clearCart: () => void
  getItemTotal: (productId: string) => number
  getTotal: () => number
  setCurrentOrderId: (orderId: string | null) => void
}

type CartStore = CartState & CartActions

export const useCartStore = create<CartStore>()(persist(
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

    addToCart: (item: Omit<CartItem, 'quantity'>, quantity = 1) => {
      const { cart } = get()
      const existingItemIndex = cart.findIndex(cartItem => cartItem.productId === item.productId)
      
      if (existingItemIndex >= 0) {
        // Update quantity of existing item
        const updatedCart = [...cart]
        updatedCart[existingItemIndex].quantity += quantity
        set({ cart: updatedCart })
      } else {
        // Add new item to cart
        const newItem: CartItem = {
          ...item,
          quantity
        }
        set({ cart: [...cart, newItem] })
      }
    },

    removeFromCart: (productId: string) => {
      const { cart } = get()
      const updatedCart = cart.filter(item => item.productId !== productId)
      set({ cart: updatedCart })
    },

    updateQuantity: (productId: string, quantity: number) => {
      const { cart } = get()
      if (quantity <= 0) {
        get().removeFromCart(productId)
        return
      }
      
      const updatedCart = cart.map(item => 
        item.productId === productId 
          ? { ...item, quantity }
          : item
      )
      set({ cart: updatedCart })
    },

    clearCart: () => {
      set({ cart: [], currentOrderId: null })
    },

    getItemTotal: (productId: string) => {
      const { cart } = get()
      const item = cart.find(cartItem => cartItem.productId === productId)
      return item ? item.price * item.quantity : 0
    },

    getTotal: () => {
      const { cart } = get()
      return cart.reduce((total, item) => total + (item.price * item.quantity), 0)
    },

    setCurrentOrderId: (orderId: string | null) => {
      set({ currentOrderId: orderId })
    }
  }),
  {
    name: 'cart-store'
  }
))
