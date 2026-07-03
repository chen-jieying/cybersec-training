import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
});

export function loginUser(credentials: { username: string; password: string; role: string }) {
  return api.post('/auth/login', credentials);
}

export function fetchRadarData() {
  return api.get('/statistics/radar');
}

export function sendChatMessage(message: string) {
  return api.post('/chat', { message });
}

export function fetchUsers() {
  return api.get('/users');
}

export function exportUsers() {
  return api.get('/users/export', { responseType: 'blob' });
}

export function importUsers(file: File) {
  const data = new FormData();
  data.append('file', file);
  return api.post('/users/import', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

export function previewPdf() {
  return api.get('/resource/pdf', { responseType: 'blob' });
}
