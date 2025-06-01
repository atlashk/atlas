<template>
  <div class="search-product p-4 border bg-light shadow-sm rounded mb-3">
    <h5 class="mb-4 text-center">Store Products</h5>

    <!-- Filters Form -->
    <form @submit.prevent="applyFilters(1)" class="bg-white p-4 rounded-3 mb-4 border">
      <div class="row g-3">
        <!-- Search Input -->
        <div class="col-md-12">
          <label for="keyword" class="form-label">Search Products</label>
          <input
            id="keyword"
            v-model.trim="filters.keyword"
            type="text"
            class="form-control"
            placeholder="Search products..."
          />
        </div>

        <!-- Price Range -->
        <div class="col-md-12">
          <label class="form-label">Price Range</label>
          <div class="row g-3">
            <div class="col-md-6">
              <input
                v-model.number="filters.minPrice"
                type="number"
                class="form-control"
                placeholder="Min Price"
                min="0"
                step="0.01"
              />
            </div>
            <div class="col-md-6">
              <input
                v-model.number="filters.maxPrice"
                type="number"
                class="form-control"
                placeholder="Max Price"
                min="0"
                step="0.01"
              />
            </div>
          </div>
        </div>

        <!-- Brand and Category Filters -->
        <div class="col-md-12">
          <div class="row g-3">
            <div class="col-md-6">
              <label for="brandId" class="form-label">Brand</label>
              <div v-if="isLoadingBrands" class="text-center py-2" aria-live="polite">
                <div class="spinner-border spinner-border-sm" role="status" aria-label="Loading brands">
                  <span class="visually-hidden">Loading...</span>
                </div>
              </div>
              <select
                v-else
                id="brandId"
                v-model="filters.brandId"
                class="form-select"
                :disabled="!brands.length"
              >
                <option value="">All Brands</option>
                <option v-for="brand in brands" :key="brand.id" :value="brand.id">
                  {{ brand.name }}
                </option>
              </select>
              <div v-if="!brands.length && !isLoadingBrands" class="text-muted small mt-1">
                No brands available
              </div>
            </div>
            <div class="col-md-6">
              <label for="categoryIds" class="form-label">Categories</label>
              <div v-if="isLoadingCategories" class="text-center py-2" aria-live="polite">
                <div class="spinner-border spinner-border-sm" role="status" aria-label="Loading categories">
                  <span class="visually-hidden">Loading...</span>
                </div>
              </div>
              <select
                v-else
                id="categoryIds"
                v-model="filters.categoryIds"
                class="form-select"
                multiple
                :disabled="!categories.length"
                size="3"
              >
                <option v-for="category in categories" :key="category.id" :value="category.id">
                  {{ category.name }}
                </option>
              </select>
              <div v-if="!categories.length && !isLoadingCategories" class="text-muted small mt-1">
                No categories available
              </div>
              <small v-if="!isLoadingCategories" class="text-muted">Hold Ctrl/Cmd to select multiple</small>
            </div>
          </div>
        </div>

        <!-- Submit Button -->
        <div class="col-md-12 mt-3">
          <button type="submit" class="btn btn-primary">
            <i class="bi bi-search"></i> Search
          </button>
          <button type="button" @click="resetFilters" class="btn btn-outline-secondary ms-2">
            <i class="bi bi-arrow-counterclockwise"></i> Reset
          </button>
        </div>
      </div>
    </form>

    <!-- Products Grid -->
    <div v-if="isLoadingProducts" class="text-center py-5" aria-live="polite">
      <div class="spinner-border" role="status" aria-label="Loading products">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    <div v-else-if="products && products.length" class="mt-4 row g-3">
      <div v-for="product in products" :key="product.id" class="col-md-4">
        <div class="card h-100 shadow-sm">
          <img
            :src="getProductImageUrl(product.image)"
            class="card-img-top product-image"
            :alt="product.name"
          />
          <div class="card-body d-flex flex-column">
            <h5 class="card-title product-name" @click="showProductDetails(product)">
              {{ product.name }}
            </h5>
            <h6 class="card-subtitle mb-3 text-muted">
              ${{ formatCurrency(product.price) }}
            </h6>
            <button @click="handleAddToCart(product)" class="btn btn-primary mt-auto">
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </div>
    <p v-else class="text-center text-muted mt-3">No products found.</p>

    <!-- Product Details Modal -->
    <ProductDetailsModal v-model="showModal" :product="selectedProduct" :is-loading="isLoadingProduct" />

    <!-- Pagination -->
    <div v-if="metadata.totalPages > 1" class="d-flex justify-content-center align-items-center mt-4 gap-3">
      <button
        class="btn btn-outline-secondary"
        @click="changePage(metadata.currentPage - 1)"
        :disabled="metadata.currentPage === 1"
      >
        Previous
      </button>
      <span>Page {{ metadata.currentPage }} of {{ metadata.totalPages }}</span>
      <button
        class="btn btn-outline-secondary"
        @click="changePage(metadata.currentPage + 1)"
        :disabled="metadata.currentPage === metadata.totalPages"
      >
        Next
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Brand, Category, Product, SearchProductFilters } from '@/interfaces/product.interface';
import { productService } from '@/services';
import { useCartStore } from '@/stores/cart.store';
import { formatCurrency } from '@/utils/formatter.util';
import { getProductImageUrl } from '@/utils/productImage.util';
import { onMounted, reactive, ref } from 'vue';
import { toast } from 'vue3-toastify';
import ProductDetailsModal from './ProductDetailsModal.vue';

const cartStore = useCartStore();

const showModal = ref(false);
const products = ref<Product[]>([]);
const categories = ref<Category[]>([]);
const brands = ref<Brand[]>([]);
const selectedProduct = ref<Product | null>(null);
const isLoadingProducts = ref(false);
const isLoadingProduct = ref(false);
const isLoadingBrands = ref(false);
const isLoadingCategories = ref(false);

const metadata = reactive({
  currentPage: 1,
  pageSize: 9,
  totalPages: 1,
  totalRecords: 0,
});

const filters = reactive<SearchProductFilters>({
  keyword: undefined,
  minPrice: undefined,
  maxPrice: undefined,
  brandId: '',
  categoryIds: [],
  page: 1,
  size: 9,
});

const loadBrands = async () => {
  isLoadingBrands.value = true;
  try {
    const { data } = await productService.listBrand();
    brands.value = data;
  } catch {
    toast.error('Failed to load brands');
  } finally {
    isLoadingBrands.value = false;
  }
};

const loadCategories = async () => {
  isLoadingCategories.value = true;
  try {
    const { data } = await productService.listCategory();
    categories.value = data;
  } catch {
    toast.error('Failed to load categories');
  } finally {
    isLoadingCategories.value = false;
  }
};

const applyFilters = async (page: number) => {
  if (isLoadingProducts.value) return;

  isLoadingProducts.value = true;
  try {
    filters.page = page;
    metadata.currentPage = page;
    const response = await productService.searchProduct(filters);
    products.value = response.data;
    Object.assign(metadata, {
      currentPage: response.metadata?.currentPage ?? 1,
      pageSize: response.metadata?.pageSize ?? 9,
      totalPages: response.metadata?.totalPages ?? 1,
      totalRecords: response.metadata?.totalRecords ?? 0,
    });
  } catch {
    toast.error('Failed to load products');
  } finally {
    isLoadingProducts.value = false;
  }
};

const changePage = (newPage: number) => {
  if (newPage >= 1 && newPage <= metadata.totalPages) {
    applyFilters(newPage);
  }
};

const resetFilters = () => {
  Object.assign(filters, {
    keyword: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    brandId: undefined,
    categoryIds: undefined,
    page: 1,
    size: 9,
  });
  applyFilters(1);
};

const showProductDetails = async (product: Product) => {
  isLoadingProduct.value = true; // Set loading state
  try {
    const response = await productService.getProduct(product.id); // Fetch product details
    selectedProduct.value = response.data; // Set fetched product
    showModal.value = true; // Open modal
  } catch (error) {
    toast.error('Failed to load product details');
    console.error(error);
  } finally {
    isLoadingProduct.value = false; // Reset loading state
  }
};

const handleAddToCart = (product: Product) => {
  cartStore.addToCart(product);
};

onMounted(async () => {
  await Promise.all([loadBrands(), loadCategories(), applyFilters(1)]);
});
</script>

<style scoped>
.product-image {
  height: 180px;
  object-fit: cover;
}

.product-name {
  cursor: pointer;
  transition: color 0.2s;
}

.product-name:hover {
  color: #0d6efd;
  text-decoration: underline;
}
</style>
