import { http } from './http';
import type {
  KnowledgeDirectoryResponse,
  KnowledgeDirectorySaveRequest,
  KnowledgeDirectoryTreeRequest,
  KnowledgeDirectoryTreeResult,
} from '../types/directory';
import type { Result } from '../types/knowledge';

export function treeKnowledgeDirectory(params: KnowledgeDirectoryTreeRequest) {
  return http.get<unknown, KnowledgeDirectoryTreeResult>('/knowledge-directories/tree', { params });
}

export function createKnowledgeDirectory(data: KnowledgeDirectorySaveRequest) {
  return http.post<unknown, Result<number>>('/knowledge-directories', data);
}

export function updateKnowledgeDirectory(id: number, data: KnowledgeDirectorySaveRequest) {
  return http.put<unknown, Result<void>>(`/knowledge-directories/${id}`, { ...data, id });
}

export function deleteKnowledgeDirectory(id: number) {
  return http.delete<unknown, Result<void>>(`/knowledge-directories/${id}`);
}

export function enableKnowledgeDirectory(id: number) {
  return http.put<unknown, Result<void>>(`/knowledge-directories/${id}/enable`);
}

export function disableKnowledgeDirectory(id: number) {
  return http.put<unknown, Result<void>>(`/knowledge-directories/${id}/disable`);
}
