import { defineStore } from "pinia";
import api from "../services/api";
import { showNotification } from "../state/notification";

interface UserProfile {
  username: string;
  email: string;
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("code-armor-token") || "",
    profile: null as UserProfile | null,
    loading: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token)
  },
  actions: {
    async register(payload: { email: string; username: string; password: string }) {
      this.loading = true;
      try {
        await api.post("/api/auth/register", payload);
        showNotification("Account created. Please log in.", "success");
      } finally {
        this.loading = false;
      }
    },
    async login(payload: { email: string; password: string }) {
      this.loading = true;
      try {
        const response = await api.post("/api/auth/login", payload);
        const token = response.data?.token as string | undefined;
        if (!token) {
          throw new Error("Missing token");
        }
        this.token = token;
        localStorage.setItem("code-armor-token", token);
        await this.fetchProfile();
      } finally {
        this.loading = false;
      }
    },
    async fetchProfile() {
      const response = await api.get("/api/user/me");
      this.profile = {
        username: response.data.username,
        email: response.data.email
      };
    },
    async updateUsername(username: string) {
      const response = await api.put("/api/user/me", { username });
      this.profile = {
        username: response.data.username,
        email: response.data.email
      };
      showNotification("Profile updated.", "success");
    },
    logout() {
      this.token = "";
      this.profile = null;
      localStorage.removeItem("code-armor-token");
    }
  }
});
