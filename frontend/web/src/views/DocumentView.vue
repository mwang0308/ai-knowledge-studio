<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>文档处理</h1>
        <div class="sub">按文档查看解析状态、文档结构、解析块和分片结果。</div>
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
                    :disabled="isProcessing(item)"
                    @click="confirmReprocess(item)"
                  >
                    重新处理
                  </button>
                  <button class="btn" type="button" :disabled="isProcessing(item)" @click="openReuploadDialog(item)">
                    重新上传
                  </button>
                  <button class="btn danger" type="button" @click="confirmDelete(item)">
                    删除
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
            <div class="subsection-head">完整文档结构树</div>
            <div class="structure-tree dialog-structure-tree">
              <el-tree
                v-if="structureTree.length"
                class="document-outline-tree"
                :data="structureTree"
                node-key="key"
                :props="{ label: 'name', children: 'children' }"
                :current-node-key="activeStructureNodeKey"
                :expand-on-click-node="false"
                :indent="22"
                default-expand-all
                highlight-current
                @node-click="selectStructureNode"
              >
                <template #default="{ data }">
                  <span class="document-outline-node" :class="`level-${Math.min(data.level || 1, 6)}`">
                    <span class="document-outline-level">H{{ data.level || 1 }}</span>
                    <span class="document-outline-main">
                      <span class="document-outline-title" :title="data.path">{{ data.name }}</span>
                      <span v-if="outlinePageText(data)" class="document-outline-page">{{ outlinePageText(data) }}</span>
                    </span>
                    <small>{{ data.chunkCount }}</small>
                  </span>
                </template>
              </el-tree>
              <div v-else class="empty-state">暂无文档结构数据。</div>
            </div>
          </section>

          <section class="block-chunk-area dialog-block-chunk-area">
            <div class="subsection-head">
              <span>解析块与分片</span>
              <span>{{ activeStructurePath || '-' }}</span>
            </div>
            <div v-if="!activeBlocks.length" class="empty-state">当前结构下暂无解析块。</div>
            <template v-else>
              <div class="block-section">
                <div class="section-caption">
                  <b>解析块</b>
                  <span>点击解析块查看对应分片</span>
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
                    <b>{{ block.blockName }}</b>
                    <small>{{ block.chunks.length }} 个分片</small>
                  </button>
                </div>
              </div>

              <div class="chunk-review-workbench">
                <div class="block-detail dialog-block-detail">
                  <div class="block-detail-head">
                    <b><span>当前解析块：</span>{{ activeBlock?.blockName || '-' }}</b>
                    <span>{{ activeBlock?.chunks.length || 0 }} 个分片</span>
                  </div>
                  <table class="table">
                    <thead><tr><th>分片</th><th>来源章节</th><th>来源解析块</th><th>Token</th><th>页码</th><th>状态</th></tr></thead>
                    <tbody>
                      <tr v-for="item in activeBlock?.chunks || []" :key="item.id" :class="{ active: item.id === activeChunk?.id }" @click="activeChunk = item">
                        <td>{{ item.chunkId }}</td>
                        <td>{{ normalizeTitlePath(item.titlePath) }}</td>
                        <td>{{ chunkParseBlockText(item) }}</td>
                        <td>{{ item.tokenCount }}</td>
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

    <el-dialog
      v-model="reuploadVisible"
      width="520px"
      title="重新上传处理"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="reupload-panel">
        <div class="field">
          <label>当前文档</label>
          <div class="readonly-field">{{ reuploadDocument?.name || '-' }}</div>
        </div>
        <div class="field">
          <label>选择新文件</label>
          <input class="input" type="file" @change="handleReuploadFileChange">
        </div>
        <div class="muted">提交后会生成新版本、重新投递解析分片任务，并清空当前审核发布状态。</div>
      </div>
      <template #footer>
        <button class="btn" type="button" @click="reuploadVisible = false">取消</button>
        <button class="btn primary" type="button" :disabled="!reuploadFile" @click="submitReupload">提交处理</button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import {
  deleteKnowledgeDocument,
  getKnowledgeDocumentStructure,
  pageKnowledgeChunk,
  pageKnowledgeDocument,
  reprocessKnowledgeDocument,
  reuploadKnowledgeDocument,
} from '../api/document';
import type {
  KnowledgeChunkResponse,
  KnowledgeDocumentResponse,
  KnowledgeDocumentStructureNode,
  KnowledgeDocumentStructureResponse,
} from '../types/document';

interface StructureNode {
  titlePath: string;
  blocks: BlockNode[];
  chunks: KnowledgeChunkResponse[];
}

interface BlockNode {
  blockId: string;
  blockName: string;
  chunks: KnowledgeChunkResponse[];
}

interface TreeNode {
  sectionId?: string;
  key: string;
  name: string;
  path: string;
  level: number;
  pageStart?: number;
  pageEnd?: number;
  chunkCount: number;
  children: TreeNode[];
}

const route = useRoute();
const documents = ref<KnowledgeDocumentResponse[]>([]);
const chunks = ref<KnowledgeChunkResponse[]>([]);
const documentStructure = ref<KnowledgeDocumentStructureResponse>();
const activeDocument = ref<KnowledgeDocumentResponse>();
const activeStructurePath = ref('');
const activeBlock = ref<BlockNode>();
const activeChunk = ref<KnowledgeChunkResponse>();
const detailVisible = ref(false);
const reuploadVisible = ref(false);
const reuploadDocument = ref<KnowledgeDocumentResponse>();
const reuploadFile = ref<File>();
const total = ref(0);
const unassignedBlockName = '未归属解析块';

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
    const blockIds = chunk.parseBlockId ? [chunk.parseBlockId] : (chunk.blockIds?.length ? chunk.blockIds : [unassignedBlockName]);
    blockIds.forEach((blockId) => {
      let block = structure.blocks.find((item) => item.blockId === blockId);
      if (!block) {
        block = { blockId, blockName: blockDisplayName(chunk, blockId), chunks: [] };
        structure.blocks.push(block);
      }
      if (!block.chunks.some((item) => item.chunkId === chunk.chunkId)) {
        block.chunks.push(chunk);
      }
    });
    structureMap.set(titlePath, structure);
  });
  return Array.from(structureMap.values());
});
const structureTree = computed<TreeNode[]>(() => {
  if (documentStructure.value?.directoryTree?.length) {
    const apiTree = mapStructureTree(documentStructure.value.directoryTree);
    if (apiTree.some(hasChildNode)) return ensureDocumentRoot(apiTree);
    return ensureDocumentRoot(buildPathTree(flattenApiStructure(documentStructure.value.directoryTree)));
  }
  return ensureDocumentRoot(buildPathTree(structureNodes.value.map((item) => ({ path: item.titlePath, name: item.titlePath }))));
});
const activeStructureNodeKey = computed(() => activeStructurePath.value || '__document_root__');

function mapStructureTree(nodes: KnowledgeDocumentStructureNode[]): TreeNode[] {
  return nodes.map((node) => ({
    sectionId: node.sectionId,
    key: normalizeTitlePath(node.titlePath),
    name: node.title,
    path: normalizeTitlePath(node.titlePath),
    level: node.level,
    pageStart: node.pageStart,
    pageEnd: node.pageEnd,
    chunkCount: countChunksByPath(node.titlePath),
    children: mapStructureTree(node.children || []),
  }));
}

function hasChildNode(node: TreeNode): boolean {
  return node.children.length > 0 || node.children.some(hasChildNode);
}

function flattenApiStructure(nodes: KnowledgeDocumentStructureNode[]): Array<{ sectionId?: string; path: string; name: string; level?: number; pageStart?: number; pageEnd?: number }> {
  return nodes.flatMap((node) => [
    {
      sectionId: node.sectionId,
      path: normalizeTitlePath(node.titlePath),
      name: node.title,
      level: node.level,
      pageStart: node.pageStart,
      pageEnd: node.pageEnd,
    },
    ...flattenApiStructure(node.children || []),
  ]);
}

function buildPathTree(items: Array<{ sectionId?: string; path: string; name: string; level?: number; pageStart?: number; pageEnd?: number }>): TreeNode[] {
  const roots: TreeNode[] = [];
  const nodeMap = new Map<string, TreeNode>();
  items.forEach((item) => {
    const normalizedPath = normalizeTitlePath(item.path);
    const parts = normalizedPath.split('/').map((part) => part.trim()).filter(Boolean);
    parts.forEach((part, index) => {
      const path = parts.slice(0, index + 1).join(' / ');
      let node = nodeMap.get(path);
      if (!node) {
        node = { key: path, name: part, path, level: index + 1, chunkCount: countChunksByPath(path), children: [] };
        nodeMap.set(path, node);
        if (index === 0) roots.push(node);
        else nodeMap.get(parts.slice(0, index).join(' / '))?.children.push(node);
      }
      if (index === parts.length - 1) {
        node.sectionId = item.sectionId;
        node.name = item.name || part;
        node.level = item.level || index + 1;
        node.pageStart = item.pageStart;
        node.pageEnd = item.pageEnd;
      }
    });
  });
  return roots;
}

function ensureDocumentRoot(nodes: TreeNode[]): TreeNode[] {
  if (nodes.length <= 1) return nodes;
  return [{
    key: '__document_root__',
    name: activeDocument.value?.name || '文档目录',
    path: '',
    level: 1,
    chunkCount: chunks.value.length,
    children: nodes,
  }];
}
const activeChunks = computed(() => {
  return activeStructurePath.value
    ? chunks.value.filter((chunk) => normalizeTitlePath(chunk.titlePath).startsWith(activeStructurePath.value))
    : chunks.value;
});

const activeBlocks = computed<BlockNode[]>(() => {
  const blockMap = new Map<string, BlockNode>();
  activeChunks.value.forEach((chunk) => {
    const blockIds = chunk.parseBlockId ? [chunk.parseBlockId] : (chunk.blockIds?.length ? chunk.blockIds : [unassignedBlockName]);
    blockIds.forEach((blockId) => {
      const block = blockMap.get(blockId) || { blockId, blockName: blockDisplayName(chunk, blockId), chunks: [] };
      if (!block.chunks.some((item) => item.chunkId === chunk.chunkId)) {
        block.chunks.push(chunk);
      }
      blockMap.set(blockId, block);
    });
  });
  return Array.from(blockMap.values());
});

function countChunksByPath(titlePath?: string) {
  const normalized = normalizeTitlePath(titlePath);
  return chunks.value.filter((chunk) => normalizeTitlePath(chunk.titlePath).startsWith(normalized)).length;
}

function blockDisplayName(chunk: KnowledgeChunkResponse, fallback: string) {
  if (chunk.parseBlockName) {
    return chunk.parseBlockName;
  }
  if (chunk.blockNames?.length) {
    return chunk.blockNames.join('，');
  }
  return fallback;
}

function chunkParseBlockText(chunk: KnowledgeChunkResponse) {
  return chunk.parseBlockName || chunk.blockNames?.join('，') || '-';
}

function outlinePageText(node: TreeNode) {
  if (!node.pageStart && !node.pageEnd) {
    return '';
  }
  if (node.pageStart && node.pageEnd && node.pageStart !== node.pageEnd) {
    return `第 ${node.pageStart}-${node.pageEnd} 页`;
  }
  return `第 ${node.pageStart || node.pageEnd} 页`;
}

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

function isProcessing(document: KnowledgeDocumentResponse) {
  return document.parseStatus === 'PARSE_CHUNKING';
}

async function loadDocuments() {
  const result = await pageKnowledgeDocument(query);
  documents.value = result.data.records;
  total.value = result.data.total;
}

function searchDocuments() {
  query.pageNo = 1;
  activeDocument.value = undefined;
  documentStructure.value = undefined;
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
  documentStructure.value = undefined;
  activeStructurePath.value = '';
  activeBlock.value = undefined;
  activeChunk.value = undefined;
  detailVisible.value = false;
}

async function openDocumentDetail(document: KnowledgeDocumentResponse) {
  activeDocument.value = document;
  documentStructure.value = undefined;
  activeStructurePath.value = '';
  activeBlock.value = undefined;
  activeChunk.value = undefined;
  detailVisible.value = true;
  const [structureResult, chunkRecords] = await Promise.all([
    getKnowledgeDocumentStructure(document.id),
    loadAllDocumentChunks(document),
  ]);
  documentStructure.value = structureResult.data;
  chunks.value = chunkRecords;
  activeStructurePath.value = structureTree.value[0]?.path || '';
  resetActiveBlock();
}

async function confirmReprocess(document: KnowledgeDocumentResponse) {
  await ElMessageBox.confirm(`确认重新处理“${document.name}”？当前版本已有分片会被清空并重新生成。`, '重新处理', {
    confirmButtonText: '确认处理',
    cancelButtonText: '取消',
    type: 'warning',
  });
  const result = await reprocessKnowledgeDocument(document.id);
  ElMessage.success(`已提交重新处理任务：${result.data.taskNo}`);
  resetDocumentSelection();
  await loadDocuments();
}

function openReuploadDialog(document: KnowledgeDocumentResponse) {
  reuploadDocument.value = document;
  reuploadFile.value = undefined;
  reuploadVisible.value = true;
}

function handleReuploadFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  reuploadFile.value = input.files?.[0];
}

async function submitReupload() {
  if (!reuploadDocument.value || !reuploadFile.value) {
    return;
  }
  const data = new FormData();
  data.append('file', reuploadFile.value);
  const result = await reuploadKnowledgeDocument(reuploadDocument.value.id, data);
  ElMessage.success(`已提交重新上传处理任务：${result.data.taskNo}`);
  reuploadVisible.value = false;
  resetDocumentSelection();
  await loadDocuments();
}

async function confirmDelete(document: KnowledgeDocumentResponse) {
  await ElMessageBox.confirm(`确认删除“${document.name}”？原文、解析产物、分片和治理记录会同步清理。`, '删除文档', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await deleteKnowledgeDocument(document.id);
  ElMessage.success('文档已删除');
  resetDocumentSelection();
  await loadDocuments();
}

async function loadAllDocumentChunks(document: KnowledgeDocumentResponse) {
  const pageSize = 100;
  const records: KnowledgeChunkResponse[] = [];
  let pageNo = 1;
  let totalRecords = 0;
  do {
    const result = await pageKnowledgeChunk({
      documentId: document.id,
      versionId: document.currentVersionId,
      pageNo,
      pageSize,
    });
    records.push(...result.data.records);
    totalRecords = result.data.total;
    pageNo += 1;
  } while (records.length < totalRecords);
  return records;
}

function selectStructureNode(node: TreeNode) {
  activeStructurePath.value = node.path;
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
