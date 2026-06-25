import axios from 'axios';
import { ElMessage } from 'element-plus';

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

http.interceptors.response.use(
  (response) => {
    const body = response.data;
    if (body && typeof body === 'object' && 'code' in body && body.code !== '000000') {
      ElMessage.error(body.message || '请求失败，请稍后重试');
      return Promise.reject(body);
    }
    return body;
  },
  (error) => {
    const message = error?.response?.data?.message || '请求失败，请稍后重试';
    ElMessage.error(message);
    return Promise.reject(error);
  },
);
