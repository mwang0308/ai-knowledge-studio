import type { PageResult, Result } from './knowledge';

export interface DashboardTodoPageRequest {
  pageNo: number;
  pageSize: number;
  documentName?: string;
  parseStatus?: string;
  reviewStatus?: string;
  publishStatus?: string;
  createStartTime?: string;
  createEndTime?: string;
}

export interface DashboardTodoResponse {
  documentId: number;
  documentName: string;
  knowledgeBaseId: number;
  directoryId: number;
  currentStatus: string;
  parseStatus: string;
  reviewStatus: string;
  publishStatus: string;
  nextAction: string;
  actionPath: string;
  createTime?: string;
  updateTime?: string;
}

export interface DashboardSummaryResponse {
  knowledgeBaseCount: number;
  documentCount: number;
  waitAuditCount: number;
  processFailedCount: number;
  publishedCount: number;
  chunkCount: number;
  todos: DashboardTodoResponse[];
}

export type DashboardSummaryResult = Result<DashboardSummaryResponse>;
export type DashboardTodoPageResult = Result<PageResult<DashboardTodoResponse>>;
