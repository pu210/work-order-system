import Swal from "sweetalert2";

const toast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  didOpen: (element) => {
    element.addEventListener("mouseenter", Swal.stopTimer);
    element.addEventListener("mouseleave", Swal.resumeTimer);
  },
});

export const notify = {
  success(message, options = {}) {
    return toast.fire({
      icon: "success",
      title: message,
      ...options,
    });
  },

  error(message, options = {}) {
    return toast.fire({
      icon: "error",
      title: message,
      ...options,
    });
  },

  warning(message, options = {}) {
    return toast.fire({
      icon: "warning",
      title: message,
      ...options,
    });
  },

  info(message, options = {}) {
    return toast.fire({
      icon: "info",
      title: message,
      ...options,
    });
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
