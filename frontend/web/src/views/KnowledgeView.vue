<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>知识库管理</h1>
        <div class="sub">知识库是顶层空间。这里仅保留知识库级操作：目录管理、编辑、停用、删除。</div>
      </div>
      <div class="btns"><button class="btn primary" @click="openCreate">新建知识库</button></div>
    </div>

    <section class="panel">
      <div class="panel-head knowledge-panel-head">
        <div class="filter-bar knowledge-filter-bar">
          <input v-model="query.name" class="input" placeholder="搜索知识库名称" @keyup.enter="searchKnowledgeBases">
          <select v-model="query.status" class="select" @change="searchKnowledgeBases">
            <option :value="undefined">全部状态</option>
            <option :value="1">启用</option>
            <option :value="0">停用</option>
          </select>
          <el-date-picker
            v-model="createTimeRange"
            class="knowledge-date-picker"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            @change="searchKnowledgeBases"
          />
        </div>
        <div class="btns knowledge-filter-actions"><button class="btn" @click="searchKnowledgeBases">查询</button><span class="status blue">共 {{ total }} 个知识库</span></div>
      </div>
      <div class="panel-body">
        <table class="table">
          <thead>
            <tr><th>知识库名称</th><th>描述</th><th>文档数</th><th>分片数</th><th>状态</th><th>创建时间</th><th>修改时间</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in rows" :key="item.id" :class="{ active: item.id === activeId }">
              <td>{{ item.name }}</td><td>{{ item.description || '-' }}</td><td>{{ item.documentCount }}</td><td>{{ item.chunkCount }}</td>
              <td><span class="status" :class="item.status === 1 ? 'green' : 'red'">{{ item.statusName }}</span></td>
              <td>{{ formatDate(item.createTime) }}</td><td>{{ formatDate(item.updateTime) }}</td>
              <td><span class="ops"><RouterLink class="link" :to="{ path: '/directory', query: { knowledgeBaseId: item.id } }">目录管理</RouterLink><a class="link" @click="openEdit(item)">编辑</a><a class="link" @click="toggleStatus(item)">{{ item.status === 1 ? '停用' : '启用' }}</a><a class="link" @click="removeKnowledgeBase(item)">删除</a></span></td>
            </tr>
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
            @current-change="loadKnowledgeBases"
          />
        </div>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="知识库配置" width="620px">
      <div class="form">
        <div class="field"><label>知识库名称</label><input v-model="form.name" class="input"></div>
        <div class="field"><label>知识库描述</label><input v-model="form.description" class="input"></div>
      </div>
      <template #footer>
        <button class="btn" @click="dialogVisible = false">取消</button>
        <button class="btn primary" @click="saveKnowledgeBase">保存配置</button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createKnowledgeBase,
  deleteKnowledgeBase,
  disableKnowledgeBase,
  enableKnowledgeBase,
  pageKnowledgeBase,
  updateKnowledgeBase,
} from '../api/knowledge';
import type { KnowledgeBaseResponse } from '../types/knowledge';

const dialogVisible = ref(false);
const editingId = ref<number>();
const activeId = ref<number>();
const total = ref(0);
const createTimeRange = ref<[string, string] | ''>('');
const query = reactive<{ pageNo: number; pageSize: number; name?: string; status?: number }>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined,
});
const form = reactive({
  name: '',
  description: '',
});

const fallbackRows: KnowledgeBaseResponse[] = [
  { id: 1, name: '财务共享知识库', description: '财务共享制度、费用标准和审批流程', status: 1, statusName: '启用', publishedStatus: 0, publishedStatusName: '未发布', documentCount: 286, chunkCount: 1286, createTime: '2026-05-18 00:00:00', updateTime: '2026-06-24 00:00:00' },
  { id: 2, name: '人事制度知识库', description: '人事制度与员工服务知识', status: 1, statusName: '启用', publishedStatus: 0, publishedStatusName: '未发布', documentCount: 164, chunkCount: 620, createTime: '2026-04-26 00:00:00', updateTime: '2026-06-20 00:00:00' },
  { id: 3, name: '法务合同知识库', description: '合同模板和法务条款', status: 0, statusName: '停用', publishedStatus: 0, publishedStatusName: '未发布', documentCount: 72, chunkCount: 310, createTime: '2026-06-02 00:00:00', updateTime: '2026-06-21 00:00:00' },
];

const rows = ref<KnowledgeBaseResponse[]>(fallbackRows);
total.value = fallbackRows.length;
activeId.value = fallbackRows[0]?.id;

function formatDate(value?: string) {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 19);
}

function openCreate() {
  editingId.value = undefined;
  Object.assign(form, { name: '', description: '' });
  dialogVisible.value = true;
}

function openEdit(item: KnowledgeBaseResponse) {
  editingId.value = item.id;
  Object.assign(form, {
    name: item.name,
    description: item.description || '',
  });
  dialogVisible.value = true;
}

function searchKnowledgeBases() {
  query.pageNo = 1;
  loadKnowledgeBases();
}

function handlePageSizeChange() {
  query.pageNo = 1;
  loadKnowledgeBases();
}

async function loadKnowledgeBases() {
  try {
    const [createStartDate, createEndDate] = createTimeRange.value || [];
    const result = await pageKnowledgeBase({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      name: query.name,
      status: query.status,
      createStartTime: createStartDate ? `${createStartDate} 00:00:00` : undefined,
      createEndTime: createEndDate ? `${createEndDate} 23:59:59` : undefined,
    });
    rows.value = result.data.records;
    total.value = result.data.total;
    activeId.value = rows.value[0]?.id;
  } catch {
    const filteredRows = fallbackRows;
    const start = (query.pageNo - 1) * query.pageSize;
    rows.value = filteredRows.slice(start, start + query.pageSize);
    total.value = filteredRows.length;
  }
}

async function saveKnowledgeBase() {
  if (!form.name.trim()) {
    ElMessage.warning('知识库名称不能为空');
    return;
  }
  const payload = {
    name: form.name,
    description: form.description,
  };
  if (editingId.value) {
    await updateKnowledgeBase(editingId.value, payload);
  } else {
    await createKnowledgeBase(payload);
  }
  ElMessage.success('知识库配置已保存');
  dialogVisible.value = false;
  await loadKnowledgeBases();
}

async function toggleStatus(item: KnowledgeBaseResponse) {
  if (item.status === 1) {
    await disableKnowledgeBase(item.id);
    ElMessage.success('知识库已停用');
  } else {
    await enableKnowledgeBase(item.id);
    ElMessage.success('知识库已启用');
  }
  await loadKnowledgeBases();
}

async function removeKnowledgeBase(item: KnowledgeBaseResponse) {
  await ElMessageBox.confirm(`确认删除知识库“${item.name}”？`, '删除知识库', { type: 'warning' });
  await deleteKnowledgeBase(item.id);
  ElMessage.success('知识库已删除');
  await loadKnowledgeBases();
}

onMounted(loadKnowledgeBases);
</script>
