<template>
  <section class="grid gap-10 lg:grid-cols-[1.1fr_0.9fr]">
    <div class="space-y-8">
      <div class="inline-flex items-center gap-2 rounded-full border border-slate-800 bg-slate-900/60 px-4 py-2 text-xs uppercase tracking-[0.2em] text-slate-400">
        Secure analysis · Continuous delivery
      </div>
      <h1 class="text-4xl font-semibold leading-tight text-white md:text-5xl">
        Launch safer front-end builds with Code Armor.
      </h1>
      <p class="text-lg text-slate-300">
        Upload any HTML, CSS, JavaScript, or framework bundle. We scan it for
        security gaps, improvement opportunities, and deployment readiness — then
        publish to the cloud when it is approved.
      </p>
      <div class="flex flex-wrap gap-4">
        <button
          class="rounded-xl bg-sky-500 px-6 py-3 text-sm font-semibold text-slate-950 transition hover:bg-sky-400"
          type="button"
          @click="openModal('login')"
        >
          Sign in
        </button>
        <button
          class="rounded-xl border border-slate-700 px-6 py-3 text-sm font-semibold text-slate-200 transition hover:border-slate-500"
          type="button"
          @click="openModal('register')"
        >
          Create account
        </button>
      </div>
      <div class="grid gap-4 md:grid-cols-3">
        <div class="rounded-2xl border border-slate-900 bg-slate-900/60 p-4">
          <p class="text-sm font-semibold text-white">Deep package inspection</p>
          <p class="mt-2 text-xs text-slate-400">
            Analyze static files, framework bundles, and build outputs.
          </p>
        </div>
        <div class="rounded-2xl border border-slate-900 bg-slate-900/60 p-4">
          <p class="text-sm font-semibold text-white">Actionable findings</p>
          <p class="mt-2 text-xs text-slate-400">
            Know what to fix, why it matters, and how to improve fast.
          </p>
        </div>
        <div class="rounded-2xl border border-slate-900 bg-slate-900/60 p-4">
          <p class="text-sm font-semibold text-white">Ready-to-share hosting</p>
          <p class="mt-2 text-xs text-slate-400">
            Publish on cloud platforms as soon as your build passes.
          </p>
        </div>
      </div>
    </div>
    <div class="rounded-3xl border border-slate-900 bg-gradient-to-br from-slate-900 via-slate-950 to-slate-950 p-8">
      <div class="space-y-6">
        <div class="rounded-2xl border border-slate-800 bg-slate-950 p-5">
          <p class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Vision
          </p>
          <h2 class="mt-3 text-xl font-semibold text-white">
            One secure pipeline for every front-end release.
          </h2>
          <p class="mt-3 text-sm text-slate-400">
            Code Armor connects upload, detection, and deployment into a single
            experience so teams can ship faster with less risk.
          </p>
        </div>
        <div class="rounded-2xl border border-slate-800 bg-slate-950 p-5">
          <p class="text-sm text-slate-300">
            Supported bundles include React, Vue, Angular, and vanilla
            front-end stacks. Drag and drop, review insights, and deliver with
            confidence.
          </p>
        </div>
        <div class="rounded-2xl border border-slate-800 bg-slate-950 p-5">
          <div class="flex items-center justify-between text-xs text-slate-500">
            <span>Security pipeline</span>
            <span>Always-on</span>
          </div>
          <div class="mt-4 h-2 rounded-full bg-slate-800">
            <div class="h-2 w-4/5 rounded-full bg-sky-500"></div>
          </div>
          <p class="mt-3 text-xs text-slate-400">
            Automate verification and publish trusted artifacts.
          </p>
        </div>
      </div>
    </div>
  </section>

  <AuthModal
    :open="modalOpen"
    :mode="modalMode"
    @close="modalOpen = false"
    @switch="handleSwitch"
  />
</template>

<script setup lang="ts">
import { ref } from "vue";
import AuthModal from "../components/AuthModal.vue";

const modalOpen = ref(false);
const modalMode = ref<"login" | "register">("login");

const openModal = (mode: "login" | "register") => {
  modalMode.value = mode;
  modalOpen.value = true;
};

const handleSwitch = (mode: "login" | "register") => {
  modalMode.value = mode;
};
</script>
