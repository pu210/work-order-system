import api from "@/plugins/axios.js";

export function getUsers(params) {
  return api.get("/users", { params }).then((res) => res.data.data);
}
export function updateUserStatus(userId, status) {
  return api.patch(`/users/${userId}`, { status }).then((res) => res.data.data);
}

export function createUser(payload) {
  return api.post("/users", payload).then((res) => res.data.data);
}

export function reviewUserRegistration(userId, payload) {
  return api
    .patch(`/users/${userId}/approval`, payload)
    .then((res) => res.data.data);
}
