import type { Result } from './knowledge';

export interface KnowledgeDirectoryResponse {
  id: number;
  knowledgeBaseId: number;
  parentId?: number;
  name: string;
  description?: string;
  path: string;
  level: number;
  sortOrder: number;
  status: number;
  statusName: string;
  createTime?: string;
  updateTime?: string;
  children: KnowledgeDirectoryResponse[];
}

export interface KnowledgeDirectoryTreeRequest {
  knowledgeBaseId?: number;
  description?: string;
  status?: number;
  createStartTime?: string;
  createEndTime?: string;
}

export interface KnowledgeDirectorySaveRequest {
  knowledgeBaseId: number;
  parentId?: number;
  name: string;
  description?: string;
  sortOrder?: number;
}

export type KnowledgeDirectoryTreeResult = Result<KnowledgeDirectoryResponse[]>;
