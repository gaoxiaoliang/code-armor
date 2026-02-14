<template>
  <transition name="fade">
    <div
      v-if="notificationState.open"
      class="fixed right-6 top-6 z-50 w-[320px] rounded-2xl border border-slate-700 bg-slate-900/90 p-4 shadow-2xl"
      :class="kindClass"
      role="alert"
    >
      <div class="flex items-start justify-between gap-3">
        <div>
          <p class="text-sm font-semibold text-slate-100">{{ title }}</p>
          <p class="mt-1 text-sm text-slate-300">{{ notificationState.message }}</p>
        </div>
        <button
          class="text-slate-400 hover:text-slate-200"
          type="button"
          @click="hideNotification"
        >
          ✕
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed } from "vue";
import {
  hideNotification,
  notificationState
} from "../state/notification";

const kindClass = computed(() => {
  switch (notificationState.kind) {
    case "success":
      return "border-emerald-500/50";
    case "error":
      return "border-rose-500/50";
    default:
      return "border-sky-500/50";
  }
});

const title = computed(() => {
  switch (notificationState.kind) {
    case "success":
      return "Success";
    case "error":
      return "Something went wrong";
    default:
      return "Notice";
  }
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
