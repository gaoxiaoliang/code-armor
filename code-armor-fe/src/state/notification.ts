import { reactive } from "vue";

export type NotificationKind = "error" | "success" | "info";

export const notificationState = reactive({
  open: false,
  message: "",
  kind: "info" as NotificationKind
});

let timeoutId: number | undefined;

export const showNotification = (
  message: string,
  kind: NotificationKind = "info"
) => {
  notificationState.open = true;
  notificationState.message = message;
  notificationState.kind = kind;

  if (timeoutId) {
    window.clearTimeout(timeoutId);
  }
  timeoutId = window.setTimeout(() => {
    notificationState.open = false;
  }, 4000);
};

export const hideNotification = () => {
  notificationState.open = false;
};
