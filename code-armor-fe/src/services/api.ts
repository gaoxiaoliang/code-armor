import axios from "axios";
import { showNotification } from "../state/notification";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
  timeout: 15000
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("code-armor-token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    if (response.status !== 200) {
      showNotification("Request failed.", "error");
      return Promise.reject(new Error("Unexpected response"));
    }
    return response;
  },
  (error) => {
    const message =
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.message ||
      "Request failed.";
    showNotification(message, "error");
    if (error?.response?.status === 401) {
      localStorage.removeItem("code-armor-token");
    }
    return Promise.reject(error);
  }
);

export default api;
