import { http } from './http';
import type {
  KnowledgeChunkPageRequest,
  KnowledgeChunkPageResult,
  KnowledgeDocumentPageRequest,
  KnowledgeDocumentPageResult,
  KnowledgeDocumentStructureResult,
  KnowledgeDocumentUploadResult,
  KnowledgeTaskProgressResult,
  PublishSubmitRequest,
  RetrievalTestRequest,
  RetrievalTestResult,
  ReviewSubmitRequest,
} from '../types/document';
import type { Result } from '../types/knowledge';

export function uploadKnowledgeDocument(data: FormData) {
  return http.post<unknown, KnowledgeDocumentUploadResult>('/knowledge-documents/upload', data);
}

export function reuploadKnowledgeDocument(documentId: string, data: FormData) {
  return http.post<unknown, KnowledgeDocumentUploadResult>(`/knowledge-documents/${documentId}/reupload`, data);
}

export function reprocessKnowledgeDocument(documentId: string) {
  return http.post<unknown, KnowledgeDocumentUploadResult>(`/knowledge-documents/${documentId}/reprocess`);
}

export function deleteKnowledgeDocument(documentId: string) {
  return http.delete<unknown, Result<boolean>>(`/knowledge-documents/${documentId}`);
}

export function pageKnowledgeDocument(params: KnowledgeDocumentPageRequest) {
  return http.get<unknown, KnowledgeDocumentPageResult>('/knowledge-documents/page', { params });
}

export function getKnowledgeTaskProgress(taskId: string) {
  return http.get<unknown, KnowledgeTaskProgressResult>(`/knowledge-documents/tasks/${taskId}/progress`);
}

export function pageKnowledgeChunk(params: KnowledgeChunkPageRequest) {
  return http.get<unknown, KnowledgeChunkPageResult>('/knowledge-chunks/page', { params });
}

export function getKnowledgeDocumentStructure(documentId: string) {
  return http.get<unknown, KnowledgeDocumentStructureResult>(`/knowledge-documents/${documentId}/structure`);
}

export function testRetrieval(data: RetrievalTestRequest) {
  return http.post<unknown, RetrievalTestResult>('/retrieval-tests', data);
}

export function passReview(data: ReviewSubmitRequest) {
  return http.post<unknown, Result<boolean>>('/knowledge-governance/review/pass', data);
}

export function rejectReview(data: ReviewSubmitRequest) {
  return http.post<unknown, Result<boolean>>('/knowledge-governance/review/reject', data);
}

export function publishDocument(data: PublishSubmitRequest) {
  return http.post<unknown, Result<boolean>>('/knowledge-governance/publish', data);
}

export function offlineDocument(data: PublishSubmitRequest) {
  return http.post<unknown, Result<boolean>>('/knowledge-governance/offline', data);
}
