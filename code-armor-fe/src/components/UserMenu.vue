<template>
  <div class="relative">
    <button
      class="flex items-center gap-2 rounded-full border border-slate-800 bg-slate-900 px-3 py-2 text-sm text-slate-200"
      type="button"
      @click="toggle"
    >
      <span class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-800 text-xs">
        {{ initials }}
      </span>
      <span class="hidden sm:inline">{{ profile?.username || "Account" }}</span>
    </button>

    <div
      v-if="open"
      class="absolute right-0 mt-3 w-48 rounded-2xl border border-slate-800 bg-slate-900 p-2 shadow-xl"
    >
      <button
        class="flex w-full items-center rounded-xl px-3 py-2 text-left text-sm text-slate-200 hover:bg-slate-800"
        type="button"
        @click="goProfile"
      >
        Profile
      </button>
      <button
        class="flex w-full items-center rounded-xl px-3 py-2 text-left text-sm text-slate-200 hover:bg-slate-800"
        type="button"
        @click="logout"
      >
        Sign out
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const open = ref(false);

const profile = computed(() => authStore.profile);
const initials = computed(() => {
  const name = profile.value?.username ?? "CA";
  return name
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
});

const toggle = () => {
  open.value = !open.value;
};

const goProfile = () => {
  open.value = false;
  router.push({ name: "profile" });
};

const logout = () => {
  open.value = false;
  authStore.logout();
  router.push({ name: "home" });
};
</script>
