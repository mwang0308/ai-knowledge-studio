<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>召回测试</h1>
        <div class="sub">发布前验证问题能否命中正确分片，测试记录由后端保存。</div>
      </div>
      <div class="btns"><RouterLink class="btn" to="/document">返回文档</RouterLink></div>
    </div>

    <div class="grid two">
      <section class="panel">
        <div class="panel-head">
          <h2>待测试文档</h2>
          <div class="btns"><button class="btn" @click="loadDocuments">刷新</button></div>
        </div>
        <div class="panel-body">
          <table class="table">
            <thead><tr><th>文档</th><th>解析</th><th>索引</th><th>分片</th><th>发布</th></tr></thead>
            <tbody>
              <tr v-for="item in documents" :key="item.id" :class="{ active: item.id === activeDocument?.id }" @click="selectDocument(item)">
                <td>{{ item.name }}</td>
                <td><span class="status" :class="item.parseStatus === 'PARSE_CHUNKED' ? 'green' : 'amber'">{{ item.parseStatus }}</span></td>
                <td>{{ item.indexStatus }}</td>
                <td>{{ item.chunkCount }}</td>
                <td>{{ item.publishStatus }}</td>
              </tr>
              <tr v-if="!documents.length"><td colspan="5">暂无可测试文档</td></tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>测试参数</h2></div>
        <div class="panel-body">
          <div class="form">
            <div class="field"><label>测试范围</label><select v-model="form.testScope" class="select"><option value="DOCUMENT">当前文档</option><option value="DIRECTORY">当前目录</option><option value="KNOWLEDGE_BASE">当前知识库</option><option value="PUBLISHED">已发布内容</option></select></div>
            <div class="field"><label>测试问题</label><input v-model="form.queryText" class="input" placeholder="输入要验证的业务问题"></div>
            <div class="field"><label>TopK</label><select v-model.number="form.topK" class="select"><option :value="3">Top 3</option><option :value="5">Top 5</option><option :value="10">Top 10</option></select></div>
          </div>
          <div class="btns end-actions"><button class="btn primary" :disabled="testing" @click="runTest">{{ testing ? '测试中' : '开始测试' }}</button></div>
          <table class="kv block-gap">
            <tbody>
              <tr><td>文档</td><td>{{ activeDocument?.name || '-' }}</td></tr>
              <tr><td>结果</td><td>{{ testResult ? `TopScore ${testResult.topScore} / ${testResult.latencyMs}ms` : '-' }}</td></tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <section class="panel section-gap">
      <div class="panel-head"><h2>命中结果</h2><span class="status blue">{{ testResult?.hits.length || 0 }} 条</span></div>
      <div class="panel-body">
        <table class="table">
          <thead><tr><th>排名</th><th>分片</th><th>文档</th><th>标题路径</th><th>页码</th><th>相似度</th><th>发布</th></tr></thead>
          <tbody>
            <tr v-for="hit in testResult?.hits || []" :key="hit.chunkId" :class="{ active: hit.rankNo === 1 }">
              <td>{{ hit.rankNo }}</td>
              <td>{{ hit.chunkId }}</td>
              <td>{{ hit.documentId }}</td>
              <td>{{ hit.titlePath || '-' }}</td>
              <td>{{ pageText(hit) }}</td>
              <td>{{ hit.score }}</td>
              <td><span class="status" :class="hit.enabled ? 'green' : 'amber'">{{ hit.publishStatus }}</span></td>
            </tr>
            <tr v-if="!testResult?.hits.length"><td colspan="7">请输入问题并执行召回测试</td></tr>
          </tbody>
        </table>
        <div v-if="testResult?.hits[0]" class="detail-card block-gap"><strong>Top1 内容</strong><br>{{ testResult.hits[0].contentPreview }}</div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { pageKnowledgeDocument, testRetrieval } from '../api/document';
import type { KnowledgeDocumentResponse, RetrievalHitResponse, RetrievalTestResponse } from '../types/document';

const documents = ref<KnowledgeDocumentResponse[]>([]);
const activeDocument = ref<KnowledgeDocumentResponse>();
const testResult = ref<RetrievalTestResponse>();
const testing = ref(false);

const form = reactive({
  testScope: 'DOCUMENT',
  queryText: '',
  topK: 5,
});

function selectDocument(document: KnowledgeDocumentResponse) {
  activeDocument.value = document;
  testResult.value = undefined;
}

function pageText(hit: RetrievalHitResponse) {
  if (!hit.pageStart && !hit.pageEnd) {
    return '-';
  }
  return `${hit.pageStart || '-'} - ${hit.pageEnd || '-'}`;
}

async function loadDocuments() {
  const result = await pageKnowledgeDocument({ pageNo: 1, pageSize: 100, parseStatus: 'PARSE_CHUNKED' });
  documents.value = result.data.records;
  activeDocument.value = documents.value[0];
}

async function runTest() {
  if (!activeDocument.value) {
    ElMessage.warning('请选择文档');
    return;
  }
  if (!form.queryText.trim()) {
    ElMessage.warning('请输入测试问题');
    return;
  }
  testing.value = true;
  try {
    const result = await testRetrieval({
      knowledgeBaseId: activeDocument.value.knowledgeBaseId,
      directoryId: ['DIRECTORY'].includes(form.testScope) ? activeDocument.value.directoryId : undefined,
      documentId: ['DOCUMENT'].includes(form.testScope) ? activeDocument.value.id : undefined,
      queryText: form.queryText,
      testScope: form.testScope,
      topK: form.topK,
    });
    testResult.value = result.data;
    ElMessage.success('召回测试完成');
  } finally {
    testing.value = false;
  }
}

onMounted(loadDocuments);
</script>
