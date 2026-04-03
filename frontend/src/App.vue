<template>
  <van-config-provider :theme="theme" class="app-root">
    <router-view />
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ConfigProviderTheme } from 'vant'

const theme = ref<ConfigProviderTheme>('light')
</script>

<style>
.app-root {
  height: 100vh;
  width: 100vw;
  background-color: var(--van-background-2);
}
</style>
