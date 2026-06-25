import { http } from './http';
import type {
  KnowledgeBasePageRequest,
  KnowledgeBaseResponse,
  KnowledgeBaseSaveRequest,
  PageResult,
  Result,
} from '../types/knowledge';

export function pageKnowledgeBase(params: KnowledgeBasePageRequest) {
  return http.get<unknown, Result<PageResult<KnowledgeBaseResponse>>>('/knowledge-bases/page', { params });
}

export function createKnowledgeBase(data: KnowledgeBaseSaveRequest) {
  return http.post<unknown, Result<number>>('/knowledge-bases', data);
}

export function updateKnowledgeBase(id: number, data: KnowledgeBaseSaveRequest) {
  return http.put<unknown, Result<void>>(`/knowledge-bases/${id}`, { ...data, id });
}

export function enableKnowledgeBase(id: number) {
  return http.put<unknown, Result<void>>(`/knowledge-bases/${id}/enable`);
}

export function disableKnowledgeBase(id: number) {
  return http.put<unknown, Result<void>>(`/knowledge-bases/${id}/disable`);
}

export function deleteKnowledgeBase(id: number) {
  return http.delete<unknown, Result<void>>(`/knowledge-bases/${id}`);
}
