<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark ">
    <div class="container-fluid">
      <!-- Brand name with custom navigation logic -->
      <a href="#" class="navbar-brand" @click="handleBrandClick">Atlas</a>

      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <!-- Order History for non-admin users -->
          <li v-if="userStore.isAuthenticated && userStore.profile?.role !== 'ADMIN'" class="nav-item">
            <router-link to="/order-history" class="nav-link">Order History</router-link>
          </li>

          <!-- Admin menu items -->
          <li v-if="userStore.isAuthenticated && userStore.profile?.role === 'ADMIN'" class="nav-item">
            <router-link to="/admin/user" class="nav-link">User Management</router-link>
          </li>
          <li v-if="userStore.isAuthenticated && userStore.profile?.role === 'ADMIN'" class="nav-item">
            <router-link to="/admin/product" class="nav-link">Product Management</router-link>
          </li>
          <li v-if="userStore.isAuthenticated && userStore.profile?.role === 'ADMIN'" class="nav-item">
            <router-link to="/admin/order" class="nav-link">Order Management</router-link>
          </li>
        </ul>

        <!-- Right-aligned user info & auth buttons -->
        <ul class="navbar-nav ms-auto d-flex align-items-center">
          <li v-if="userStore.isAuthenticated" class="nav-item">
            <span class="text-light me-3">Welcome, {{ userStore.fullName }}</span>
          </li>
          <li v-if="userStore.isAuthenticated" class="nav-item">
            <button class="btn btn-danger" @click="handleLogout">Logout</button>
          </li>
          <li v-else class="nav-item">
            <router-link to="/login" class="btn btn-light me-3">Login</router-link>
            <router-link to="/register" class="btn btn-outline-success">Register</router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user.store'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const handleBrandClick = () => {
  if (!userStore.isAuthenticated) {
    router.push({ name: 'storeFront' })
  } else if (userStore.profile?.role === 'ADMIN') {
    router.push({ name: 'adminDashboard' })
  } else {
    router.push({ name: 'storeFront' })
  }
}

const handleLogout = async () => {
  await userStore.logout();
  router.push({ name: 'storeFront' })
}
</script>
