import { defineStore } from 'pinia';

export const useWorkspaceStore = defineStore('workspace', {
  state: () => ({
    activeKnowledgeBase: '财务共享知识库',
    activeDirectory: '报销制度 / 费用标准',
  }),
});
