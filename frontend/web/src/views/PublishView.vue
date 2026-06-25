<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>审核发布</h1>
        <div class="sub">审核通过后才能发布；发布会启用当前版本分片进入正式检索范围。</div>
      </div>
      <div class="btns"><button class="btn" @click="loadDocuments">刷新</button></div>
    </div>

    <section class="panel">
      <div class="panel-head">
        <div class="filter-bar">
          <input v-model="query.name" class="input" placeholder="搜索文档名称" @keyup.enter="loadDocuments">
          <select v-model="query.publishStatus" class="select" @change="loadDocuments">
            <option :value="undefined">全部发布状态</option>
            <option value="UNPUBLISHED">未发布</option>
            <option value="PUBLISHED">已发布</option>
            <option value="OFFLINE">已下架</option>
          </select>
        </div>
        <div class="btns"><span class="status blue">共 {{ total }} 个文档</span></div>
      </div>
      <div class="panel-body">
        <table class="table">
          <thead><tr><th>文档</th><th>版本</th><th>解析</th><th>索引</th><th>审核</th><th>发布</th><th>分片</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="item in documents" :key="item.id" :class="{ active: item.id === activeDocument?.id }" @click="activeDocument = item">
              <td>{{ item.name }}</td>
              <td>v{{ item.currentVersionId || '-' }}</td>
              <td><span class="status" :class="item.parseStatus === 'PARSE_CHUNKED' ? 'green' : 'amber'">{{ item.parseStatus }}</span></td>
              <td>{{ item.indexStatus }}</td>
              <td><span class="status" :class="reviewTone(item.reviewStatus)">{{ item.reviewStatus }}</span></td>
              <td><span class="status" :class="publishTone(item.publishStatus)">{{ item.publishStatus }}</span></td>
              <td>{{ item.chunkCount }}</td>
              <td>
                <span class="ops">
                  <a class="link" @click.stop="openReview(item)">审核</a>
                  <a v-if="item.reviewStatus === 'AUDIT_PASSED' && item.publishStatus !== 'PUBLISHED'" class="link" @click.stop="publish(item)">发布</a>
                  <a v-if="item.publishStatus === 'PUBLISHED'" class="link" @click.stop="offline(item)">下架</a>
                  <RouterLink class="link" :to="{ path: '/retrieval' }">测试</RouterLink>
                </span>
              </td>
            </tr>
            <tr v-if="!documents.length"><td colspan="8">暂无已完成处理的文档</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <el-dialog v-model="reviewVisible" title="审核文档" width="720px">
      <table class="kv">
        <tbody>
          <tr><td>文档</td><td>{{ activeDocument?.name || '-' }}</td></tr>
          <tr><td>解析状态</td><td>{{ activeDocument?.parseStatus || '-' }}</td></tr>
          <tr><td>索引状态</td><td>{{ activeDocument?.indexStatus || '-' }}</td></tr>
          <tr><td>分片数量</td><td>{{ activeDocument?.chunkCount ?? '-' }}</td></tr>
        </tbody>
      </table>
      <div class="form block-gap"><div class="field"><label>审核意见</label><textarea v-model="reviewComment" class="textarea" placeholder="驳回时建议填写原因"></textarea></div></div>
      <template #footer>
        <button class="btn" @click="reviewVisible = false">关闭</button>
        <button class="btn red" @click="reject">驳回</button>
        <button class="btn green" @click="pass">审核通过</button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { offlineDocument, pageKnowledgeDocument, passReview, publishDocument, rejectReview } from '../api/document';
import type { KnowledgeDocumentResponse } from '../types/document';

const documents = ref<KnowledgeDocumentResponse[]>([]);
const activeDocument = ref<KnowledgeDocumentResponse>();
const total = ref(0);
const reviewVisible = ref(false);
const reviewComment = ref('');

const query = reactive({
  name: undefined as string | undefined,
  parseStatus: 'PARSE_CHUNKED',
  publishStatus: undefined as string | undefined,
  pageNo: 1,
  pageSize: 100,
});

function reviewTone(status: string) {
  if (status === 'AUDIT_PASSED') {
    return 'green';
  }
  if (status === 'AUDIT_REJECTED') {
    return 'red';
  }
  return 'amber';
}

function publishTone(status: string) {
  if (status === 'PUBLISHED') {
    return 'green';
  }
  if (status === 'OFFLINE') {
    return 'red';
  }
  return 'amber';
}

function openReview(document: KnowledgeDocumentResponse) {
  activeDocument.value = document;
  reviewComment.value = '';
  reviewVisible.value = true;
}

async function loadDocuments() {
  const result = await pageKnowledgeDocument(query);
  documents.value = result.data.records;
  total.value = result.data.total;
  activeDocument.value = documents.value[0];
}

async function pass() {
  if (!activeDocument.value) {
    return;
  }
  await passReview({ documentId: activeDocument.value.id, reviewComment: reviewComment.value });
  ElMessage.success('审核已通过');
  reviewVisible.value = false;
  await loadDocuments();
}

async function reject() {
  if (!activeDocument.value) {
    return;
  }
  await rejectReview({ documentId: activeDocument.value.id, reviewComment: reviewComment.value });
  ElMessage.success('审核已驳回');
  reviewVisible.value = false;
  await loadDocuments();
}

async function publish(document: KnowledgeDocumentResponse) {
  await publishDocument({ documentId: document.id });
  ElMessage.success('文档已发布');
  await loadDocuments();
}

async function offline(document: KnowledgeDocumentResponse) {
  await offlineDocument({ documentId: document.id });
  ElMessage.success('文档已下架');
  await loadDocuments();
}

onMounted(loadDocuments);
</script>
