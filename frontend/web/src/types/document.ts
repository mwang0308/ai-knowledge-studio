import type { PageResult, Result } from './knowledge';

export interface KnowledgeDocumentUploadResponse {
  fileResourceId: number;
  documentId: string;
  versionId: string;
  taskId: string;
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
  id: string;
  knowledgeBaseId: number;
  directoryId: number;
  name: string;
  currentVersionId?: string;
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
  taskId: string;
  taskNo: string;
  documentId: string;
  versionId: string;
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
  documentId?: string;
  versionId?: string;
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
  documentId: string;
  documentVersionId: string;
  chunkNo: number;
  titlePath?: string;
  contentPreview?: string;
  tokenCount: number;
  charCount: number;
  pageStart?: number;
  pageEnd?: number;
  blockIds?: string[];
  blockNames?: string[];
  parseBlockId?: string;
  parseBlockName?: string;
  publishStatus: string;
  enabled: number;
  createTime?: string;
  updateTime?: string;
}

export interface KnowledgeDocumentStructureNode {
  sectionId: string;
  parentSectionId?: string;
  title: string;
  titlePath: string;
  level: number;
  pageStart?: number;
  pageEnd?: number;
  blockCount: number;
  children: KnowledgeDocumentStructureNode[];
}

export interface KnowledgeDocumentParseBlock {
  parseBlockId: string;
  parseBlockName: string;
  pageStart?: number;
  pageEnd?: number;
  sectionIds: string[];
  sectionTitles: string[];
  textPreview?: string;
}

export interface KnowledgeDocumentStructureResponse {
  documentId: string;
  versionId: string;
  directoryTree: KnowledgeDocumentStructureNode[];
  parseBlocks: KnowledgeDocumentParseBlock[];
}

export interface RetrievalTestRequest {
  knowledgeBaseId: number;
  directoryId?: number;
  documentId?: string;
  queryText: string;
  testScope: string;
  topK: number;
}

export interface RetrievalHitResponse {
  rankNo: number;
  chunkId: string;
  documentId: string;
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
  documentId?: string;
  queryText: string;
  testScope: string;
  topK: number;
  topScore: number;
  latencyMs: number;
  hits: RetrievalHitResponse[];
}

export interface ReviewSubmitRequest {
  documentId: string;
  reviewComment?: string;
}

export interface PublishSubmitRequest {
  documentId: string;
}

export type KnowledgeChunkPageResult = Result<PageResult<KnowledgeChunkResponse>>;
export type KnowledgeDocumentStructureResult = Result<KnowledgeDocumentStructureResponse>;
export type RetrievalTestResult = Result<RetrievalTestResponse>;
