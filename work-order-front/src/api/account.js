import api from "@/plugins/axios.js";

// 取得目前登入使用者的個人資料

export function getProfile() {
  return api
    .get("/api/account/profile", {
      skipGlobalError: true,
    })
    .then((response) => response.data.data);
}

// 更新目前登入使用者的姓名、Email、電話
export function updateProfile(payload) {
  return api
    .patch("/api/account/profile", payload, {
      skipGlobalError: true,
    })
    .then((response) => response.data.data);
}

// 修改目前登入使用者的密碼

export function changePassword(payload) {
  return api
    .patch("/api/account/password", payload, {
      skipGlobalError: true,
    })
    .then((response) => response.data);
}
