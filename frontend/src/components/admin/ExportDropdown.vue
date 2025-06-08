<template>
  <Dropdown
    :button-class="buttonClass"
    :disabled="isExporting"
    @open="$emit('open')"
    @close="$emit('close')"
  >
    <template #button>
      <i class="bi bi-download me-1"></i>
      <span v-if="isExporting">
        <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
        Exporting...
      </span>
      <span v-else>Export</span>
    </template>
    
    <template #menu="{ close }">
      <li>
        <button 
          class="dropdown-item" 
          type="button"
          @click="handleExport('csv', close)" 
          :disabled="isExporting"
        >
          <i class="bi bi-filetype-csv me-2"></i>Export as CSV
        </button>
      </li>
      <li>
        <button 
          class="dropdown-item" 
          type="button"
          @click="handleExport('xlsx', close)" 
          :disabled="isExporting"
        >
          <i class="bi bi-filetype-xlsx me-2"></i>Export as Excel
        </button>
      </li>
    </template>
  </Dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Dropdown from '../common/Dropdown.vue';

interface Props {
  isExporting?: boolean;
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';
  outline?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isExporting: false,
  variant: 'success',
  outline: true
});

const emit = defineEmits<{
  export: [fileType: 'csv' | 'xlsx']
  open: []
  close: []
}>();

const buttonClass = computed(() => {
  const baseClass = 'btn dropdown-toggle';
  const variantClass = props.outline ? `btn-outline-${props.variant}` : `btn-${props.variant}`;
  return `${baseClass} ${variantClass}`;
});

const handleExport = (fileType: 'csv' | 'xlsx', closeDropdown: () => void) => {
  closeDropdown();
  emit('export', fileType);
};
</script> 