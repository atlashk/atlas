<template>
  <div class="login-container d-flex align-items-center justify-content-center min-vh-100">
    <div class="login-card bg-white p-4 p-md-5 rounded-4 shadow-lg">
      <div class="text-center mb-4">
        <img src="@/assets/images/logo.svg" alt="Atlas Logo" class="logo mb-3" />
        <h3 class="fw-bold text-primary">Welcome Back</h3>
        <p class="text-muted">Sign in to your account</p>
      </div>
      
      <div v-if="successMessage" class="alert alert-success" role="alert">
        {{ successMessage }}
      </div>
      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>
      
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label class="form-label fw-medium">Username</label>
          <div class="position-relative">
            <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
            <input v-model="username" type="text" class="form-control form-control-lg ps-5" placeholder="Enter your username" required />
          </div>
        </div>
        
        <div class="mb-4">
          <div class="d-flex justify-content-between">
            <label class="form-label fw-medium">Password</label>
            <a href="#" class="text-decoration-none small">Forgot password?</a>
          </div>
          <div class="position-relative">
            <i class="bi bi-lock position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
            <input v-model="password" type="password" class="form-control form-control-lg ps-5" placeholder="Enter your password" required />
          </div>
        </div>
        
        <button type="submit" class="btn btn-primary btn-lg w-100 mb-4">
          <span v-if="isLoggingIn" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          <span v-else><i class="bi bi-box-arrow-in-right me-2"></i></span>
          Sign In
        </button>
        
        <div class="text-center">
          <p class="text-muted">Don't have an account? <router-link to="/register" class="text-decoration-none fw-medium">Sign up</router-link></p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { LoginRequest } from '@/interfaces/auth.interface'
import { useUserStore } from '@/stores/user.store'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const successMessage = ref('')
const errorMessage = ref('')
const isLoggingIn = ref(false)
const userStore = useUserStore()
const router = useRouter()

const handleLogin = async () => {
  try {
    isLoggingIn.value = true
    errorMessage.value = ''
    
    const credentials: LoginRequest = {
      identifier: username.value,
      password: password.value
    }
    
    const response = await userStore.login(credentials)
    if (response.success) {
      router.push('/')
    } else {
      errorMessage.value = response.errorMessage || 'Login failed. Please check your credentials.'
    }
  } catch (error) {
    errorMessage.value = 'An unexpected error occurred. Please try again.'
  } finally {
    isLoggingIn.value = false
  }
}
</script>

<style scoped>
.login-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-card {
  width: 100%;
  max-width: 450px;
  transition: all 0.3s ease;
}

.logo {
  height: 60px;
  width: auto;
}

.form-control:focus {
  border-color: #4dabf7;
  box-shadow: 0 0 0 0.25rem rgba(77, 171, 247, 0.25);
}

.btn-primary {
  background-color: #41b883;
  border-color: #41b883;
  transition: all 0.2s;
}

.btn-primary:hover, .btn-primary:focus {
  background-color: #34a873;
  border-color: #34a873;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.btn-primary:active {
  transform: translateY(0);
}

@media (max-width: 576px) {
  .login-card {
    margin: 1rem;
  }
}
</style>
