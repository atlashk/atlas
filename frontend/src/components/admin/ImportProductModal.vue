<template>
  <div class="modal fade" id="importProductModal" tabindex="-1" aria-labelledby="importProductModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="importProductModalLabel">
            <i class="bi bi-upload me-2"></i>Import Products
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <!-- File Type Selection -->
          <div class="mb-4">
            <label class="form-label fw-bold">Select File Type</label>
            <div class="d-flex gap-3">
              <div class="form-check">
                <input
                  class="form-check-input"
                  type="radio"
                  name="fileType"
                  id="csvType"
                  value="csv"
                  v-model="selectedFileType"
                >
                <label class="form-check-label" for="csvType">
                  <i class="bi bi-filetype-csv me-1"></i>CSV File
                </label>
              </div>
              <div class="form-check">
                <input
                  class="form-check-input"
                  type="radio"
                  name="fileType"
                  id="xlsxType"
                  value="xlsx"
                  v-model="selectedFileType"
                >
                <label class="form-check-label" for="xlsxType">
                  <i class="bi bi-filetype-xlsx me-1"></i>Excel File
                </label>
              </div>
            </div>
          </div>

          <!-- File Upload Area -->
          <div class="mb-4">
            <label class="form-label fw-bold">Upload File</label>
            <div 
              class="border border-2 border-dashed rounded p-4 text-center"
              :class="{
                'border-primary bg-light': isDragOver,
                'border-secondary': !isDragOver
              }"
              @dragover.prevent="isDragOver = true"
              @dragleave.prevent="isDragOver = false"
              @drop.prevent="handleFileDrop"
            >
              <div v-if="!selectedFile">
                <i class="bi bi-cloud-upload fs-1 text-muted mb-3"></i>
                <p class="mb-2">Drag and drop your file here, or</p>
                <button type="button" class="btn btn-outline-primary" @click="fileInput?.click()">
                  Choose File
                </button>
                <p class="small text-muted mt-2">
                  Supported formats: {{ selectedFileType?.toUpperCase() || 'CSV, XLSX' }}
                </p>
              </div>
              <div v-else class="d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center">
                  <i class="bi bi-file-earmark-text fs-4 text-primary me-2"></i>
                  <div class="text-start">
                    <div class="fw-medium">{{ selectedFile.name }}</div>
                    <div class="small text-muted">{{ formatFileSize(selectedFile.size) }}</div>
                  </div>
                </div>
                <button type="button" class="btn btn-outline-danger btn-sm" @click="removeFile">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
            <input
              ref="fileInput"
              type="file"
              class="d-none"
              :accept="fileAccept"
              @change="handleFileSelect"
            >
          </div>

          <!-- Template Download -->
          <div class="alert alert-info">
            <i class="bi bi-info-circle me-2"></i>
            <strong>Need a template?</strong> Download a sample file to see the required format.
            <div class="mt-2">
              <button 
                type="button" 
                class="btn btn-sm btn-outline-info me-2"
                @click="downloadTemplate('csv')"
                :disabled="isDownloading"
              >
                <i class="bi bi-download me-1"></i>CSV Template
              </button>
              <button 
                type="button" 
                class="btn btn-sm btn-outline-info"
                @click="downloadTemplate('xlsx')"
                :disabled="isDownloading"
              >
                <i class="bi bi-download me-1"></i>Excel Template
              </button>
            </div>
          </div>

          <!-- Import Progress -->
          <div v-if="isImporting" class="mb-3">
            <div class="d-flex align-items-center mb-2">
              <div class="spinner-border spinner-border-sm me-2" role="status"></div>
              <span>Importing products...</span>
            </div>
            <div class="progress">
              <div class="progress-bar progress-bar-striped progress-bar-animated" style="width: 100%"></div>
            </div>
          </div>

          <!-- Import Results -->
          <div v-if="importResult" class="alert" :class="importResult.success ? 'alert-success' : 'alert-danger'">
            <div v-if="importResult.success">
              <i class="bi bi-check-circle me-2"></i>
              <strong>Import Successful!</strong>
              Products imported successfully.
            </div>
            <div v-else>
              <i class="bi bi-exclamation-triangle me-2"></i>
              <strong>Import Failed!</strong>
              {{ importResult.errorMessage }}
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" :disabled="isImporting">
            Cancel
          </button>
          <button 
            type="button" 
            class="btn btn-primary" 
            @click="handleImport"
            :disabled="!selectedFile || !selectedFileType || isImporting"
          >
            <span v-if="isImporting">
              <span class="spinner-border spinner-border-sm me-1" role="status"></span>
              Importing...
            </span>
            <span v-else>
              <i class="bi bi-upload me-1"></i>Import Products
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { FileType } from '@/interfaces/product.interface';
import { productService } from '@/services';
import { toast } from 'vue3-toastify';

interface ImportResult {
  success: boolean
  errorMessage?: string
}

const emit = defineEmits<{
  imported: []
}>();

const selectedFileType = ref<'csv' | 'xlsx'>('csv');
const selectedFile = ref<File | null>(null);
const isDragOver = ref(false);
const isImporting = ref(false);
const isDownloading = ref(false);
const importResult = ref<ImportResult | null>(null);

// Add explicit type for the file input ref
const fileInput = ref<HTMLInputElement>();

const fileAccept = computed(() => {
  if (selectedFileType.value === 'csv') return '.csv';
  if (selectedFileType.value === 'xlsx') return '.xlsx,.xls';
  return '.csv,.xlsx,.xls';
});

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  console.log('File input event:', event);
  console.log('Files from input:', target.files);
  
  if (target.files && target.files[0]) {
    const file = target.files[0];
    console.log('Selected file:', file);
    console.log('File properties:', {
      name: file.name,
      size: file.size,
      type: file.type,
      lastModified: file.lastModified
    });
    
    selectedFile.value = file;
    validateFile();
  } else {
    console.log('No file selected or files array is empty');
  }
};

const handleFileDrop = (event: DragEvent) => {
  isDragOver.value = false;
  console.log('File drop event:', event);
  console.log('DataTransfer files:', event.dataTransfer?.files);
  
  if (event.dataTransfer?.files && event.dataTransfer.files[0]) {
    const file = event.dataTransfer.files[0];
    console.log('Dropped file:', file);
    console.log('File properties:', {
      name: file.name,
      size: file.size,
      type: file.type,
      lastModified: file.lastModified
    });
    
    selectedFile.value = file;
    validateFile();
  } else {
    console.log('No file dropped or files array is empty');
  }
};

const validateFile = () => {
  if (!selectedFile.value) return;
  
  const fileName = selectedFile.value.name.toLowerCase();
  const isValidType = selectedFileType.value === 'csv' 
    ? fileName.endsWith('.csv')
    : fileName.endsWith('.xlsx') || fileName.endsWith('.xls');
    
  if (!isValidType) {
    toast.error(`Please select a valid ${selectedFileType.value.toUpperCase()} file`);
    selectedFile.value = null;
  }
};

const removeFile = () => {
  selectedFile.value = null;
  importResult.value = null;
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const downloadTemplate = async (fileType: 'csv' | 'xlsx') => {
  isDownloading.value = true;
  try {
    // Create template data with headers and sample row
    const templateData = [
      ['Name', 'Price', 'Quantity', 'Status', 'Available From', 'Active', 'Description', 'Attribute Name 1', 'Attribute Value 1', 'Attribute Name 2', 'Attribute Value 2', 'Attribute Name 3', 'Attribute Value 3', 'Brand ID', 'Category IDs'],
      ['Sample Product', '29.99', '100', 'IN_STOCK', '2024-01-01 00:00:00', 'true', 'Sample product description', 'Color', 'Red', 'Size', 'Large', 'Material', 'Cotton', '1', '1|2|3']
    ];
    
    const csvContent = templateData.map(row => 
      row.map(cell => `"${cell.replace(/"/g, '""')}"`).join(',')
    ).join('\n');
    
    const blob = new Blob([csvContent], { 
      type: fileType === 'csv' ? 'text/csv;charset=utf-8;' : 'application/vnd.ms-excel' 
    });
    
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `product-import-template.${fileType}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    toast.success(`${fileType.toUpperCase()} template downloaded successfully`);
  } catch (error) {
    toast.error('Failed to download template');
  } finally {
    isDownloading.value = false;
  }
};

// Add modal close functionality
const closeModal = () => {
  const modalElement = document.getElementById('importProductModal');
  if (modalElement) {
    modalElement.classList.remove('show');
    modalElement.style.display = 'none';
    modalElement.removeAttribute('aria-modal');
    modalElement.removeAttribute('role');
    document.body.classList.remove('modal-open');
    
    // Remove backdrop
    const backdrop = document.getElementById('modal-backdrop');
    if (backdrop) {
      backdrop.remove();
    }
    
    // Reset state
    resetState();
  }
};

// Reset state when modal is hidden
const resetState = () => {
  selectedFile.value = null;
  importResult.value = null;
  isDragOver.value = false;
  isImporting.value = false;
  selectedFileType.value = 'csv';
};

// Setup close button listeners
onMounted(() => {
  const modalElement = document.getElementById('importProductModal');
  if (modalElement) {
    // Close button and cancel button listeners
    const closeButtons = modalElement.querySelectorAll('[data-bs-dismiss="modal"]');
    closeButtons.forEach(button => {
      button.addEventListener('click', closeModal);
    });
    
    // Backdrop click listener
    modalElement.addEventListener('click', (event) => {
      if (event.target === modalElement) {
        closeModal();
      }
    });
    
    // Escape key listener
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && modalElement.classList.contains('show')) {
        closeModal();
      }
    };
    document.addEventListener('keydown', handleEscape);
    
    // Cleanup on unmount
    onUnmounted(() => {
      document.removeEventListener('keydown', handleEscape);
    });
  }
});

// Update handleImport to not auto-close on success
const handleImport = async () => {
  if (!selectedFile.value) return;
  
  isImporting.value = true;
  importResult.value = null;
  
  try {
    // Auto-detect file type from file extension
    const fileName = selectedFile.value.name.toLowerCase();
    const fileType = fileName.endsWith('.csv') ? FileType.CSV : FileType.EXCEL;
    
    console.log('Importing file:', selectedFile.value.name, 'Type:', fileType);
    
    await productService.importProduct(selectedFile.value, fileType);
    
    importResult.value = {
      success: true
    };

    toast.success('Products imported successfully!');
    emit('imported');
  } catch (error: any) {
    importResult.value = {
      success: false,
      errorMessage: error.response?.data?.errorMessage || 'Failed to import products'
    };
    toast.error('Failed to import products');
  } finally {
    isImporting.value = false;
  }
};
</script>

<style scoped>
.border-dashed {
  border-style: dashed !important;
}

.progress {
  height: 8px;
}
</style> 