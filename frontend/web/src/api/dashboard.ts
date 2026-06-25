import { http } from './http';
import type { DashboardSummaryResult, DashboardTodoPageRequest, DashboardTodoPageResult } from '../types/dashboard';

export function getDashboardSummary() {
  return http.get<unknown, DashboardSummaryResult>('/dashboard/summary');
}

export function pageDashboardTodos(params: DashboardTodoPageRequest) {
  return http.get<unknown, DashboardTodoPageResult>('/dashboard/todos/page', { params });
}
