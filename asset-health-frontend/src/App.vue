<template>
  <div>
    <header class="topbar">
      <div class="brand">
        <div class="logo">AH</div>
        <div>
          <div class="brand-title">Asset Health Management</div>
          <div class="brand-sub">Interactive dashboard prototype</div>
        </div>
      </div>
      <div class="top-actions">
        <button class="btn secondary" @click="handleExport">Export</button>
        <button class="btn">Edit dashboard</button>
      </div>
    </header>
    <div class="shell">
      <aside class="sidebar">
        <div class="nav-label">Dashboards</div>
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: $route.path === item.path }"
        >
          <span class="nav-icon" v-html="item.icon"></span>{{ item.label }}
        </router-link>

      </aside>
      <main class="main">
        <router-view />
        <div class="footer-note">Asset Health Management Dashboard &mdash; Vue.js + Spring Boot</div>
      </main>
    </div>
  </div>
</template>

<script setup>
const navItems = [
  { path: '/overview', label: 'Asset & Capacity Overview', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
  { path: '/transformer', label: 'Transformer Fleet', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z"/></svg>' },
  { path: '/quality', label: 'Asset Data Quality', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>' },
]

function handleExport() {
  window.print()
}
</script>
