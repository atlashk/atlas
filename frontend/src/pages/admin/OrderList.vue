<template>
  <div class="container-fluid px-5 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-1">Order Management</h3>
        <p class="text-muted mb-0">Manage customer orders</p>
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
              <label for="orderId" class="form-label">Order ID</label>
              <input
                id="orderId"
                v-model.trim="filters.orderId"
                type="text"
                placeholder="Enter order ID..."
                class="form-control"
              />
            </div>

            <div class="col-md-6">
              <label for="userId" class="form-label">User ID</label>
              <input
                id="userId"
                v-model.trim="filters.userId"
                type="text"
                placeholder="Enter user ID..."
                class="form-control"
              />
            </div>

            <div class="col-md-6">
              <label for="status" class="form-label">Status</label>
              <select id="status" v-model="filters.status" class="form-select">
                <option value="">All statuses</option>
                <option v-for="status in orderStatuses" :key="status" :value="status">
                  {{ formatOrderStatusLabel(status) }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Date Range</label>
              <div class="input-group">
                <input
                  v-model="filters.startDate"
                  type="date"
                  placeholder="Start date"
                  class="form-control"
                />
                <span class="input-group-text bg-light">to</span>
                <input
                  v-model="filters.endDate"
                  type="date"
                  placeholder="End date"
                  class="form-control"
                />
              </div>
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

    <!-- Orders Table Card -->
    <div class="card shadow-sm">
      <div class="card-header bg-light py-3">
        <h5 class="mb-0">Order List</h5>
      </div>
      <div v-if="isLoadingOrders" class="text-center py-5" aria-live="polite">
        <div class="spinner-border text-primary" role="status" aria-label="Loading orders">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
      <div v-else class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-bordered mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col" class="px-4">ID</th>
                <th scope="col" class="px-4">Code</th>
                <th scope="col" class="px-4">User</th>
                <th scope="col" class="px-4">Amount</th>
                <th scope="col" class="px-4">Status</th>
                <th scope="col" class="px-4">Created At</th>
                <th scope="col" class="px-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.id">
                <td class="px-4">{{ order.id }}</td>
                <td class="px-4">{{ order.code }}</td>
                <td class="px-4">{{ order.user ? `${order.user.firstName} ${order.user.lastName}` : 'N/A' }}</td>
                <td class="px-4">${{ formatCurrency(order.amount) }}</td>
                <td class="px-4">
                  <span :class="getOrderStatusBadgeClasses(order.status)">
                    {{ formatOrderStatusLabel(order.status) }}
                  </span>
                </td>
                <td class="px-4">{{ formatDate(order.createdAt) }}</td>
                <td class="px-4">
                  <button
                    class="btn btn-sm btn-outline-secondary"
                    @click="toggleDetails(order.id)"
                  >
                    {{ selectedOrderId === order.id ? 'Hide Details' : 'View Details' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Order Details -->
        <template v-for="order in orders" :key="'details-' + order.id">
          <div v-show="selectedOrderId === order.id" class="p-4 border-top">
            <h6 class="mb-3">User Information</h6>
            <dl class="row mb-3">
              <dt class="col-sm-3">User ID</dt>
              <dd class="col-sm-9">{{ order.user?.id ?? 'N/A' }}</dd>
              <dt class="col-sm-3">First Name</dt>
              <dd class="col-sm-9">{{ order.user?.firstName ?? 'N/A' }}</dd>
              <dt class="col-sm-3">Last Name</dt>
              <dd class="col-sm-9">{{ order.user?.lastName ?? 'N/A' }}</dd>
            </dl>

            <div v-if="order.cancelReason" class="alert alert-danger mb-3">
              <strong>Cancellation Reason:</strong> {{ order.cancelReason }}
            </div>

            <h6 class="mb-3">Order Items</h6>
            <div class="table-responsive">
              <table class="table table-bordered">
                <thead class="table-light">
                  <tr>
                    <th scope="col">Product ID</th>
                    <th scope="col">Product Name</th>
                    <th scope="col">Price</th>
                    <th scope="col">Quantity</th>
                    <th scope="col">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in order.orderItems" :key="item.product.id">
                    <td>{{ item.product.id }}</td>
                    <td>{{ item.product.name }}</td>
                    <td>${{ formatCurrency(item.product.price) }}</td>
                    <td>{{ item.quantity }}</td>
                    <td>${{ formatCurrency(item.product.price * item.quantity) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </template>

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
                Previous
              </button>
              <button
                @click="changePage(metadata.currentPage + 1)"
                :disabled="metadata.currentPage >= metadata.totalPages"
                class="btn btn-outline-secondary px-3"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { orderService } from '@/services';
import { formatCurrency, formatDate, formatOrderStatusLabel, getOrderStatusBadgeClasses } from '@/utils/formatter.util';
import { computed, onMounted, reactive, ref } from 'vue';
import { toast } from 'vue3-toastify';
import { OrderStatus, type ListOrderFilters, type Order } from '../../interfaces/order.interface';

const orders = ref<Order[]>([]);
const isLoadingOrders = ref(true);
const selectedOrderId = ref<number | null>(null);
const filters = reactive<ListOrderFilters>({
  orderId: undefined,
  userId: undefined,
  status: '',
  startDate: undefined,
  endDate: undefined,
  page: 1,
  size: 20,
});
const metadata = reactive({
  currentPage: 1,
  pageSize: 20,
  totalPages: 1,
  totalRecords: 0,
});

// Computed property for order statuses
const orderStatuses = computed(() => Object.values(OrderStatus));

const toggleDetails = (orderId: number) => {
  selectedOrderId.value = selectedOrderId.value === orderId ? null : orderId;
};

const applyFilters = async (page: number) => {
  isLoadingOrders.value = true;
  try {
    filters.page = page;
    metadata.currentPage = page;
    
    const response = await orderService.listOrder(filters);
    
    orders.value = response.data;
    Object.assign(metadata, response.metadata);
    selectedOrderId.value = null;
  } catch (error) {
    toast.error('Failed to load orders');
    orders.value = []; // Initialize empty array to avoid undefined errors
  } finally {
    isLoadingOrders.value = false;
  }
};

const changePage = (newPage: number) => {
  if (newPage >= 1 && newPage <= metadata.totalPages) {
    applyFilters(newPage);
  }
};

const resetFilters = () => {
  Object.assign(filters, {
    id: undefined,
    userId: undefined,
    status: '',
    startDate: undefined,
    endDate: undefined,
    page: 1,
    size: 20,
  });
  applyFilters(1); // Reset to page 1
};

// Initial load
onMounted(() => {
  applyFilters(1);
});
</script>

<style scoped>
.table-responsive {
  min-height: 200px;
}
</style>
