<template>
  <section class="space-y-8">
    <div class="rounded-3xl border border-slate-900 bg-slate-900/50 p-8">
      <p class="text-xs uppercase tracking-[0.2em] text-slate-500">
        Profile
      </p>
      <h2 class="mt-2 text-2xl font-semibold text-white">
        Personal center
      </h2>
      <p class="mt-2 text-sm text-slate-400">
        Manage your username and review account details.
      </p>

      <div class="mt-6 grid gap-6 md:grid-cols-2">
        <div class="rounded-2xl border border-slate-800 bg-slate-950 p-5">
          <p class="text-xs uppercase tracking-[0.2em] text-slate-500">Email</p>
          <p class="mt-3 text-lg text-white">{{ profile?.email }}</p>
        </div>
        <div class="rounded-2xl border border-slate-800 bg-slate-950 p-5">
          <p class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Current username
          </p>
          <p class="mt-3 text-lg text-white">{{ profile?.username }}</p>
        </div>
      </div>

      <form class="mt-8 max-w-xl space-y-4" @submit.prevent="save">
        <div>
          <label class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Update username
          </label>
          <input
            v-model="username"
            class="mt-2 w-full rounded-xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 focus:border-sky-500 focus:outline-none"
            placeholder="New username"
            required
          />
        </div>
        <button
          class="rounded-xl bg-sky-500 px-6 py-3 text-sm font-semibold text-slate-950 hover:bg-sky-400"
          type="submit"
          :disabled="saving"
        >
          Save changes
        </button>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useAuthStore } from "../stores/auth";

const authStore = useAuthStore();
const saving = ref(false);
const username = ref("");

const profile = computed(() => authStore.profile);

const save = async () => {
  if (!username.value.trim()) return;
  saving.value = true;
  try {
    await authStore.updateUsername(username.value.trim());
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  if (!authStore.profile) {
    await authStore.fetchProfile();
  }
  username.value = authStore.profile?.username ?? "";
});
</script>
