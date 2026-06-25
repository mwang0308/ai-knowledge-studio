import { createRouter, createWebHistory } from 'vue-router';
import WorkspaceLayout from '../layouts/WorkspaceLayout.vue';
import DashboardView from '../views/DashboardView.vue';
import KnowledgeView from '../views/KnowledgeView.vue';
import DirectoryView from '../views/DirectoryView.vue';
import UploadView from '../views/UploadView.vue';
import DocumentView from '../views/DocumentView.vue';
import RetrievalView from '../views/RetrievalView.vue';
import PublishView from '../views/PublishView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: WorkspaceLayout,
      children: [
        { path: '', name: 'dashboard', component: DashboardView, meta: { crumb: '首页 / 知识库工作台' } },
        { path: 'knowledge', name: 'knowledge', component: KnowledgeView, meta: { crumb: '知识库 / 创建与维护' } },
        { path: 'directory', name: 'directory', component: DirectoryView, meta: { crumb: '知识库 / 财务共享知识库 / 目录管理' } },
        { path: 'upload', name: 'upload', component: UploadView, meta: { crumb: '知识库 / 财务共享知识库 / 报销制度 / 费用标准 / 文档上传' } },
        { path: 'document', name: 'document', component: DocumentView, meta: { crumb: '财务共享报账手册V2.0.docx / 文档处理' } },
        { path: 'retrieval', name: 'retrieval', component: RetrievalView, meta: { crumb: '检索测试' } },
        { path: 'publish', name: 'publish', component: PublishView, meta: { crumb: '审核发布' } },
      ],
    },
  ],
});

export default router;
