<template>
  <div class="min-h-screen bg-slate-950">
    <header class="border-b border-slate-900">
      <div
        class="mx-auto flex max-w-6xl items-center justify-between px-6 py-4"
      >
        <div class="flex items-center gap-3">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-2xl bg-sky-500/10 text-lg font-semibold text-sky-300"
          >
            CA
          </div>
          <div>
            <p class="text-sm font-semibold text-slate-100">Code Armor</p>
            <p class="text-xs text-slate-500">Secure delivery for web builds</p>
          </div>
        </div>
        <nav class="flex items-center gap-4 text-sm text-slate-300">
          <RouterLink class="hover:text-white" to="/">Home</RouterLink>
          <RouterLink
            v-if="isAuthenticated"
            class="hover:text-white"
            to="/upload"
          >
            Upload
          </RouterLink>
          <UserMenu v-if="isAuthenticated" />
        </nav>
      </div>
    </header>

    <main class="mx-auto w-full max-w-6xl px-6 py-10">
      <RouterView />
    </main>
    <NotificationToast />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { RouterLink, RouterView } from "vue-router";
import NotificationToast from "./components/NotificationToast.vue";
import UserMenu from "./components/UserMenu.vue";
import { useAuthStore } from "./stores/auth";

const authStore = useAuthStore();
const isAuthenticated = computed(() => authStore.isAuthenticated);

onMounted(async () => {
  if (authStore.isAuthenticated && !authStore.profile) {
    await authStore.fetchProfile();
  }
});
</script>
