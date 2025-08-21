import { defineStore } from 'pinia';

export const useFlashStore = defineStore('flash', {
  state: () => ({
    successMessage: '' as string,
    errorMessage: '' as string,
  }),
  actions: {
    setSuccess(msg: string) {
      this.successMessage = msg;
      setTimeout(() => this.successMessage = '', 5000); // auto-clear after 5s
    },
    setError(msg: string) {
      this.errorMessage = msg;
      setTimeout(() => this.errorMessage = '', 5000); // auto-clear after 5s
    },
    clear() {
      this.successMessage = '';
      this.errorMessage = '';
    },
  },
});
