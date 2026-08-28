import api from "@/plugins/axios.js";

export function getUsers(params) {
  return api
    .get("/api/users", {
      params,
      skipGlobalError: true,
      skipForbiddenRedirect: true,
    })
    .then((res) => res.data.data);
}
export function updateUserStatus(userId, status) {
  return api
    .patch(`/api/users/${userId}`, { status }, { skipGlobalError: true })
    .then((res) => res.data.data);
}

export function createUser(payload) {
  return api.post("/api/users", payload).then((res) => res.data.data);
}

export function reviewUserRegistration(userId, payload) {
  return api
    .patch(`/api/users/${userId}/approval`, payload, {
      skipGlobalError: true,
    })
    .then((res) => res.data.data);
}
export function getUser(userId) {
  return api.get(`/api/users/${userId}`).then((res) => res.data.data);
}

export function updateUser(userId, payload) {
  return api
    .patch(`/api/users/${userId}`, payload)
    .then((res) => res.data.data);
}
