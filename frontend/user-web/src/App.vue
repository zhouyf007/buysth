<template>
  <div class="app-shell">
    <AppHeader />
    <main class="app-main">
      <router-view />
    </main>
    <footer class="app-footer">
      <div class="footer-inner">
        <strong>数码商城</strong>
        <span>全链路微服务演示系统 · Vue3 + Spring Cloud</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

