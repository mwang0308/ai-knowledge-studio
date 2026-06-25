export interface PageResult<T> {
  total: number;
  pageNo: number;
  pageSize: number;
  records: T[];
}

export interface Result<T> {
  code: string;
  message: string;
  data: T;
  traceId?: string;
}

export interface KnowledgeBaseResponse {
  id: number;
  name: string;
  description?: string;
  status: number;
  statusName: string;
  publishedStatus: number;
  publishedStatusName: string;
  documentCount: number;
  chunkCount: number;
  createTime?: string;
  updateTime?: string;
}

export interface KnowledgeBasePageRequest {
  pageNo: number;
  pageSize: number;
  name?: string;
  status?: number;
  createStartTime?: string;
  createEndTime?: string;
}

export interface KnowledgeBaseSaveRequest {
  name: string;
  description?: string;
}
