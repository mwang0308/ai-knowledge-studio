import type { PageResult, Result } from './knowledge';

export interface KnowledgeDocumentUploadResponse {
  fileResourceId: number;
  documentId: number;
  versionId: number;
  taskId: number;
  taskNo: string;
  mqMessageId: string;
  fileHash: string;
  parseStatus: string;
  taskStatus: string;
}

export type KnowledgeDocumentUploadResult = Result<KnowledgeDocumentUploadResponse>;

export interface KnowledgeDocumentPageRequest {
  knowledgeBaseId?: number;
  directoryId?: number;
  name?: string;
  parseStatus?: string;
  publishStatus?: string;
  pageNo: number;
  pageSize: number;
}

export interface KnowledgeDocumentResponse {
  id: number;
  knowledgeBaseId: number;
  directoryId: number;
  name: string;
  currentVersionId?: number;
  fileName: string;
  fileExt: string;
  fileSize: number;
  parseStatus: string;
  indexStatus: string;
  reviewStatus: string;
  publishStatus: string;
  chunkCount: number;
  errorMessage?: string;
  createTime?: string;
  updateTime?: string;
}

export interface KnowledgeTaskProgressResponse {
  taskId: number;
  taskNo: string;
  documentId: number;
  versionId: number;
  stageCode: string;
  taskStatus: string;
  progress: number;
  errorCode?: string;
  errorMessage?: string;
  startTime?: string;
  finishTime?: string;
  updateTime?: string;
}

export type KnowledgeDocumentPageResult = Result<PageResult<KnowledgeDocumentResponse>>;
export type KnowledgeTaskProgressResult = Result<KnowledgeTaskProgressResponse>;

export interface KnowledgeChunkPageRequest {
  knowledgeBaseId?: number;
  directoryId?: number;
  documentId?: number;
  versionId?: number;
  publishStatus?: string;
  enabled?: number;
  pageNo: number;
  pageSize: number;
}

export interface KnowledgeChunkResponse {
  id: number;
  chunkId: string;
  knowledgeBaseId: number;
  directoryId: number;
  documentId: number;
  documentVersionId: number;
  chunkNo: number;
  titlePath?: string;
  contentPreview?: string;
  tokenCount: number;
  charCount: number;
  pageStart?: number;
  pageEnd?: number;
  blockIds?: string[];
  publishStatus: string;
  enabled: number;
  createTime?: string;
  updateTime?: string;
}

export interface RetrievalTestRequest {
  knowledgeBaseId: number;
  directoryId?: number;
  documentId?: number;
  queryText: string;
  testScope: string;
  topK: number;
}

export interface RetrievalHitResponse {
  rankNo: number;
  chunkId: string;
  documentId: number;
  titlePath?: string;
  contentPreview?: string;
  pageStart?: number;
  pageEnd?: number;
  score: number;
  publishStatus: string;
  enabled: number;
}

export interface RetrievalTestResponse {
  testId: number;
  knowledgeBaseId: number;
  directoryId?: number;
  documentId?: number;
  queryText: string;
  testScope: string;
  topK: number;
  topScore: number;
  latencyMs: number;
  hits: RetrievalHitResponse[];
}

export interface ReviewSubmitRequest {
  documentId: number;
  reviewComment?: string;
}

export interface PublishSubmitRequest {
  documentId: number;
}

export type KnowledgeChunkPageResult = Result<PageResult<KnowledgeChunkResponse>>;
export type RetrievalTestResult = Result<RetrievalTestResponse>;
