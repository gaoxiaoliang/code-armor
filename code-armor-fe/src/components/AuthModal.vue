<template>
  <div
    v-if="open"
    class="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/80 px-4 py-8"
  >
    <div
      class="w-full max-w-lg rounded-3xl border border-slate-800 bg-slate-900 p-8 shadow-2xl"
    >
      <div class="flex items-start justify-between">
        <div>
          <p class="text-xs uppercase tracking-[0.2em] text-slate-500">
            {{ modeLabel }}
          </p>
          <h2 class="mt-3 text-2xl font-semibold text-white">
            {{ headline }}
          </h2>
          <p class="mt-2 text-sm text-slate-400">
            {{ subheadline }}
          </p>
        </div>
        <button
          class="text-slate-400 hover:text-slate-200"
          type="button"
          @click="close"
        >
          ✕
        </button>
      </div>

      <form class="mt-6 space-y-4" @submit.prevent="handleSubmit">
        <div v-if="mode === 'register'">
          <label class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Username
          </label>
          <input
            v-model="form.username"
            class="mt-2 w-full rounded-xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 focus:border-sky-500 focus:outline-none"
            placeholder="Choose a display name"
            required
          />
        </div>
        <div>
          <label class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Email
          </label>
          <input
            v-model="form.email"
            type="email"
            class="mt-2 w-full rounded-xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 focus:border-sky-500 focus:outline-none"
            placeholder="name@company.com"
            required
          />
        </div>
        <div>
          <label class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Password
          </label>
          <input
            v-model="form.password"
            type="password"
            class="mt-2 w-full rounded-xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 focus:border-sky-500 focus:outline-none"
            placeholder="Minimum 8 characters"
            required
          />
        </div>

        <button
          class="mt-4 flex w-full items-center justify-center rounded-xl bg-sky-500 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-sky-400"
          type="submit"
          :disabled="loading"
        >
          {{ actionLabel }}
        </button>
      </form>

      <div class="mt-6 text-center text-sm text-slate-400">
        <span>{{ togglePrompt }}</span>
        <button
          class="ml-2 font-semibold text-sky-400 hover:text-sky-300"
          type="button"
          @click="toggle"
        >
          {{ toggleLabel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const props = defineProps<{ open: boolean; mode: "login" | "register" }>();
const emit = defineEmits<{
  (event: "close"): void;
  (event: "switch", mode: "login" | "register"): void;
}>();

const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  email: "",
  password: "",
  username: ""
});

const loading = computed(() => authStore.loading);
const modeLabel = computed(() =>
  props.mode === "login" ? "Access" : "Get started"
);
const headline = computed(() =>
  props.mode === "login"
    ? "Continue your security review"
    : "Create your Code Armor workspace"
);
const subheadline = computed(() =>
  props.mode === "login"
    ? "Sign in to upload code packages and review security insights."
    : "Start with your email and a secure password."
);
const actionLabel = computed(() =>
  props.mode === "login" ? "Sign in" : "Create account"
);
const togglePrompt = computed(() =>
  props.mode === "login"
    ? "New to Code Armor?"
    : "Already have access?"
);
const toggleLabel = computed(() =>
  props.mode === "login" ? "Create an account" : "Sign in"
);

const handleSubmit = async () => {
  if (props.mode === "login") {
    await authStore.login({
      email: form.email,
      password: form.password
    });
    emit("close");
    router.push({ name: "upload" });
    return;
  }

  await authStore.register({
    email: form.email,
    password: form.password,
    username: form.username
  });
  emit("switch", "login");
};

const close = () => emit("close");
const toggle = () =>
  emit("switch", props.mode === "login" ? "register" : "login");
</script>
