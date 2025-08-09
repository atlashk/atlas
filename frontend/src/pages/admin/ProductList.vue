<template>
  <div class="container-fluid px-5 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-1">Product Management</h3>
        <p class="text-muted mb-0">Manage your product catalog</p>
      </div>
      <div class="d-flex gap-2">
        <button 
          class="btn btn-outline-primary" 
          @click="handleImportClick"
          :disabled="isImporting"
        >
          <i class="bi bi-upload me-1"></i>
          <span v-if="isImporting">
            <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
            Importing...
          </span>
          <span v-else>Import</span>
        </button>
        <ExportDropdown 
          :is-exporting="isExporting"
          @export="handleExport"
        />
        <button class="btn btn-success" @click="router.push({ name: 'adminProductAdd' })">
          <i class="bi bi-plus-lg"></i> Add New Product
        </button>
      </div>
    </div>

    <!-- Filters Card -->
    <div class="card mb-4 shadow-sm">
      <div class="card-header bg-light py-3">
        <h5 class="mb-0">Search Filters</h5>
      </div>
      <div class="card-body p-4">
        <form @submit.prevent="applyFilters(1)">
          <div class="row g-4">
            <div class="col-md-6">
              <label for="productId" class="form-label">Product ID</label>
              <input
                id="productId"
                v-model.trim="filters.id"
                type="text"
                placeholder="Enter product ID..."
                class="form-control"
              />
            </div>

            <div class="col-md-6">
              <label for="keyword" class="form-label">Search</label>
              <input
                id="keyword"
                v-model.trim="filters.keyword"
                type="text"
                placeholder="Search by product name or description..."
                class="form-control"
              />
            </div>

            <div class="col-md-6">
              <label class="form-label">Price Range</label>
              <div class="input-group">
                <input
                  v-model.number="filters.minPrice"
                  type="number"
                  step="0.01"
                  placeholder="Min price"
                  class="form-control"
                />
                <span class="input-group-text bg-light">to</span>
                <input
                  v-model.number="filters.maxPrice"
                  type="number"
                  step="0.01"
                  placeholder="Max price"
                  class="form-control"
                />
              </div>
            </div>

            <div class="col-md-6">
              <label for="status" class="form-label">Status</label>
              <select id="status" v-model="filters.status" class="form-select">
                <option value="">All statuses</option>
                <option v-for="status in productStatuses" :key="status" :value="status">
                  {{ formatProductStatusLabel(status) }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label for="availableFrom" class="form-label">Available From</label>
              <input
                id="availableFrom"
                v-model="filters.availableFrom"
                type="date"
                class="form-control"
              />
            </div>

            <div class="col-md-6">
              <label class="form-label">Activity</label>
              <div class="border rounded p-3">
                <div class="d-flex gap-4">
                  <div class="form-check">
                    <input
                      type="checkbox"
                      class="form-check-input"
                      id="activeCheck"
                      v-model="activeStates.active"
                      @change="handleActiveStateChange"
                    />
                    <label class="form-check-label" for="activeCheck">Active</label>
                  </div>
                  <div class="form-check">
                    <input
                      type="checkbox"
                      class="form-check-input"
                      id="inactiveCheck"
                      v-model="activeStates.inactive"
                      @change="handleActiveStateChange"
                    />
                    <label class="form-check-label" for="inactiveCheck">Inactive</label>
                  </div>
                </div>
              </div>
            </div>

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
                <option value="">All brands</option>
                <option v-for="brand in brands" :key="brand.id" :value="brand.id">
                  {{ brand.name }}
                </option>
              </select>
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
                multiple
                class="form-select"
                :disabled="!categories.length"
                size="3"
              >
                <option v-for="category in categories" :key="category.id" :value="category.id">
                  {{ category.name }}
                </option>
              </select>
              <small class="text-muted">Hold Ctrl/Cmd to select multiple</small>
            </div>
          </div>

          <div class="d-flex gap-2 mt-4 pt-3 border-top">
            <button type="submit" class="btn btn-primary">
              <i class="bi bi-search"></i> Search
            </button>
            <button type="button" @click="resetFilters" class="btn btn-outline-secondary">
              <i class="bi bi-arrow-counterclockwise"></i> Reset
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Products Table -->
    <div class="card shadow-sm">
      <div class="card-header bg-light py-3">
        <h5 class="mb-0">Product List</h5>
      </div>
      <div v-if="isLoadingProducts" class="text-center py-5" aria-live="polite">
        <div class="spinner-border text-primary" role="status" aria-label="Loading products">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
      <div v-else class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-bordered mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col" class="px-4">ID</th>
                <th scope="col" class="px-4">Name</th>
                <th scope="col" class="px-4">Image</th>
                <th scope="col" class="px-4">Price</th>
                <th scope="col" class="px-4">Quantity</th>
                <th scope="col" class="px-4">Status</th>
                <th scope="col" class="px-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td class="px-4">{{ product.id }}</td>
                <td class="px-4">{{ product.name }}</td>
                <td class="px-4">
                  <img
                    :src="getProductImageUrl(product.image)"
                    :alt="product.name"
                    class="rounded"
                    style="width: 48px; height: 48px; object-fit: cover;"
                  />
                </td>
                <td class="px-4">${{ formatCurrency(product.price) }}</td>
                <td class="px-4">{{ product.quantity }}</td>
                <td class="px-4">
                  <span :class="getProductStatusBadgeClasses(product.status)">
                    {{ formatProductStatusLabel(product.status) }}
                  </span>
                </td>
                <td class="px-4">
                  <div class="d-flex gap-2">
                    <button
                      class="btn btn-sm btn-outline-secondary"
                      @click="router.push({ name: 'adminProductInfo', params: { id: product.id } })"
                    >
                      <i class="bi bi-eye me-1"></i> View
                    </button>
                    <button
                      class="btn btn-sm btn-outline-primary"
                      @click="router.push({ name: 'adminProductEdit', params: { id: product.id } })"
                    >
                      <i class="bi bi-pencil me-1"></i> Edit
                    </button>
                    <button
                      class="btn btn-sm btn-outline-danger"
                      @click="handleDelete(product.id)"
                    >
                      <i class="bi bi-trash me-1"></i> Delete
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="card-footer bg-light py-3">
          <div class="d-flex justify-content-between align-items-center">
            <span class="text-muted">
              Page {{ metadata.currentPage }} of {{ metadata.totalPages }}
              <span class="ms-2">({{ metadata.totalRecords }} records)</span>
            </span>
            <div class="btn-group">
              <button
                @click="changePage(metadata.currentPage - 1)"
                :disabled="metadata.currentPage <= 1"
                class="btn btn-outline-secondary px-3"
              >
                <i class="bi bi-chevron-left me-1"></i> Previous
              </button>
              <button
                @click="changePage(metadata.currentPage + 1)"
                :disabled="metadata.currentPage >= metadata.totalPages"
                class="btn btn-outline-secondary px-3"
              >
                Next <i class="bi bi-chevron-right ms-1"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Import Modal -->
    <ImportProductModal @imported="handleImportSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ProductStatus, FileType, type Brand, type Category, type ListProductFilters, type Product, type ExportProductFilters } from '@/interfaces/product.interface';
import { productService } from '@/services';
import { useFlashStore } from '@/stores/flash.store';
import { formatCurrency, formatProductStatusLabel, getProductStatusBadgeClasses } from '@/utils/formatter.util';
import { getProductImageUrl } from '@/utils/productImage.util';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import ExportDropdown from '@/components/admin/ExportDropdown.vue';
import ImportProductModal from '@/components/admin/ImportProductModal.vue';

const router = useRouter();
const flashStore = useFlashStore();

const activeStates = reactive({
  active: true,
  inactive: true,
});
const brands = ref<Brand[]>([]);
const categories = ref<Category[]>([]);
const products = ref<Product[]>([]);
const isLoadingBrands = ref(false);
const isLoadingCategories = ref(false);
const isLoadingProducts = ref(false);
const isExporting = ref(false);
const isImporting = ref(false);
const metadata = reactive({
  currentPage: 1,
  pageSize: 20,
  totalPages: 1,
  totalRecords: 0,
});
const filters = reactive<ListProductFilters>({
  id: undefined,
  keyword: undefined,
  minPrice: undefined,
  maxPrice: undefined,
  status: '',
  availableFrom: undefined,
  isActive: undefined,
  brandId: '',
  categoryIds: [],
  page: 1,
  size: 20,
});

const productStatuses = computed(() => Object.values(ProductStatus));

const loadBrands = async () => {
  isLoadingBrands.value = true;
  try {
    const { data } = await productService.listBrand();
    brands.value = data;
  } catch (error) {
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
  } catch (error) {
    toast.error('Failed to load categories');
  } finally {
    isLoadingCategories.value = false;
  }
};

const handleActiveStateChange = () => {
  if (activeStates.active && activeStates.inactive) {
    filters.isActive = undefined;
  } else if (activeStates.active) {
    filters.isActive = true;
  } else if (activeStates.inactive) {
    filters.isActive = false;
  } else {
    filters.isActive = undefined;
  }
};

const applyFilters = async (page: number) => {
  if (isLoadingProducts.value) return;

  isLoadingProducts.value = true;
  try {
    filters.page = page;
    metadata.currentPage = page;
    const response = await productService.listProduct(filters);
    products.value = response.data;
    Object.assign(metadata, response.metadata);
  } catch (error) {
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
  activeStates.active = true;
  activeStates.inactive = true;
  Object.assign(filters, {
    id: undefined,
    keyword: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    status: '',
    availableFrom: undefined,
    isActive: undefined,
    brandId: '',
    categoryIds: [],
    page: 1,
    size: 20,
  });
  applyFilters(1);
};

const handleDelete = async (productId: number) => {
  if (!confirm('Are you sure you want to delete this product?')) return;

  try {
    await productService.deleteProduct(productId);
    toast.success('Product deleted successfully');
    applyFilters(metadata.currentPage); // Preserve current page after deletion
  } catch (error) {
    toast.error('Failed to delete product');
  }
};

const handleExport = async (fileType: 'csv' | 'excel') => {
  if (isExporting.value) return;

  isExporting.value = true;
  try {    
    const exportFilters: ExportProductFilters = {
      id: filters.id,
      keyword: filters.keyword,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      status: filters.status,
      availableFrom: filters.availableFrom,
      isActive: filters.isActive,
      brandId: filters.brandId,
      categoryIds: filters.categoryIds,
      fileType: fileType === 'csv' ? FileType.CSV : FileType.EXCEL
    };
    await productService.exportProduct(exportFilters);
    toast.success(`Products exported successfully as ${fileType.toUpperCase()}`);
  } catch (error) {
    console.error('Export failed:', error);
    toast.error('Failed to export products');
  } finally {
    isExporting.value = false;
  }
};

const handleImportClick = () => {
  const modalElement = document.getElementById('importProductModal');
  if (modalElement) {
    modalElement.classList.add('show');
    modalElement.style.display = 'block';
    modalElement.setAttribute('aria-modal', 'true');
    modalElement.setAttribute('role', 'dialog');
    
    // Add backdrop
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.id = 'modal-backdrop';
    document.body.appendChild(backdrop);
    
    // Add body class
    document.body.classList.add('modal-open');
  }
};

const handleImportSuccess = () => {
  // Refresh the product list after successful import
  applyFilters(1);
};

onMounted(async () => {
  try {
    await Promise.all([
      loadBrands(),
      loadCategories(),
      applyFilters(1),
    ]);
    if (flashStore.successMessage) toast.success(flashStore.successMessage);
    if (flashStore.errorMessage) toast.error(flashStore.errorMessage);
  } finally {
    isLoadingProducts.value = false;  // Ensure it's set to false after initial load
  }
});
</script>

<style scoped>
.table-responsive {
  min-height: 200px;
}

/* Use Bootstrap's .table-hover instead of custom hover styles */

/* Use Bootstrap's .shadow-sm instead of custom image shadows */
</style>
