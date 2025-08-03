<template>
  <div>
    <h3 class="my-4">Order History</h3>

    <!-- Filters Section -->
    <div class="card mb-4">
      <div class="card-body">
        <form @submit.prevent="applyFilters(1)">
          <div class="row g-3">
            <div class="col-md-4">
              <label for="startDate" class="form-label">Start Date</label>
              <input id="startDate" v-model="filters.startDate" type="date" class="form-control" />
            </div>
            <div class="col-md-4">
              <label for="endDate" class="form-label">End Date</label>
              <input id="endDate" v-model="filters.endDate" type="date" class="form-control" />
            </div>
            <div class="col-md-4">
              <label for="status" class="form-label">Status</label>
              <select id="status" v-model="filters.status" class="form-select">
                <option value="">All Statuses</option>
                <option v-for="status in orderStatuses" :key="status" :value="status">
                  {{ formatOrderStatusLabel(status) }}
                </option>
              </select>
            </div>
          </div>
          <div class="d-flex gap-2 mt-3">
            <button type="submit" class="btn btn-primary">
              <i class="bi bi-search me-1"></i> Search
            </button>
            <button type="button" @click="resetFilters" class="btn btn-outline-secondary">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="text-center my-4" aria-live="polite">
      <div class="spinner-border" role="status" aria-label="Loading orders">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <!-- No Orders -->
    <p v-else-if="!orders.length" class="text-center text-muted mt-3">No orders found.</p>

    <!-- Orders List -->
    <ul v-else class="list-group mb-4">
      <li v-for="order in orders" :key="order.id" class="list-group-item">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <p><strong>Order #{{ order.code }}</strong> - {{ formatDate(order.createdAt) }}</p>
            <p><strong>Total:</strong> {{ formatCurrency(order.amount) }}</p>
            <p>
              <strong class="me-1">Status:</strong>
              <span :class="getOrderStatusBadgeClasses(order.status)">
                {{ formatOrderStatusLabel(order.status) }}
              </span>
            </p>
            <div v-if="order.status === OrderStatus.CANCELED && order.cancelReason" class="mt-3">
              <p><strong>Cancellation Reason:</strong> <span class="text-danger">{{ order.cancelReason }}</span></p>
            </div>
          </div>
          <button @click="toggleDetails(order.id)" class="btn btn-outline-secondary btn-sm">
            {{ selectedOrderId === order.id ? 'Hide Details' : 'View Details' }}
          </button>
        </div>

        <!-- Order Details -->
        <div v-show="selectedOrderId === order.id" class="mt-3">
          <table class="table table-bordered">
            <thead>
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
                <td>{{ formatCurrency(item.product.price) }}</td>
                <td>{{ item.quantity }}</td>
                <td>{{ formatCurrency(item.product.price * item.quantity) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </li>
    </ul>

    <!-- Pagination -->
    <div v-if="metadata.totalPages > 1" class="d-flex justify-content-center align-items-center mt-4 gap-3">
      <button class="btn btn-outline-secondary" @click="changePage(metadata.currentPage - 1)"
        :disabled="metadata.currentPage === 1">
        Previous
      </button>
      <span>Page {{ metadata.currentPage }} of {{ metadata.totalPages }}</span>
      <button class="btn btn-outline-secondary" @click="changePage(metadata.currentPage + 1)"
        :disabled="metadata.currentPage === metadata.totalPages">
        Next
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { OrderStatus, type ListOrderFilters, type Order } from '@/interfaces/order.interface';
import { orderService } from '@/services';
import { formatCurrency, formatDate, formatOrderStatusLabel, getOrderStatusBadgeClasses } from '@/utils/formatter.util';
import { computed, onMounted, reactive, ref } from 'vue';
import { toast } from 'vue3-toastify';

const orders = ref<Order[]>([]);
const selectedOrderId = ref<number | null>(null);
const isLoading = ref(false);
const filters = reactive<ListOrderFilters>({
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

const applyFilters = async (page: number) => {
  if (isLoading.value) return;

  isLoading.value = true;
  try {
    filters.page = page;
    metadata.currentPage = page;

    // Clean up empty or undefined filters
    const apiFilters: ListOrderFilters = { ...filters };
    Object.keys(apiFilters).forEach((key) => {
      const typedKey = key as keyof ListOrderFilters;
      if (apiFilters[typedKey] === '' || apiFilters[typedKey] === undefined) {
        delete apiFilters[typedKey];
      }
    });

    const response = await orderService.listOrder(apiFilters);
    orders.value = response.data;
    Object.assign(metadata, response.metadata);
  } catch (error) {
    toast.error('Failed to load orders');
  } finally {
    isLoading.value = false;
  }
};

const changePage = (newPage: number) => {
  if (newPage >= 1 && newPage <= metadata.totalPages) {
    applyFilters(newPage);
  }
};

const resetFilters = () => {
  Object.assign(filters, {
    status: '',
    startDate: undefined,
    endDate: undefined,
  });
  applyFilters(1);
};

const toggleDetails = (orderId: number) => {
  selectedOrderId.value = selectedOrderId.value === orderId ? null : orderId;
};

onMounted(() => applyFilters(1));
</script>

<style scoped>
.table-responsive {
  min-height: 200px;
}
</style>