<template>
 <div class="container-fluid px-5 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-1">Product Information</h3>
        <p class="text-muted mb-0">View product details</p>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-outline-primary" @click="router.push({ name: 'adminProductEdit', params: { id: product?.id } })">
          <i class="bi bi-pencil"></i> Edit
        </button>
        <button class="btn btn-outline-danger" @click="handleDelete">
          <i class="bi bi-trash"></i> Delete
        </button>
        <button class="btn btn-outline-secondary" @click="router.back()">
          <i class="bi bi-arrow-left"></i> Back
        </button>
      </div>
    </div>

    <div v-if="isLoadingProduct" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="product" class="row g-4">
      <!-- Product Basic Information -->
      <div class="col-md-4">
        <div class="card shadow-sm">
          <img :src="getProductImageUrl(product.image)" :alt="product.name" class="card-img-top product-image">
          <div class="card-body p-4">
            <h5 class="card-title h4 mb-3">{{ product.name }}</h5>
            <p class="card-text mb-4">
              <span class="fs-4 fw-bold text-primary">${{ product.price.toFixed(2) }}</span>
            </p>
            <div class="d-flex align-items-center gap-2 mb-3">
              <span :class="{
                'badge': true,
                'bg-success': product.status === 'IN_STOCK',
                'bg-danger': product.status === 'OUT_STOCK',
                'bg-secondary': product.status === 'DISCONTINUED'
              }">
                {{ product.status }}
              </span>
              <span v-if="product.status === 'IN_STOCK'" class="badge bg-info">Stock: {{ product.quantity }}</span>
            </div>
            <p class="mb-3"><strong>Brand:</strong> {{ product.brand.name }}</p>
            <p class="mb-0">
              <strong>Categories:</strong>
              <span v-for="category in product.categories" :key="category.id" class="badge bg-secondary ms-1">
                {{ category.name }}
              </span>
            </p>
          </div>
        </div>
      </div>

      <!-- Product Other Information -->
      <div class="col-md-8">
        <!-- Details -->
        <div class="card shadow-sm mb-4">
          <div class="card-header bg-light py-3">
            <h5 class="mb-0">Description</h5>
          </div>
          <div class="card-body p-4">
            <p class="card-text">{{ product.details.description }}</p>
          </div>
        </div>

        <!-- Attributes -->
        <div class="card shadow-sm">
          <div class="card-header bg-light py-3">
            <h5 class="mb-0">Specifications</h5>
          </div>
          <div class="card-body p-4">
            <div class="table-responsive">
              <table class="table table-bordered mb-0">
                <tbody>
                  <tr v-for="attr in product.attributes" :key="attr.id">
                    <th class="bg-light" style="width: 200px">{{ attr.name }}</th>
                    <td>{{ attr.value }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="alert alert-danger">
      Product not found
    </div>
  </div>
</template>

<script setup lang="ts">
import { productService } from '@/services';
import { useFlashStore } from '@/stores/flash.store';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import { getProductImageUrl } from '../../utils/productImage.util';

const router = useRouter();
const route = useRoute();
const flashStore = useFlashStore();

const isLoadingProduct = ref(true);
const product = ref<any>(null);

const handleDelete = async () => {
  if (!product.value) return;

  if (confirm('Are you sure you want to delete this product?')) {
    const response = await productService.deleteProduct(product.value.id);
    if (response.success) {
      flashStore.setSuccess('Deleted product successfully!');
      router.push({ name: 'adminProductList' });
    } else {
      toast.error(response.errorMessage || 'Failed to delete product');
    }
  }
};

onMounted(async () => {
  try {
    const productId = route.params.id as string;
    const response = await productService.getProductAdmin(parseInt(productId));
    if (response.success) {
      product.value = response.data;
    } else {
      flashStore.setError(response.errorMessage || 'Failed to load product');
      router.push({ name: 'adminProductList' });
    }
  } finally {
    isLoadingProduct.value = false;
  }
});
</script>

<style scoped>
.product-image {
  object-fit: cover;
  height: 300px;
  width: 100%;
}
</style>