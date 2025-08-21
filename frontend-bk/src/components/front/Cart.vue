<template>
  <div class="shopping-cart p-4 border bg-light shadow-sm rounded">
    <h5 class="mb-4 text-center">Your Cart</h5>

    <ul v-if="cart.length" class="list-group mb-3">
      <li
        v-for="item in cart"
        :key="item.product.id"
        class="list-group-item border-0 border-bottom py-3"
      >
        <div class="d-flex align-items-center gap-3">
          <!-- Remove button -->
          <button
            @click="removeFromCart(item.product.id)"
            class="btn btn-link text-danger p-0 fs-5"
            :disabled="isProcessing"
          >
            <i class="bi bi-x"></i>
          </button>

          <!-- Product image -->
          <img
            :src="getProductImageUrl(item.product.image)"
            :alt="item.product.name"
            class="cart-item-image"
          />

          <!-- Product name -->
          <div class="flex-grow-1">
            <strong>{{ item.product.name }}</strong>
          </div>

          <!-- Quantity controls -->
          <div class="d-flex align-items-center gap-2">
            <button
              @click="decreaseQuantity(item)"
              class="btn btn-sm btn-outline-secondary"
              :disabled="isProcessing"
            >
              -
            </button>
            <input
              type="number"
              v-model.number="item.quantity"
              class="form-control form-control-sm text-center"
              style="width: 40px"
              min="1"
              @change="updateQuantity(item.product.id, item.quantity)"
              :disabled="isProcessing"
            />
            <button
              @click="increaseQuantity(item)"
              class="btn btn-sm btn-outline-secondary"
              :disabled="isProcessing"
            >
              +
            </button>
          </div>

          <!-- Price -->
          <span class="fw-bold">${{ formatCurrency(cartStore.getItemTotal(item)) }}</span>
        </div>
      </li>
    </ul>

    <p v-else class="text-center text-muted">Your cart is empty.</p>

    <div v-if="cart.length" class="d-flex justify-content-between mt-3">
      <span class="fw-bold">Total:</span>
      <span class="fw-bold">${{ formatCurrency(total) }}</span>
    </div>

    <button
      @click="handlePlaceOrder"
      class="btn btn-success w-100 mt-4"
      :disabled="!cart.length || isProcessing"
    >
      {{ isProcessing ? "Processing..." : "Place Order" }}
    </button>
  </div>
</template>

<script setup lang="ts">
import type { PlaceOrderItemRequest } from '@/interfaces/order.interface';
import { orderService } from '@/services';
import { useCartStore, type CartItem } from '@/stores/cart.store';
import { useUserStore } from '@/stores/user.store';
import { formatCurrency } from '@/utils/formatter.util';
import { getProductImageUrl } from '@/utils/productImage.util';
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

const isProcessing = ref(false);
const cartStore = useCartStore();
const userStore = useUserStore();
const router = useRouter();

const cart = computed(() => cartStore.cart);
const total = computed(() => cartStore.getTotal());

function removeFromCart(productId: number) {
  cartStore.removeFromCart(productId);
}

function increaseQuantity(item: CartItem) {
  cartStore.updateQuantity(item.product.id, item.quantity + 1);
}

function decreaseQuantity(item: CartItem) {
  if (item.quantity <= 1) {
    cartStore.removeFromCart(item.product.id);
  } else {
    cartStore.updateQuantity(item.product.id, item.quantity - 1);
  }
}

function updateQuantity(productId: number, quantity: number) {
  cartStore.updateQuantity(productId, quantity > 0 ? quantity : 1);
}

async function handlePlaceOrder() {
  if (!cart.value.length || isProcessing.value) return;

  if (!userStore.isAuthenticated) {
    router.push({ name: 'login' });
    return
  }

  try {
    isProcessing.value = true;

    const orderItems = cart.value.map((cartItem) => ({
      productId: cartItem.product.id,
      quantity: cartItem.quantity,
    } as PlaceOrderItemRequest));

    const response = await orderService.placeOrder({orderItems});
    
    if (response.success && response.data) {
      cartStore.currentOrderId = response.data;
      cartStore.clearCart();
    } else {
      console.error('Failed to place order:', response.errorMessage);
    }
  } catch (err: any) {
    console.error('Failed to place order:', err?.message || err);
  } finally {
    isProcessing.value = false;
  }
}
</script>

<style scoped>
.shopping-cart {
  background-color: #f8f9fa;
  border-radius: 5px;
}

.cart-item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

input[type='number']::-webkit-inner-spin-button,
input[type='number']::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>
