<template>
  <div class="register-container d-flex align-items-center justify-content-center min-vh-100">
    <div class="register-card bg-white p-4 p-md-5 rounded-4 shadow-lg">
      <div class="text-center mb-4">
        <img src="@/assets/images/logo.svg" alt="Atlas Logo" class="logo mb-3" />
        <h3 class="fw-bold text-primary">Create Account</h3>
        <p class="text-muted">Join our community today</p>
      </div>
      
      <div v-if="successMessage" class="alert alert-success" role="alert">
        {{ successMessage }}
      </div>
      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>
      
      <form @submit.prevent="submit">
        <div class="row g-3">
          <div class="col-md-6 mb-3">
            <label class="form-label fw-medium">First Name</label>
            <div class="position-relative">
              <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
              <input v-model="firstName" type="text" class="form-control ps-5" placeholder="First name" required />
            </div>
          </div>
          
          <div class="col-md-6 mb-3">
            <label class="form-label fw-medium">Last Name</label>
            <div class="position-relative">
              <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
              <input v-model="lastName" type="text" class="form-control ps-5" placeholder="Last name" required />
            </div>
          </div>
          
          <div class="col-12 mb-3">
            <label class="form-label fw-medium">Username</label>
            <div class="position-relative">
              <i class="bi bi-person-badge position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
              <input v-model="username" type="text" class="form-control ps-5" placeholder="Choose a username" required />
            </div>
          </div>
          
          <div class="col-12 mb-3">
            <label class="form-label fw-medium">Email</label>
            <div class="position-relative">
              <i class="bi bi-envelope position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
              <input v-model="email" type="email" class="form-control ps-5" placeholder="Your email address" required />
            </div>
          </div>
          
          <div class="col-12 mb-3">
            <label class="form-label fw-medium">Phone Number</label>
            <div class="position-relative">
              <i class="bi bi-phone position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
              <input v-model="phoneNumber" type="text" class="form-control ps-5" placeholder="Your phone number" required />
            </div>
          </div>
          
          <div class="col-md-6 mb-3">
            <label class="form-label fw-medium">Password</label>
            <div class="position-relative">
              <i class="bi bi-lock position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
              <input v-model="password" type="password" class="form-control ps-5" placeholder="Create password" required />
            </div>
          </div>
          
          <div class="col-md-6 mb-3">
            <label class="form-label fw-medium">Confirm Password</label>
            <div class="position-relative">
              <i class="bi bi-lock-fill position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
              <input v-model="confirmPassword" type="password" class="form-control ps-5" placeholder="Confirm password" required />
            </div>
          </div>
          
          <div class="col-12 mt-2">
            <button type="submit" class="btn btn-primary btn-lg w-100 mb-3">
              <i class="bi bi-person-plus me-2"></i>Register
            </button>
            
            <div class="text-center">
              <p class="text-muted">Already have an account? <router-link to="/login" class="text-decoration-none fw-medium">Sign in</router-link></p>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { RegisterRequest } from '@/interfaces/user.interface';
import { userService } from '@/services';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// Form state
const username = ref<string>('');
const firstName = ref<string>('');
const lastName = ref<string>('');
const email = ref<string>('');
const phoneNumber = ref<string>('');
const password = ref<string>('');
const confirmPassword = ref<string>('');
const successMessage = ref<string>('');
const errorMessage = ref<string>('');

// API call to register
const submit = async () => {
  errorMessage.value = '';
  
  if (password.value !== confirmPassword.value) {
    errorMessage.value = 'Passwords do not match';
    return;
  }

  const request: RegisterRequest = {
    username: username.value,
    firstName: firstName.value,
    lastName: lastName.value,
    email: email.value,
    phoneNumber: phoneNumber.value,
    password: password.value,
  };

  try {
    await userService.register(request);
    successMessage.value = 'You registered successfully';
    resetForm();
    setTimeout(goToLogin, 1000);
  } catch (error) {
    errorMessage.value = 'Registration request failed: ' + error;
  }
};

const goToLogin = () => {
  router.push({ name: 'login' });
};

// Reset form fields
const resetForm = () => {
  username.value = '';
  firstName.value = '';
  lastName.value = '';
  email.value = '';
  phoneNumber.value = '';
  password.value = '';
  confirmPassword.value = '';
};  
</script>

<style scoped>
.register-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.register-card {
  width: 100%;
  max-width: 700px;
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
  .register-card {
    margin: 1rem;
  }
}
</style>
