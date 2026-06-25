<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>文档处理</h1>
        <div class="sub">按文档查看解析状态、文档结构、结构块和分片结果。</div>
      </div>
      <div class="btns">
        <RouterLink class="btn" :to="{ path: '/upload', query: { knowledgeBaseId: query.knowledgeBaseId, directoryId: query.directoryId } }">上传文档</RouterLink>
        <RouterLink class="btn primary" to="/retrieval">召回测试</RouterLink>
      </div>
    </div>

    <section class="panel document-table-panel">
      <div class="panel-head document-filter-head">
        <div class="filter-bar document-filter-bar">
          <input v-model="query.name" class="input" placeholder="搜索文档名称" @keyup.enter="searchDocuments">
          <select v-model="query.parseStatus" class="select" @change="searchDocuments">
            <option :value="undefined">全部解析状态</option>
            <option value="UPLOADED">已上传</option>
            <option value="PARSE_CHUNKING">解析分片中</option>
            <option value="PARSE_CHUNKED">解析分片完成</option>
            <option value="PROCESS_FAILED">处理失败</option>
          </select>
          <select v-model="query.publishStatus" class="select" @change="searchDocuments">
            <option :value="undefined">全部发布状态</option>
            <option value="UNPUBLISHED">未发布</option>
            <option value="PUBLISHED">已发布</option>
            <option value="OFFLINE">已下架</option>
          </select>
        </div>
        <div class="btns">
          <button class="btn" @click="searchDocuments">查询</button>
          <span class="status blue">共 {{ total }} 个文档</span>
        </div>
      </div>
      <div class="panel-body document-table-body">
        <table class="table document-list-table">
          <thead><tr><th>文档</th><th>解析状态</th><th>审核 / 发布</th><th>分片</th><th>更新时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="item in documents" :key="item.id" :class="{ active: item.id === activeDocument?.id && detailVisible }">
              <td>
                <b>{{ item.name }}</b>
                <div class="muted">{{ item.fileExt }} / v{{ item.currentVersionId || '-' }}</div>
              </td>
              <td><span class="status" :class="statusTone(item.parseStatus)">{{ parseStatusText(item.parseStatus) }}</span></td>
              <td>
                <span>{{ reviewStatusText(item.reviewStatus) }}</span>
                <div class="muted">{{ publishStatusText(item.publishStatus) }}</div>
              </td>
              <td>{{ item.chunkCount }}</td>
              <td>{{ formatDate(item.updateTime) }}</td>
              <td>
                <div class="ops document-row-ops">
                  <button class="btn" type="button" @click="openDocumentDetail(item)">分片预览</button>
                  <RouterLink class="btn" :to="{ path: '/publish', query: { documentId: item.id } }">审核发布</RouterLink>
                  <button
                    class="btn"
                    type="button"
                    :disabled="item.parseStatus !== 'PROCESS_FAILED'"
                    title="后端重试处理接口接入后启用"
                  >
                    重试处理
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!documents.length"><td colspan="6">暂无文档</td></tr>
          </tbody>
        </table>
        <div class="table-pagination">
          <el-pagination
            v-model:current-page="query.pageNo"
            v-model:page-size="query.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </section>

    <el-dialog
      v-model="detailVisible"
      class="document-detail-dialog"
      width="1320px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <div class="document-dialog-title">
          <div>
            <h2>{{ activeDocument?.name || '文档详情' }}</h2>
            <div class="muted">{{ activeDocument ? `文档 ID ${activeDocument.id} / 当前版本 ${activeDocument.currentVersionId || '-'}` : '-' }}</div>
          </div>
          <div v-if="activeDocument" class="btns">
            <span class="status" :class="statusTone(activeDocument.parseStatus)">{{ parseStatusText(activeDocument.parseStatus) }}</span>
            <span class="status" :class="activeDocument.publishStatus === 'PUBLISHED' ? 'green' : 'amber'">{{ publishStatusText(activeDocument.publishStatus) }}</span>
            <RouterLink
              class="btn primary"
              :to="{ path: '/retrieval', query: { knowledgeBaseId: activeDocument.knowledgeBaseId, directoryId: activeDocument.directoryId, documentId: activeDocument.id } }"
            >
              当前文档召回测试
            </RouterLink>
          </div>
        </div>
      </template>

      <div v-if="activeDocument" class="document-dialog-body">
        <div class="document-status-strip">
          <div><label>索引状态</label><strong>{{ indexStatusText(activeDocument.indexStatus) }}</strong></div>
          <div><label>审核状态</label><strong>{{ reviewStatusText(activeDocument.reviewStatus) }}</strong></div>
          <div><label>发布状态</label><strong>{{ publishStatusText(activeDocument.publishStatus) }}</strong></div>
          <div><label>分片数量</label><strong>{{ activeDocument.chunkCount }}</strong></div>
        </div>

        <div class="structure-workspace dialog-structure-workspace">
          <section class="structure-tree-panel">
            <div class="subsection-head">文档结构</div>
            <div class="structure-tree dialog-structure-tree">
              <button
                v-for="node in flatStructureTree"
                :key="node.path"
                class="tree-node-row"
                :class="{ active: node.path === activeStructurePath }"
                :style="{ paddingLeft: `${12 + node.level * 18}px` }"
                type="button"
                @click="selectTreeNode(node.path)"
              >
                <span>{{ node.name }}</span>
                <small>{{ node.chunkCount }}</small>
              </button>
              <div v-if="!flatStructureTree.length" class="empty-state">暂无文档结构数据。</div>
            </div>
          </section>

          <section class="block-chunk-area dialog-block-chunk-area">
            <div class="subsection-head">
              <span>结构块与分片</span>
              <span>{{ activeStructurePath || '-' }}</span>
            </div>
            <div v-if="!activeBlocks.length" class="empty-state">当前结构下暂无结构块。</div>
            <template v-else>
              <div class="block-section">
                <div class="section-caption">
                  <b>结构块</b>
                  <span>横向滚动查看全部结构块</span>
                </div>
                <div class="block-list">
                  <button
                    v-for="block in activeBlocks"
                    :key="block.blockId"
                    type="button"
                    class="block-list-item"
                    :class="{ active: block.blockId === activeBlock?.blockId }"
                    @click="selectBlock(block)"
                  >
                    <b>{{ block.blockId }}</b>
                    <small>{{ block.chunks.length }} 个分片</small>
                  </button>
                </div>
              </div>

              <div class="chunk-review-workbench">
                <div class="block-detail dialog-block-detail">
                  <div class="block-detail-head">
                    <b><span>当前结构块：</span>{{ activeBlock?.blockId || '-' }}</b>
                    <span>{{ activeBlock?.chunks.length || 0 }} 个分片</span>
                  </div>
                  <table class="table">
                    <thead><tr><th>分片</th><th>来源章节</th><th>Token</th><th>字符</th><th>页码</th><th>状态</th></tr></thead>
                    <tbody>
                      <tr v-for="item in activeBlock?.chunks || []" :key="item.id" :class="{ active: item.id === activeChunk?.id }" @click="activeChunk = item">
                        <td>{{ item.chunkId }}</td>
                        <td>{{ normalizeTitlePath(item.titlePath) }}</td>
                        <td>{{ item.tokenCount }}</td>
                        <td>{{ item.charCount }}</td>
                        <td>{{ chunkPageText(item) }}</td>
                        <td><span class="status" :class="item.enabled ? 'green' : 'amber'">{{ publishStatusText(item.publishStatus) }}</span></td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <section class="chunk-preview-box chunk-preview-side">
                  <div class="subsection-head">分片内容预览</div>
                  <div class="chunk-meta" v-if="activeChunk">
                    <b>{{ activeChunk.chunkId }}</b>
                    <span>{{ chunkPageText(activeChunk) }} / {{ activeChunk.tokenCount }} Token</span>
                  </div>
                  <div class="chunk-text dialog-chunk-text">{{ activeChunk?.contentPreview || '选择分片后查看内容。' }}</div>
                </section>
              </div>
            </template>
          </section>
        </div>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { pageKnowledgeChunk, pageKnowledgeDocument } from '../api/document';
import type { KnowledgeChunkResponse, KnowledgeDocumentResponse } from '../types/document';

interface StructureNode {
  titlePath: string;
  blocks: BlockNode[];
  chunks: KnowledgeChunkResponse[];
}

interface BlockNode {
  blockId: string;
  chunks: KnowledgeChunkResponse[];
}

interface TreeNode {
  name: string;
  path: string;
  level: number;
  chunkCount: number;
}

const route = useRoute();
const documents = ref<KnowledgeDocumentResponse[]>([]);
const chunks = ref<KnowledgeChunkResponse[]>([]);
const activeDocument = ref<KnowledgeDocumentResponse>();
const activeStructurePath = ref('');
const activeBlock = ref<BlockNode>();
const activeChunk = ref<KnowledgeChunkResponse>();
const detailVisible = ref(false);
const total = ref(0);
const unassignedBlockName = '未归属结构块';

const query = reactive({
  knowledgeBaseId: Number(route.query.knowledgeBaseId) || undefined,
  directoryId: Number(route.query.directoryId) || undefined,
  name: undefined as string | undefined,
  parseStatus: undefined as string | undefined,
  publishStatus: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10,
});

const structureNodes = computed<StructureNode[]>(() => {
  const structureMap = new Map<string, StructureNode>();
  chunks.value.forEach((chunk) => {
    const titlePath = normalizeTitlePath(chunk.titlePath);
    const structure = structureMap.get(titlePath) || { titlePath, blocks: [], chunks: [] };
    structure.chunks.push(chunk);
    const blockIds = chunk.blockIds?.length ? chunk.blockIds : [unassignedBlockName];
    blockIds.forEach((blockId) => {
      let block = structure.blocks.find((item) => item.blockId === blockId);
      if (!block) {
        block = { blockId, chunks: [] };
        structure.blocks.push(block);
      }
      block.chunks.push(chunk);
    });
    structureMap.set(titlePath, structure);
  });
  return Array.from(structureMap.values());
});
const flatStructureTree = computed<TreeNode[]>(() => {
  const nodeMap = new Map<string, TreeNode>();
  structureNodes.value.forEach((structure) => {
    const parts = structure.titlePath.split('/').map((item) => item.trim()).filter(Boolean);
    parts.forEach((part, index) => {
      const path = parts.slice(0, index + 1).join(' / ');
      const current = nodeMap.get(path);
      if (current) {
        current.chunkCount += structure.chunks.length;
      } else {
        nodeMap.set(path, {
          name: part,
          path,
          level: index,
          chunkCount: structure.chunks.length,
        });
      }
    });
  });
  return Array.from(nodeMap.values());
});
const activeChunks = computed(() => {
  return activeStructurePath.value
    ? chunks.value.filter((chunk) => normalizeTitlePath(chunk.titlePath).startsWith(activeStructurePath.value))
    : chunks.value;
});
const activeBlocks = computed<BlockNode[]>(() => {
  const blockMap = new Map<string, BlockNode>();
  activeChunks.value.forEach((chunk) => {
    const blockIds = chunk.blockIds?.length ? chunk.blockIds : [unassignedBlockName];
    blockIds.forEach((blockId) => {
      const block = blockMap.get(blockId) || { blockId, chunks: [] };
      block.chunks.push(chunk);
      blockMap.set(blockId, block);
    });
  });
  return Array.from(blockMap.values());
});

function normalizeTitlePath(titlePath?: string) {
  if (!titlePath || titlePath === 'root') {
    return '未识别结构';
  }
  return titlePath.split('/').map((item) => item.trim()).filter(Boolean).join(' / ');
}

function formatDate(value?: string) {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 19);
}

function statusTone(status: string) {
  if (['PARSE_CHUNKED', 'INDEXED', 'AUDIT_PASSED', 'PUBLISHED'].includes(status)) {
    return 'green';
  }
  if (['PROCESS_FAILED', 'INDEX_FAILED', 'AUDIT_REJECTED'].includes(status)) {
    return 'red';
  }
  return 'amber';
}

function parseStatusText(status: string) {
  const map: Record<string, string> = {
    UPLOADED: '已上传',
    PARSE_CHUNKING: '解析分片中',
    PARSE_CHUNKED: '解析分片完成',
    PROCESS_FAILED: '处理失败',
  };
  return map[status] || status || '-';
}

function indexStatusText(status: string) {
  const map: Record<string, string> = {
    WAIT_INDEX: '待索引',
    INDEXING: '索引中',
    INDEXED: '索引完成',
    INDEX_FAILED: '索引失败',
  };
  return map[status] || status || '-';
}

function reviewStatusText(status: string) {
  const map: Record<string, string> = {
    NOT_SUBMITTED: '未提交审核',
    WAIT_AUDIT: '待审核',
    AUDIT_PASSED: '审核通过',
    AUDIT_REJECTED: '审核驳回',
  };
  return map[status] || status || '-';
}

function publishStatusText(status: string) {
  const map: Record<string, string> = {
    UNPUBLISHED: '未发布',
    PUBLISHING: '发布中',
    PUBLISHED: '已发布',
    OFFLINE: '已下架',
  };
  return map[status] || status || '-';
}

function chunkPageText(chunk: KnowledgeChunkResponse) {
  if (!chunk.pageStart && !chunk.pageEnd) {
    return '-';
  }
  return `${chunk.pageStart || '-'} - ${chunk.pageEnd || '-'}`;
}

async function loadDocuments() {
  const result = await pageKnowledgeDocument(query);
  documents.value = result.data.records;
  total.value = result.data.total;
}

function searchDocuments() {
  query.pageNo = 1;
  activeDocument.value = undefined;
  activeStructurePath.value = '';
  activeBlock.value = undefined;
  activeChunk.value = undefined;
  detailVisible.value = false;
  loadDocuments();
}

function handlePageSizeChange() {
  query.pageNo = 1;
  resetDocumentSelection();
  loadDocuments();
}

function handlePageChange() {
  resetDocumentSelection();
  loadDocuments();
}

function resetDocumentSelection() {
  activeDocument.value = undefined;
  activeStructurePath.value = '';
  activeBlock.value = undefined;
  activeChunk.value = undefined;
  detailVisible.value = false;
}

async function openDocumentDetail(document: KnowledgeDocumentResponse) {
  activeDocument.value = document;
  activeStructurePath.value = '';
  activeBlock.value = undefined;
  activeChunk.value = undefined;
  detailVisible.value = true;
  const result = await pageKnowledgeChunk({
    documentId: document.id,
    versionId: document.currentVersionId,
    pageNo: 1,
    pageSize: 100,
  });
  chunks.value = result.data.records;
  activeStructurePath.value = flatStructureTree.value[0]?.path || '';
  activeBlock.value = activeBlocks.value[0];
  activeChunk.value = activeBlock.value?.chunks[0];
}

function selectTreeNode(path: string) {
  activeStructurePath.value = path;
  resetActiveBlock();
}

function selectBlock(block: BlockNode) {
  activeBlock.value = block;
  activeChunk.value = block.chunks[0];
}

function resetActiveBlock() {
  activeBlock.value = activeBlocks.value[0];
  activeChunk.value = activeBlock.value?.chunks[0];
}

onMounted(loadDocuments);
</script>
