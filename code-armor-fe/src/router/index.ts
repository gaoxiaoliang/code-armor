import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import UploadView from "../views/UploadView.vue";
import ProfileView from "../views/ProfileView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "home", component: HomeView },
    { path: "/upload", name: "upload", component: UploadView },
    { path: "/profile", name: "profile", component: ProfileView }
  ]
});

router.beforeEach((to) => {
  const token = localStorage.getItem("code-armor-token");
  if ((to.name === "upload" || to.name === "profile") && !token) {
    return { name: "home" };
  }
  return true;
});

export default router;
