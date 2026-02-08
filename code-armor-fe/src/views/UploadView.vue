<template>
  <section class="space-y-8">
    <div class="rounded-3xl border border-slate-900 bg-slate-900/50 p-8">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <p class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Upload package
          </p>
          <h2 class="mt-2 text-2xl font-semibold text-white">
            Send your build for security scanning.
          </h2>
          <p class="mt-2 text-sm text-slate-400">
            Accepts zip or tar archives with React, Vue, Angular, or static
            builds.
          </p>
        </div>
        <div class="flex flex-wrap gap-3">
          <button
            class="rounded-xl border border-slate-700 px-4 py-2 text-sm text-slate-200 hover:border-slate-500"
            type="button"
            @click="triggerFileSelect"
          >
            Choose file
          </button>
          <button
            class="rounded-xl bg-sky-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-sky-400"
            type="button"
            :disabled="!selectedFile || uploading"
            @click="upload"
          >
            Upload now
          </button>
        </div>
      </div>

      <div
        class="mt-6 flex flex-col items-center justify-center gap-4 rounded-2xl border border-dashed border-slate-700 bg-slate-950/60 px-6 py-10 text-center"
        @dragover.prevent
        @drop.prevent="handleDrop"
      >
        <p class="text-sm text-slate-300">
          Drag and drop your archive here, or use the button above.
        </p>
        <p class="text-xs text-slate-500">
          {{ selectedFile?.name || "No file selected" }}
        </p>
      </div>

      <div v-if="uploading" class="mt-6">
        <div class="flex items-center justify-between text-xs text-slate-500">
          <span>Uploading</span>
          <span>{{ progress }}%</span>
        </div>
        <div class="mt-2 h-2 rounded-full bg-slate-800">
          <div
            class="h-2 rounded-full bg-sky-500 transition-all"
            :style="{ width: `${progress}%` }"
          ></div>
        </div>
      </div>
      <input
        ref="fileInput"
        class="hidden"
        type="file"
        @change="handleFileChange"
      />
    </div>

    <div class="rounded-3xl border border-slate-900 bg-slate-900/50 p-8">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-xs uppercase tracking-[0.2em] text-slate-500">
            Your uploads
          </p>
          <h3 class="mt-2 text-xl font-semibold text-white">
            Uploaded packages
          </h3>
        </div>
        <button
          class="rounded-xl border border-slate-700 px-4 py-2 text-sm text-slate-200 hover:border-slate-500"
          type="button"
          @click="fetchFiles"
        >
          Refresh
        </button>
      </div>

      <div class="mt-6 overflow-x-auto">
        <table class="w-full text-left text-sm text-slate-300">
          <thead class="text-xs uppercase tracking-[0.2em] text-slate-500">
            <tr>
              <th class="pb-3">File name</th>
              <th class="pb-3">Size</th>
              <th class="pb-3">Uploaded</th>
              <th class="pb-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="files.length === 0">
              <td class="py-4" colspan="4">No uploads yet.</td>
            </tr>
            <tr
              v-for="file in files"
              :key="file.id"
              class="border-t border-slate-800"
            >
              <td class="py-4 font-medium text-slate-100">
                {{ file.fileName }}
              </td>
              <td class="py-4">{{ formatSize(file.size) }}</td>
              <td class="py-4">{{ formatDate(file.uploadedAt) }}</td>
              <td class="py-4">
                <button
                  class="rounded-lg border border-rose-500/50 px-3 py-1 text-xs text-rose-200 hover:border-rose-400"
                  type="button"
                  @click="deleteFile(file.id)"
                >
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-6 flex items-center justify-between text-sm text-slate-400">
        <p>Page {{ pageNo }} of {{ totalPages }}</p>
        <div class="flex gap-2">
          <button
            class="rounded-lg border border-slate-700 px-3 py-1 hover:border-slate-500"
            type="button"
            :disabled="pageNo === 1"
            @click="changePage(pageNo - 1)"
          >
            Previous
          </button>
          <button
            class="rounded-lg border border-slate-700 px-3 py-1 hover:border-slate-500"
            type="button"
            :disabled="pageNo === totalPages"
            @click="changePage(pageNo + 1)"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import api from "../services/api";
import { showNotification } from "../state/notification";

interface FileItem {
  id: number;
  fileName: string;
  size: number;
  uploadedAt: string;
}

const fileInput = ref<HTMLInputElement | null>(null);
const selectedFile = ref<File | null>(null);
const uploading = ref(false);
const progress = ref(0);
const files = ref<FileItem[]>([]);
const pageNo = ref(1);
const pageSize = ref(6);
const total = ref(0);

const totalPages = computed(() =>
  Math.max(1, Math.ceil(total.value / pageSize.value))
);

const triggerFileSelect = () => {
  fileInput.value?.click();
};

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  selectedFile.value = target.files?.[0] ?? null;
};

const handleDrop = (event: DragEvent) => {
  selectedFile.value = event.dataTransfer?.files?.[0] ?? null;
};

const upload = async () => {
  if (!selectedFile.value) {
    showNotification("Select a file before uploading.", "info");
    return;
  }
  uploading.value = true;
  progress.value = 0;

  const formData = new FormData();
  formData.append("file", selectedFile.value);

  try {
    await api.post("/api/file", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      onUploadProgress: (event) => {
        if (event.total) {
          progress.value = Math.round((event.loaded / event.total) * 100);
        }
      }
    });
    showNotification("Upload completed.", "success");
    selectedFile.value = null;
    await fetchFiles();
  } finally {
    uploading.value = false;
  }
};

const fetchFiles = async () => {
  const response = await api.get("/api/file", {
    params: { pageNo: pageNo.value, pageSize: pageSize.value }
  });
  files.value = response.data.data;
  total.value = response.data.total;
};

const deleteFile = async (id: number) => {
  await api.delete(`/api/file/${id}`);
  showNotification("File removed.", "success");
  await fetchFiles();
};

const changePage = async (page: number) => {
  pageNo.value = page;
  await fetchFiles();
};

const formatSize = (size: number) => {
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  if (size < 1024 * 1024 * 1024)
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`;
};

const formatDate = (value: string) => {
  const date = new Date(value);
  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(date);
};

onMounted(fetchFiles);
</script>
