<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>知识库工作台</h1>
        <div class="sub">实时展示知识治理状态、流程进度和待办概览。</div>
      </div>
      <div class="btns"><button class="btn" @click="loadSummary">刷新</button></div>
    </div>

    <div class="grid cols-4">
      <div v-for="item in metrics" :key="item.label" class="panel metric">
        <label>{{ item.label }}</label>
        <strong :class="item.tone">{{ item.value }}</strong>
      </div>
    </div>

    <section class="panel section-gap">
      <div class="panel-head">
        <h2>知识治理流程</h2>
        <span class="status blue">层级：知识库 → 目录 → 文档 → 解析块 → 分片</span>
      </div>
      <div class="panel-body">
        <div class="flow">
          <RouterLink v-for="item in flows" :key="item.no" :to="item.path">
            <b>{{ item.no }}</b>
            <span>{{ item.name }}</span>
            <small>{{ item.desc }}</small>
          </RouterLink>
        </div>
      </div>
    </section>

    <section class="panel section-gap">
      <div class="panel-head knowledge-panel-head">
        <h2>待办列表</h2>
        <div class="filter-bar dashboard-filter-bar">
          <input v-model="todoQuery.documentName" class="input" placeholder="搜索文档名称" @keyup.enter="searchTodos">
          <select v-model="todoQuery.parseStatus" class="select" @change="searchTodos">
            <option :value="undefined">全部解析状态</option>
            <option value="UPLOADED">已上传</option>
            <option value="PARSE_CHUNKING">解析分片中</option>
            <option value="PARSE_CHUNKED">解析分片完成</option>
            <option value="PROCESS_FAILED">处理失败</option>
          </select>
          <select v-model="todoQuery.reviewStatus" class="select" @change="searchTodos">
            <option :value="undefined">全部审核状态</option>
            <option value="WAIT_AUDIT">待审核</option>
            <option value="AUDIT_PASSED">审核通过</option>
            <option value="AUDIT_REJECTED">审核驳回</option>
          </select>
          <select v-model="todoQuery.publishStatus" class="select" @change="searchTodos">
            <option :value="undefined">全部发布状态</option>
            <option value="UNPUBLISHED">未发布</option>
            <option value="PUBLISHED">已发布</option>
            <option value="OFFLINE">已下线</option>
          </select>
          <el-date-picker
            v-model="todoCreateTimeRange"
            class="dashboard-date-picker"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            @change="searchTodos"
          />
        </div>
        <div class="btns knowledge-filter-actions">
          <button class="btn" @click="searchTodos">查询</button>
          <span class="status amber">共 {{ todoTotal }} 条待办</span>
        </div>
      </div>
      <div class="panel-body">
        <table class="table">
          <thead>
            <tr><th>文档</th><th>知识库</th><th>目录</th><th>解析状态</th><th>审核状态</th><th>发布状态</th><th>下一步</th><th>创建时间</th><th>更新时间</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in todoRows" :key="item.documentId">
              <td>{{ item.documentName }}</td>
              <td>{{ item.knowledgeBaseId }}</td>
              <td>{{ item.directoryId }}</td>
              <td><span class="status" :class="todoTone(item.parseStatus)">{{ item.parseStatus }}</span></td>
              <td>{{ item.reviewStatus }}</td>
              <td>{{ item.publishStatus }}</td>
              <td>{{ item.nextAction }}</td>
              <td>{{ formatDate(item.createTime) }}</td>
              <td>{{ formatDate(item.updateTime) }}</td>
              <td><RouterLink class="link" :to="todoActionTo(item)">处理</RouterLink></td>
            </tr>
            <tr v-if="!todoRows.length"><td colspan="10">暂无待办</td></tr>
          </tbody>
        </table>
        <div class="table-pagination">
          <el-pagination
            v-model:current-page="todoQuery.pageNo"
            v-model:page-size="todoQuery.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="todoTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleTodoPageSizeChange"
            @current-change="loadTodos"
          />
        </div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { getDashboardSummary, pageDashboardTodos } from '../api/dashboard';
import type { DashboardSummaryResponse, DashboardTodoResponse } from '../types/dashboard';

const summary = ref<DashboardSummaryResponse>();
const todoRows = ref<DashboardTodoResponse[]>([]);
const todoTotal = ref(0);
const todoCreateTimeRange = ref<[string, string] | ''>('');
const todoQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  documentName: undefined as string | undefined,
  parseStatus: undefined as string | undefined,
  reviewStatus: undefined as string | undefined,
  publishStatus: undefined as string | undefined,
});

const metrics = computed(() => [
  { label: '知识库', value: String(summary.value?.knowledgeBaseCount ?? 0) },
  { label: '文档总量', value: String(summary.value?.documentCount ?? 0) },
  { label: '待审核', value: String(summary.value?.waitAuditCount ?? 0), tone: 'amber-text' },
  { label: '处理失败', value: String(summary.value?.processFailedCount ?? 0), tone: 'red-text' },
]);

const flows = [
  { no: '01', name: '知识库', desc: '先创建知识空间', path: '/knowledge' },
  { no: '02', name: '目录规则', desc: '在知识库下建目录', path: '/directory' },
  { no: '03', name: '文档上传', desc: '挂到具体目录', path: '/upload' },
  { no: '04', name: '解析处理', desc: 'Parser Router', path: '/document' },
  { no: '05', name: '分片预览', desc: '解析块与 chunk', path: '/document' },
  { no: '06', name: '召回测试', desc: '验证命中来源', path: '/retrieval' },
  { no: '07', name: '审核发布', desc: '启用正式检索', path: '/publish' },
];

function todoTone(status: string) {
  if (status.includes('失败')) {
    return 'red';
  }
  if (status.includes('待') || status.includes('中')) {
    return 'amber';
  }
  return 'blue';
}

function formatDate(value?: string) {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 19);
}

function todoActionTo(item: DashboardTodoResponse) {
  return {
    path: item.actionPath,
    query: {
      knowledgeBaseId: item.knowledgeBaseId,
      directoryId: item.directoryId,
      documentId: item.documentId,
    },
  };
}

async function loadSummary() {
  const result = await getDashboardSummary();
  summary.value = result.data;
}

async function loadTodos() {
  const [createStartDate, createEndDate] = todoCreateTimeRange.value || [];
  const result = await pageDashboardTodos({
    ...todoQuery,
    createStartTime: createStartDate ? `${createStartDate} 00:00:00` : undefined,
    createEndTime: createEndDate ? `${createEndDate} 23:59:59` : undefined,
  });
  todoRows.value = result.data.records;
  todoTotal.value = result.data.total;
}

function searchTodos() {
  todoQuery.pageNo = 1;
  loadTodos();
}

function handleTodoPageSizeChange() {
  todoQuery.pageNo = 1;
  loadTodos();
}

onMounted(async () => {
  await loadSummary();
  await loadTodos();
});
</script>
