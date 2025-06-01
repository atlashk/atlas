<template>
  <div class="container-fluid px-5 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-1">User Management</h3>
        <p class="text-muted mb-0">Manage your user accounts</p>
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
              <label for="userId" class="form-label">User ID</label>
              <input
                id="userId"
                v-model.trim="filters.id"
                type="text"
                class="form-control"
                placeholder="Enter user ID"
              />
            </div>
            <div class="col-md-6">
              <label for="keyword" class="form-label">Search</label>
              <input
                id="keyword"
                v-model.trim="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Search by username, name, or email"
              />
            </div>
            <div class="col-md-6">
              <label for="role" class="form-label">Role</label>
              <select
                id="role"
                v-model="filters.role"
                class="form-select"
              >
                <option value="">All Roles</option>
                <option value="ADMIN">Admin</option>
                <option value="USER">User</option>
              </select>
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

    <!-- Users Table -->
    <div class="card shadow-sm">
      <div class="card-header bg-light py-3">
        <h5 class="mb-0">User List</h5>
      </div>
      <div v-if="isLoadingUsers" class="text-center py-5" aria-live="polite">
        <div class="spinner-border text-primary" role="status" aria-label="Loading users">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
      <div v-else class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-bordered mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col" class="px-4">ID</th>
                <th scope="col" class="px-4">Username</th>
                <th scope="col" class="px-4">Name</th>
                <th scope="col" class="px-4">Email</th>
                <th scope="col" class="px-4">Phone</th>
                <th scope="col" class="px-4">Role</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td class="px-4">{{ user.id }}</td>
                <td class="px-4">{{ user.username }}</td>
                <td class="px-4">{{ user.firstName }} {{ user.lastName }}</td>
                <td class="px-4">{{ user.email }}</td>
                <td class="px-4">{{ user.phoneNumber || 'N/A' }}</td>
                <td class="px-4">
                  <span :class="getRoleBadgeClasses(user.role as Role)">
                    {{ user.role }}
                  </span>
                </td>
              </tr>
              <tr v-if="!users.length">
                <td colspan="6" class="text-center text-muted py-4">
                  No users found
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
import type { Role } from '@/constants/app.constants';
import type { ListUserFilters, User } from '@/interfaces/user.interface';
import { userService } from '@/services';
import { getRoleBadgeClasses } from '@/utils/formatter.util';
import { onMounted, reactive, ref } from 'vue';
import { toast } from 'vue3-toastify';

const users = ref<User[]>([]);
const isLoadingUsers = ref(true);
const metadata = reactive({
  currentPage: 1,
  pageSize: 20,
  totalPages: 1,
  totalRecords: 0,
});
const filters = reactive<ListUserFilters>({
  id: undefined,
  keyword: undefined,
  role: '',
  page: 1,
  size: 20,
});

const applyFilters = async (page: number) => {
  isLoadingUsers.value = true;
  try {
    filters.page = page;
    metadata.currentPage = page;

    // Clean up empty or undefined filters
    const apiFilters: ListUserFilters = { ...filters };
    Object.keys(apiFilters).forEach((key) => {
      const typedKey = key as keyof ListUserFilters;
      if (apiFilters[typedKey] === '' || apiFilters[typedKey] === undefined) {
        delete apiFilters[typedKey];
      }
    });

    console.log('Fetching users with filters:', apiFilters);
    const response = await userService.listUser(apiFilters);
    
    if (response.success) {
      users.value = response.data || [];
      if (response.metadata) {
        Object.assign(metadata, response.metadata);
      }
      console.log('Users loaded:', users.value.length);
    } else {
      toast.error(response.errorMessage || 'Failed to load users');
      console.error('Failed to load users:', response.errorMessage);
    }
  } catch (error: any) {
    console.error('Error loading users:', error);
    toast.error(error.message || 'Failed to load users');
    // Initialize empty array to avoid undefined errors
    users.value = [];
  } finally {
    isLoadingUsers.value = false;
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
    keyword: undefined,
    role: undefined,
    page: 1,
    size: 20,
  });
  applyFilters(1);
};

// Always fetch users when component mounts
onMounted(() => {
  console.log('UserList component mounted, fetching users...');
  applyFilters(1);
});
</script>

<style scoped>
.table-responsive {
  min-height: 200px;
}
</style>
