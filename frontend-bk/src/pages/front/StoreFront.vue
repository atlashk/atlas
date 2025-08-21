<template>
	<div class="container-fluid py-4">
		<div class="row g-4">
			<!-- Left side: Product Search and Order History -->
			<div class="col-lg-8">
				<!-- Navigation tabs -->
				<ul class="nav nav-tabs mb-3" id="storeTabs" role="tablist">
					<li class="nav-item" role="presentation">
						<button class="nav-link" :class="{ active: activeTab === 'products' }" 
							id="products-tab" @click="setActiveTab('products')" 
							type="button" role="tab" aria-controls="products" :aria-selected="activeTab === 'products'">
							<i class="bi bi-shop me-1"></i> Products
						</button>
					</li>
					<li class="nav-item" role="presentation" v-if="userStore.isAuthenticated">
						<button class="nav-link" :class="{ active: activeTab === 'order-history' }" 
							id="order-history-tab" @click="setActiveTab('order-history')" 
							type="button" role="tab" aria-controls="order-history" :aria-selected="activeTab === 'order-history'">
							<i class="bi bi-clock-history me-1"></i> Order History
						</button>
					</li>
				</ul>

				<!-- Tab content -->
				<div class="tab-content" id="storeTabsContent">
					<!-- Products tab -->
					<div class="tab-pane fade" :class="{ 'show active': activeTab === 'products' }" 
						id="products" role="tabpanel" aria-labelledby="products-tab">
						<ProductSearch />
					</div>
					
					<!-- Order History tab -->
					<div class="tab-pane fade" :class="{ 'show active': activeTab === 'order-history' }" 
						id="order-history" role="tabpanel" aria-labelledby="order-history-tab">
						<OrderHistory v-if="userStore.isAuthenticated" />
					</div>
				</div>
			</div>
			
			<!-- Right side: Cart and Order Tracking -->
			<div class="col-lg-4">
				<div class="sticky-top" style="top: 20px; z-index: 100;">
					<Cart />
					<OrderTracking v-if="currentOrderId" />
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import Cart from '@/components/front/Cart.vue';
import OrderTracking from '@/components/front/OrderTracking.vue';
import ProductSearch from '@/components/front/ProductSearch.vue';
import OrderHistory from '@/components/front/OrderHistory.vue';
import { useCartStore } from '@/stores/cart.store';
import { useUserStore } from '@/stores/user.store';
import { computed, onMounted, ref } from "vue";

const cartStore = useCartStore();
const userStore = useUserStore();
const currentOrderId = computed(() => cartStore.currentOrderId);

// Tab management
const activeTab = ref('products');

const setActiveTab = (tabId: string) => {
  activeTab.value = tabId;
};

onMounted(() => {
  cartStore.loadCart();
})
</script>

<style scoped>
@media (max-width: 991.98px) {
  .sticky-top {
    position: relative;
    top: 0 !important;
  }
}

.nav-tabs .nav-link {
  color: #495057;
}

.nav-tabs .nav-link.active {
  font-weight: 500;
}
</style>
