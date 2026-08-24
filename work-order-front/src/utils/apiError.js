const STATUS_MESSAGES = {
  400: "請求資料有誤，請檢查輸入內容",
  401: "登入狀態已失效，請重新登入",
  403: "您沒有執行此操作的權限",
  404: "找不到指定的資料",
  409: "資料狀態已變更，請重新整理後再試",
  413: "上傳的檔案過大",
  422: "資料驗證失敗",
  429: "操作過於頻繁，請稍後再試",
  500: "伺服器發生錯誤，請稍後再試",
  502: "目前無法連線至伺服器",
  503: "服務暫時無法使用，請稍後再試",
  504: "伺服器回應逾時，請稍後再試",
};

export function getErrorMessage(error, fallback = "操作失敗，請稍後再試") {
  if (!error) {
    return fallback;
  }

  if (error.code === "ECONNABORTED") {
    return "伺服器回應逾時，請稍後再試";
  }

  if (error.isAxiosError && !error.response) {
    return "無法連線至伺服器，請確認網路連線";
  }

  if (!error.response) {
    return fallback;
  }

  const responseMessage = error.response.data?.message;

  if (
    typeof responseMessage === "string" &&
    responseMessage.trim().length > 0
  ) {
    return responseMessage;
  }

  return STATUS_MESSAGES[error.response.status] || fallback;
}

export function getErrorStatus(error) {
  return error?.response?.status ?? null;
}

export function getErrorCode(error) {
  return error?.response?.data?.code ?? null;
}
