<template>
  <div>
    <NavBar />
    <main><router-view /></main>
  </div>
</template>

<script setup lang="ts">
import NavBar from './NavBar.vue';
// Removed profile fetching from here since it's handled by router navigation guard
</script>
