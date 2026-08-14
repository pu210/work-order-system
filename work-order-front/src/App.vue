<script setup>
import { RouterView } from 'vue-router';

</script>

<template>
  <div class="app-root">
    <RouterView></RouterView>
  </div>
</template>

<style>
html,
body,
#app,
.app-root {
  width: 100%;
  min-width: 0;
  min-height: 100%;
  margin: 0;
}

body {
  overflow-x: hidden;
}
</style>
