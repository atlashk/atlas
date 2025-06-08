<template>
  <div class="position-relative" ref="dropdownRef">
    <button 
      :class="buttonClass"
      type="button" 
      @click="toggleDropdown"
      :disabled="disabled"
    >
      <slot name="button">
        {{ buttonText }}
      </slot>
    </button>
    <ul 
      v-show="isOpen" 
      :class="menuClass"
      class="dropdown-menu show position-absolute"
      :style="menuStyle"
    >
      <slot name="menu" :close="closeDropdown"></slot>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';

interface Props {
  buttonText?: string;
  buttonClass?: string;
  menuClass?: string;
  disabled?: boolean;
  placement?: 'bottom-start' | 'bottom-end' | 'top-start' | 'top-end';
}

const props = withDefaults(defineProps<Props>(), {
  buttonText: 'Dropdown',
  buttonClass: 'btn btn-outline-secondary dropdown-toggle',
  menuClass: '',
  disabled: false,
  placement: 'bottom-start'
});

const emit = defineEmits<{
  open: []
  close: []
}>();

const dropdownRef = ref<HTMLElement>();
const isOpen = ref(false);

const menuStyle = computed(() => {
  const styles: Record<string, string> = {
    'z-index': '1000'
  };

  switch (props.placement) {
    case 'bottom-start':
      styles.top = '100%';
      styles.left = '0';
      break;
    case 'bottom-end':
      styles.top = '100%';
      styles.right = '0';
      break;
    case 'top-start':
      styles.bottom = '100%';
      styles.left = '0';
      break;
    case 'top-end':
      styles.bottom = '100%';
      styles.right = '0';
      break;
  }

  return styles;
});

const toggleDropdown = () => {
  if (props.disabled) return;
  
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    emit('open');
  } else {
    emit('close');
  }
};

const closeDropdown = () => {
  isOpen.value = false;
  emit('close');
};

const handleClickOutside = (event: Event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    closeDropdown();
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

// Expose methods for parent component
defineExpose({
  close: closeDropdown,
  open: () => {
    if (!props.disabled) {
      isOpen.value = true;
      emit('open');
    }
  },
  toggle: toggleDropdown,
  isOpen: computed(() => isOpen.value)
});
</script>

<style scoped>
.dropdown-menu.show {
  display: block;
  min-width: 160px;
  background-color: #fff;
  border: 1px solid rgba(0,0,0,.15);
  border-radius: 0.375rem;
  box-shadow: 0 0.5rem 1rem rgba(0,0,0,.175);
}

:deep(.dropdown-item) {
  display: block;
  width: 100%;
  padding: 0.25rem 1rem;
  clear: both;
  font-weight: 400;
  color: #212529;
  text-align: inherit;
  text-decoration: none;
  white-space: nowrap;
  background-color: transparent;
  border: 0;
  cursor: pointer;
}

:deep(.dropdown-item:hover),
:deep(.dropdown-item:focus) {
  color: #1e2125;
  background-color: #e9ecef;
}

:deep(.dropdown-item:disabled) {
  color: #6c757d;
  pointer-events: none;
  background-color: transparent;
}

:deep(.dropdown-divider) {
  height: 0;
  margin: 0.5rem 0;
  overflow: hidden;
  border-top: 1px solid rgba(0,0,0,.15);
}
</style> 