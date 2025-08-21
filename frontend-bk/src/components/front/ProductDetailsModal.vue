<template>
  <div v-if="modelValue" class="modal-backdrop show" @click="closeModal"></div>
  <div v-if="modelValue" class="modal show d-block" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Product Details</h5>
          <button type="button" class="btn-close" @click="closeModal"></button>
        </div>
        <div class="modal-body">
          <div v-if="isLoading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
          <div v-else-if="product" class="row">
            <div class="col-md-6">
              <img :src="getProductImageUrl(product.image)" class="img-fluid rounded product-detail-image"
                :alt="product.name" />
            </div>
            <div class="col-md-6">
              <h4 class="mb-3">{{ product.name }}</h4>
              <h5 class="text-primary mb-4">${{ product.price.toFixed(2) }}</h5>

              <div class="mb-3">
                <h6>Description</h6>
                <p class="text-muted">{{ product.details.description || 'No description available.' }}</p>
              </div>

              <div v-if="product.attributes?.length" class="mb-1">
                <h6>Attributes</h6>
                <div class="table-responsive">
                  <table class="table table-bordered table-sm">
                    <tbody>
                      <tr v-for="attribute in product.attributes" :key="attribute.id">
                        <th class="text-muted">{{ attribute.name }}</th>
                        <td>{{ attribute.value }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <p v-else class="text-muted">No attributes available.</p>

              <div class="mb-4">
                <h6>Brand</h6>
                <p>{{ product.brand?.name || 'N/A' }}</p>
              </div>

              <div class="mb-4">
                <h6>Categories</h6>
                <div v-if="product.categories?.length" class="d-flex flex-wrap gap-2">
                  <span v-for="category in product.categories" :key="category.id" class="badge bg-secondary">
                    {{ category.name }}
                  </span>
                </div>
                <p v-else class="text-muted">N/A</p>
              </div>

              <div class="d-grid">
                <button @click="handleAddToCart" class="btn btn-primary">
                  Add to Cart
                </button>
              </div>
            </div>
          </div>
          <div v-else class="text-center text-muted py-5">
            No product details available.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useCartStore } from '@/stores/cart.store'
import { defineEmits, defineProps, withDefaults } from 'vue'
import { toast } from 'vue3-toastify'
import type { Product } from '../../interfaces/product.interface'
import { getProductImageUrl } from '../../utils/productImage.util'

// Initialize cart store
const cartStore = useCartStore()

// Define props with TypeScript
const props = withDefaults(defineProps<{
  modelValue: boolean
  product?: Product | null
  isLoading?: boolean
}>(), {
  product: null,
  isLoading: false
})

// Define emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const closeModal = () => {
  emit('update:modelValue', false)
}

const handleAddToCart = () => {
  if (props.product) {
    try {
      cartStore.addToCart(props.product)
      toast.success(`${props.product.name} added to cart`)
      closeModal()
    } catch (error) {
      toast.error('Failed to add product to cart')
      console.error(error)
    }
  }
}
</script>

<style scoped>
.product-detail-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  object-position: center;
}

.modal-backdrop {
  opacity: 0.5;
}
</style>
