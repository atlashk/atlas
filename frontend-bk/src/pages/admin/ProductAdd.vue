<template>
  <div class="container-fluid px-5 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-1">Create New Product</h3>
        <p class="text-muted mb-0">Add a new product to your catalog</p>
      </div>
      <button class="btn btn-outline-secondary" @click="router.back()">
        <i class="bi bi-arrow-left"></i> Back
      </button>
    </div>

    <form @submit.prevent="handleSubmit">
      <!-- Basic Information Card -->
      <div class="card shadow-sm mb-4">
        <div class="card-header bg-light py-3">
          <h5 class="mb-0">Product Basic Information</h5>
        </div>
        <div class="card-body p-4">
          <div class="row g-4">
            <div class="col-md-6">
              <label class="form-label">Product Name <span class="text-danger">*</span></label>
              <input v-model="form.name" type="text" class="form-control" required />
            </div>

            <div class="col-md-6">
              <label class="form-label">Price <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input v-model.number="form.price" type="number" step="0.01" min="0" class="form-control" required />
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Quantity <span class="text-danger">*</span></label>
              <input v-model.number="form.quantity" type="number" min="0" class="form-control" required />
            </div>

            <div class="col-md-6">
              <label class="form-label">Status <span class="text-danger">*</span></label>
              <select v-model="form.status" class="form-select" required>
                <option v-for="status in Object.values(ProductStatus)" :key="status" :value="status">
                  {{ formatStatusLabel(status) }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Available From <span class="text-danger">*</span></label>
              <input v-model="form.availableFrom" type="datetime-local" class="form-control" required />
            </div>

            <div class="col-md-6">
              <label class="form-label">Active Status <span class="text-danger">*</span></label>
              <div class="form-check form-switch">
                <input v-model="form.isActive" class="form-check-input" type="checkbox" role="switch" id="activeSwitch">
                <label class="form-check-label" for="activeSwitch">Product is active</label>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Brand <span class="text-danger">*</span></label>
              <div v-if="isLoadingBrands" class="text-center py-2">
                <div class="spinner-border spinner-border-sm" role="status">
                  <span class="visually-hidden">Loading brands...</span>
                </div>
              </div>
              <select v-else v-model.number="form.brandId" class="form-select" required :disabled="!brands.length">
                <option :value="0">Select a brand</option>
                <option v-for="brand in brands" :key="brand.id" :value="brand.id">{{ brand.name }}</option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Categories <span class="text-danger">*</span></label>
              <div v-if="isLoadingCategories" class="text-center py-2">
                <div class="spinner-border spinner-border-sm" role="status">
                  <span class="visually-hidden">Loading categories...</span>
                </div>
              </div>
              <select v-else v-model="form.categoryIds" multiple class="form-select" required :disabled="!categories.length" size="3">
                <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
              </select>
              <small class="text-muted">Hold Ctrl/Cmd to select multiple</small>
            </div>

            <div class="col-12">
              <label class="form-label">Product Image</label>
              <input type="file" class="form-control" @change="handleImageUpload" accept="image/*" />
              <div v-if="imagePreview" class="mt-2">
                <img :src="imagePreview" alt="Preview" class="img-thumbnail" style="max-height: 200px" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Details Card -->
      <div class="card shadow-sm mb-4">
        <div class="card-header bg-light py-3">
          <h5 class="mb-0">Product Details</h5>
        </div>
        <div class="card-body p-4">
          <div class="row g-4">
            <div class="col-12">
              <label class="form-label">Description <span class="text-danger">*</span></label>
              <textarea v-model="form.details.description" class="form-control" rows="5" required></textarea>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Attributes Card -->
      <div class="card shadow-sm mb-4">
        <div class="card-header bg-light py-3 d-flex justify-content-between align-items-center">
          <h5 class="mb-0">Product Attributes</h5>
          <button type="button" class="btn btn-outline-primary" @click="addAttribute">
            <i class="bi bi-plus"></i> Add Attribute
          </button>
        </div>
        <div class="card-body p-4">
          <div v-for="(attr, index) in form.attributes" :key="index" class="row g-3 mb-3">
            <div class="col-md-5">
              <input v-model="attr.name" type="text" class="form-control" placeholder="Attribute name" />
            </div>
            <div class="col-md-5">
              <input v-model="attr.value" type="text" class="form-control" placeholder="Attribute value" />
            </div>
            <div class="col-md-2">
              <button type="button" class="btn btn-outline-danger" @click="removeAttribute(index)">
                <i class="bi bi-trash"></i> Remove
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Form Actions -->
      <div class="d-flex justify-content-end gap-2 mt-4">
        <button type="button" class="btn btn-outline-secondary" @click="router.back()">
          <i class="bi bi-x"></i> Cancel
        </button>
        <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
          <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-check2 me-2"></i> Create Product
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ProductStatus, type Brand, type Category, type CreateProductRequest } from '@/interfaces/product.interface';
import { productService } from '@/services';
import { useFlashStore } from '@/stores/flash.store';
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';

const router = useRouter();
const flashStore = useFlashStore();

const brands = ref<Brand[]>([]);
const categories = ref<Category[]>([]);
const isLoadingBrands = ref(true);
const isLoadingCategories = ref(true);
const isSubmitting = ref(false);
const imagePreview = ref<string | null>(null);

const form = reactive<CreateProductRequest>({
  name: '',
  price: 0,
  image: '',
  quantity: 0,
  status: ProductStatus.IN_STOCK,
  availableFrom: '',
  isActive: true,
  brandId: 0,
  categoryIds: [],
  details: {
    description: ''
  },
  attributes: []
});

const formatStatusLabel = (status: ProductStatus): string => {
  return status.split('_')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};

const loadBrands = async () => {
  try {
    const response = await productService.listBrand();
    if (response.success) {
      brands.value = response.data;
    }
  } finally {
    isLoadingBrands.value = false;
  }
};

const loadCategories = async () => {
  try {
    const response = await productService.listCategory();
    if (response.success) {
      categories.value = response.data;
    }
  } finally {
    isLoadingCategories.value = false;
  }
};

const handleImageUpload = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files[0]) {
    const file = input.files[0];
    const reader = new FileReader();

    reader.onload = (e) => {
      const result = e.target?.result as string;
      imagePreview.value = result;
      form.image = result;
    };

    reader.readAsDataURL(file);
  }
};

const addAttribute = () => {
  form.attributes.push({ name: '', value: '' });
};

const removeAttribute = (index: number) => {
  form.attributes.splice(index, 1);
};

const handleSubmit = async () => {
  try {
    if (form.brandId === 0) {
      toast.error('Please select a brand');
      return;
    }

    if (form.categoryIds.length === 0) {
      toast.error('Please select at least one category');
      return;
    }

    isSubmitting.value = true;

    // Remove empty attributes
    form.attributes = form.attributes.filter(attr => attr.name.trim() && attr.value.trim());

    // Format date to ISO string
    const formData: CreateProductRequest = {
      ...form,
      availableFrom: new Date(form.availableFrom).toISOString()
    };

    const response = await productService.createProduct(formData);

    if (response.success) {
      flashStore.setSuccess('Added product successfully!');
      router.push({ name: 'productList' });
    } else {
      toast.error(response.errorMessage || 'Failed to add product');
    }
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(async () => {
  await Promise.all([
    loadBrands(),
    loadCategories(),
  ]);
});
</script>

<style scoped>
/* Only keep truly custom styles that Bootstrap doesn't provide */
</style>
