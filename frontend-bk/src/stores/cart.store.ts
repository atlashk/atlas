import type { Product } from '@/interfaces/product.interface';
import { defineStore } from 'pinia';

export interface CartItem {
  product: Product;
  quantity: number;
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    cart: [] as CartItem[],
    currentOrderId: null as number | null,
  }),
  actions: {
    loadCart() {
      const savedCart = localStorage.getItem('cart');
      if (savedCart) {
        try {
          this.cart = JSON.parse(savedCart);
        } catch (error) {
          console.error('Failed to parse cart data:', error);
          // Reset cart if data is corrupted
          this.cart = [];
          localStorage.removeItem('cart');
        }
      }
    },
    saveCart() {
      try {
        // Create a deep copy of the cart to avoid any reactive issues
        const cartCopy = JSON.parse(JSON.stringify(this.cart));
        localStorage.setItem('cart', JSON.stringify(cartCopy));
      } catch (error) {
        console.error('Failed to save cart:', error);
      }
    },
    addToCart(product: Product) {
      if (this.cart.length === 0) {
        this.currentOrderId = null;
      }
      const existingItem = this.cart.find(item => item.product.id === product.id);
      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        this.cart.push({ product, quantity: 1 });
      }
      this.saveCart();
    },
    removeFromCart(productId: number) {
      this.cart = this.cart.filter(item => item.product.id !== productId);
      this.saveCart();
    },
    updateQuantity(productId: number, newQuantity: number) {
      const item = this.cart.find(item => item.product.id === productId);
      if (item) {
        item.quantity = newQuantity;
        // Create a new array to ensure reactivity
        this.cart = [...this.cart];
        this.saveCart();
      }
    },
    clearCart() {
      this.cart = [];
      localStorage.removeItem('cart');
    },
    getItemTotal(item: CartItem): number {
      return item ? item.product.price * item.quantity : 0;
    },    
    getTotal(): number {
      return this.cart
        .reduce((total, item) => total + item.product.price * item.quantity, 0);
    }
  }
});
