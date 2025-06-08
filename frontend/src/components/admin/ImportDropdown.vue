<template>
  <Dropdown
    :button-class="buttonClass"
    :disabled="isImporting"
    @open="$emit('open')"
    @close="$emit('close')"
  >
    <template #button>
      <i class="bi bi-upload me-1"></i>
      <span v-if="isImporting">
        <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
        Importing...
      </span>
      <span v-else>Import</span>
    </template>
    
    <template #menu="{ close }">
      <li>
        <button 
          class="dropdown-item" 
          type="button"
          @click="handleImport('csv', close)" 
          :disabled="isImporting"
        >
          <i class="bi bi-filetype-csv me-2"></i>Import from CSV
        </button>
      </li>
      <li>
        <button 
          class="dropdown-item" 
          type="button"
          @click="handleImport('xlsx', close)" 
          :disabled="isImporting"
        >
          <i class="bi bi-filetype-xlsx me-2"></i>Import from Excel
        </button>
      </li>
    </template>
  </Dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Dropdown from '../common/Dropdown.vue';

interface Props {
  isImporting?: boolean;
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';
  outline?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isImporting: false,
  variant: 'primary',
  outline: true
});

const emit = defineEmits<{
  import: [fileType: 'csv' | 'xlsx']
  open: []
  close: []
}>();

const buttonClass = computed(() => {
  const baseClass = 'btn dropdown-toggle';
  const variantClass = props.outline ? `btn-outline-${props.variant}` : `btn-${props.variant}`;
  return `${baseClass} ${variantClass}`;
});

const handleImport = (fileType: 'csv' | 'xlsx', closeDropdown: () => void) => {
  closeDropdown();
  emit('import', fileType);
};
</script>
