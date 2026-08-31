import Swal from "sweetalert2";

const createToastConfig = (icon, message, options = {}) => {
  const { onClick, didOpen, ...restOptions } = options;
  return {
    icon,
    title: message,
    ...restOptions,
    didOpen: (element) => {
      element.addEventListener("mouseenter", Swal.stopTimer);
      element.addEventListener("mouseleave", Swal.resumeTimer);

      // 強制整個小浮窗與內部所有文字/圖示游標皆呈現手指點擊圖示 (cursor: pointer)
      element.style.setProperty("cursor", "pointer", "important");
      const children = element.querySelectorAll("*");
      children.forEach((child) => {
        child.style.setProperty("cursor", "pointer", "important");
        child.style.setProperty("user-select", "none", "important");
      });

      if (typeof onClick === "function") {
        element.addEventListener("click", (e) => {
          Swal.close();
          onClick(e);
        });
      }
      if (typeof didOpen === "function") {
        didOpen(element);
      }
    },
  };
};

const toast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 4000,
  timerProgressBar: true,
});

export const notify = {
  success(message, options = {}) {
    return toast.fire(createToastConfig("success", message, options));
  },

  error(message, options = {}) {
    return toast.fire(createToastConfig("error", message, options));
  },

  warning(message, options = {}) {
    return toast.fire(createToastConfig("warning", message, options));
  },

  info(message, options = {}) {
    return toast.fire(createToastConfig("info", message, options));
  },

  confirm({
    title = "確定要執行嗎？",
    text = "",
    confirmButtonText = "確定",
    cancelButtonText = "取消",
    icon = "warning",
  } = {}) {
    return Swal.fire({
      icon,
      title,
      text,
      showCancelButton: true,
      confirmButtonText,
      cancelButtonText,
      reverseButtons: true,
      focusCancel: true,
    });
  },

  alert({ title, text = "", icon = "info", confirmButtonText = "知道了" }) {
    return Swal.fire({
      icon,
      title,
      text,
      confirmButtonText,
    });
  },
};
