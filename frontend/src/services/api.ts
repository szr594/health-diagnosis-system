import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

export const userApi = {
  login: (data: { username: string; password: string }) =>
    api.post('/user/login', data),

  register: (data: {
    username: string;
    password: string;
    nickname?: string;
    phone?: string;
    gender?: number;
    age?: number;
  }) => api.post('/user/register', data),

  getInfo: () => api.get('/user/info'),

  updateProfile: (data: {
    nickname?: string;
    realName?: string;
    phone?: string;
    gender?: number;
    age?: number;
    height?: number;
    weight?: number;
    allergyHistory?: string;
    medicalHistory?: string;
  }) => api.post('/user/update', data),
};

export const consultationApi = {
  preDiagnosis: (data: {
    sessionId?: string;
    symptomDescription: string;
    symptomDuration?: string;
    age?: number;
    gender?: string;
    medicalHistory?: string;
    allergyHistory?: string;
  }) => api.post('/medical/ai/pre-diagnosis', data),

  list: (pageNum = 1, pageSize = 10) =>
    api.get('/medical/consultation/list', { params: { pageNum, pageSize } }),

  detail: (id: number) => api.get(`/medical/consultation/detail/${id}`),

  delete: (id: number) => api.delete(`/medical/consultation/${id}`),

  hotQuestions: () => api.get('/medical/hot'),

  sessionHistory: (sessionKey: string) =>
    api.get(`/medical/session/${sessionKey}/history`),
};

export const knowledgeApi = {
  list: (keyword?: string, pageNum = 1, pageSize = 10) =>
    api.get('/knowledge/list', { params: { keyword, pageNum, pageSize } }),

  detail: (id: number) => api.get(`/knowledge/detail/${id}`),

  create: (data: { title: string; category?: string; content: string; source?: string }) =>
    api.post('/knowledge/create', data),

  update: (id: number, data: { title: string; category?: string; content: string; source?: string }) =>
    api.put(`/knowledge/update/${id}`, data),

  delete: (id: number) => api.delete(`/knowledge/${id}`),

  search: (data: { query: string; topK?: number }) =>
    api.post('/knowledge/search', data),
};

export default api;
