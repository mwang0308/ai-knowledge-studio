<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>目录管理</h1>
        <div class="sub">目录用于组织知识库下的文档归属。文档上传、文档列表从目录进入。</div>
      </div>
      <div class="btns">
        <button class="btn primary" @click="openCreate">新增目录</button>
        <RouterLink class="btn" to="/knowledge">返回知识库</RouterLink>
      </div>
    </div>

    <section class="panel">
      <div class="panel-head">
        <div class="filter-bar directory-filter-bar">
          <select v-model="query.knowledgeBaseId" class="select" @change="searchDirectories">
            <option :value="undefined">全部知识库</option>
            <option v-for="item in knowledgeBases" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
          <input v-model="query.description" class="input" placeholder="搜索目录描述" @keyup.enter="searchDirectories">
          <select v-model="query.status" class="select" @change="searchDirectories">
            <option :value="undefined">全部状态</option>
            <option :value="1">启用</option>
            <option :value="0">停用</option>
          </select>
          <el-date-picker
            v-model="createTimeRange"
            class="directory-date-picker"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            @change="searchDirectories"
          />
        </div>
        <div class="btns">
          <button class="btn" @click="searchDirectories">查询</button>
          <span class="status blue">共 {{ flatRows.length }} 个目录</span>
        </div>
      </div>
      <div class="panel-body">
        <table class="table">
          <thead>
            <tr><th>目录名称</th><th>所属知识库</th><th>目录描述</th><th>层级</th><th>排序</th><th>状态</th><th>创建时间</th><th>修改时间</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in flatRows" :key="item.id" :class="{ active: item.id === activeId }">
              <td>
                <span class="directory-name-cell">
                  <span class="directory-indent" :style="{ width: `${(item.level - 1) * 18}px` }"></span>
                  <span>{{ item.name }}</span>
                </span>
              </td>
              <td>{{ knowledgeBaseNameMap.get(item.knowledgeBaseId) || '-' }}</td>
              <td>{{ item.description || '-' }}</td>
              <td>{{ item.level }}</td>
              <td>{{ item.sortOrder }}</td>
              <td><span class="status" :class="item.status === 1 ? 'green' : 'red'">{{ item.statusName }}</span></td>
              <td>{{ formatDate(item.createTime) }}</td>
              <td>{{ formatDate(item.updateTime) }}</td>
              <td>
                <span class="ops">
                  <RouterLink class="link" to="/upload">上传文档</RouterLink>
                  <RouterLink class="link" to="/document">文档列表</RouterLink>
                  <a class="link" @click="openEdit(item)">编辑</a>
                  <a class="link" @click="toggleStatus(item)">{{ item.status === 1 ? '停用' : '启用' }}</a>
                  <a class="link" @click="removeDirectory(item)">删除</a>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="目录配置" width="620px">
      <div class="form">
        <div class="field">
          <label>所属知识库</label>
          <select v-model="form.knowledgeBaseId" class="select">
            <option v-for="item in knowledgeBases" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </div>
        <div class="field">
          <label>上级目录</label>
          <select v-model="form.parentId" class="select">
            <option :value="undefined">一级目录</option>
            <option v-for="item in parentOptions" :key="item.id" :value="item.id">{{ item.path }}</option>
          </select>
        </div>
        <div class="field"><label>目录名称</label><input v-model="form.name" class="input"></div>
        <div class="field"><label>目录描述</label><input v-model="form.description" class="input"></div>
        <div class="field"><label>排序值</label><input v-model.number="form.sortOrder" class="input" type="number"></div>
      </div>
      <template #footer>
        <button class="btn" @click="dialogVisible = false">取消</button>
        <button class="btn primary" @click="saveDirectory">保存配置</button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createKnowledgeDirectory,
  deleteKnowledgeDirectory,
  disableKnowledgeDirectory,
  enableKnowledgeDirectory,
  treeKnowledgeDirectory,
  updateKnowledgeDirectory,
} from '../api/directory';
import { pageKnowledgeBase } from '../api/knowledge';
import type { KnowledgeDirectoryResponse } from '../types/directory';
import type { KnowledgeBaseResponse } from '../types/knowledge';

const dialogVisible = ref(false);
const route = useRoute();
const editingId = ref<number>();
const activeId = ref<number>();
const treeRows = ref<KnowledgeDirectoryResponse[]>([]);
const knowledgeBases = ref<KnowledgeBaseResponse[]>([]);
const createTimeRange = ref<[string, string] | ''>('');

const query = reactive<{ knowledgeBaseId?: number; description?: string; status?: number }>({
  knowledgeBaseId: undefined,
  description: undefined,
  status: undefined,
});

const form = reactive<{ knowledgeBaseId?: number; parentId?: number; name: string; description: string; sortOrder: number }>({
  knowledgeBaseId: undefined,
  parentId: undefined,
  name: '',
  description: '',
  sortOrder: 0,
});

const flatRows = computed(() => flattenTree(treeRows.value));
const knowledgeBaseNameMap = computed(() => new Map(knowledgeBases.value.map((item) => [item.id, item.name])));
const parentOptions = computed(() => flatRows.value.filter((item) => item.id !== editingId.value && item.knowledgeBaseId === form.knowledgeBaseId));

function flattenTree(rows: KnowledgeDirectoryResponse[]) {
  const result: KnowledgeDirectoryResponse[] = [];
  const walk = (items: KnowledgeDirectoryResponse[]) => {
    items.forEach((item) => {
      result.push(item);
      if (item.children?.length) {
        walk(item.children);
      }
    });
  };
  walk(rows);
  return result;
}

function formatDate(value?: string) {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 19);
}

function openCreate() {
  editingId.value = undefined;
  Object.assign(form, {
    knowledgeBaseId: query.knowledgeBaseId,
    parentId: undefined,
    name: '',
    description: '',
    sortOrder: 0,
  });
  if (!form.knowledgeBaseId) {
    form.knowledgeBaseId = knowledgeBases.value[0]?.id;
  }
  dialogVisible.value = true;
}

function openEdit(item: KnowledgeDirectoryResponse) {
  editingId.value = item.id;
  Object.assign(form, {
    knowledgeBaseId: item.knowledgeBaseId,
    parentId: item.parentId,
    name: item.name,
    description: item.description || '',
    sortOrder: item.sortOrder,
  });
  dialogVisible.value = true;
}

function searchDirectories() {
  loadDirectories();
}

async function loadKnowledgeBases() {
  const result = await pageKnowledgeBase({ pageNo: 1, pageSize: 100, status: 1 });
  knowledgeBases.value = result.data.records;
  if (route.query.knowledgeBaseId) {
    const routeKnowledgeBaseId = Number(route.query.knowledgeBaseId);
    query.knowledgeBaseId = knowledgeBases.value.some((item) => item.id === routeKnowledgeBaseId) ? routeKnowledgeBaseId : undefined;
  }
  if (!knowledgeBases.value.length) {
    treeRows.value = [];
    ElMessage.warning('请先创建并启用知识库');
  }
}

async function loadDirectories() {
  const [createStartDate, createEndDate] = createTimeRange.value || [];
  const result = await treeKnowledgeDirectory({
    knowledgeBaseId: query.knowledgeBaseId,
    description: query.description,
    status: query.status,
    createStartTime: createStartDate ? `${createStartDate} 00:00:00` : undefined,
    createEndTime: createEndDate ? `${createEndDate} 23:59:59` : undefined,
  });
  treeRows.value = result.data;
  activeId.value = flatRows.value[0]?.id;
}

async function saveDirectory() {
  if (!form.knowledgeBaseId) {
    ElMessage.warning('请选择所属知识库');
    return;
  }
  if (!form.name.trim()) {
    ElMessage.warning('目录名称不能为空');
    return;
  }
  const payload = {
    knowledgeBaseId: form.knowledgeBaseId,
    parentId: form.parentId,
    name: form.name,
    description: form.description,
    sortOrder: form.sortOrder,
  };
  if (editingId.value) {
    await updateKnowledgeDirectory(editingId.value, payload);
  } else {
    await createKnowledgeDirectory(payload);
  }
  ElMessage.success('目录配置已保存');
  dialogVisible.value = false;
  query.knowledgeBaseId = form.knowledgeBaseId;
  await loadDirectories();
}

async function toggleStatus(item: KnowledgeDirectoryResponse) {
  if (item.status === 1) {
    await disableKnowledgeDirectory(item.id);
    ElMessage.success('目录已停用');
  } else {
    await enableKnowledgeDirectory(item.id);
    ElMessage.success('目录已启用');
  }
  await loadDirectories();
}

async function removeDirectory(item: KnowledgeDirectoryResponse) {
  await ElMessageBox.confirm(`确认删除目录“${item.name}”？`, '删除目录', { type: 'warning' });
  await deleteKnowledgeDirectory(item.id);
  ElMessage.success('目录已删除');
  await loadDirectories();
}

onMounted(async () => {
  try {
    await loadKnowledgeBases();
    await loadDirectories();
  } catch {
    treeRows.value = [];
  }
});
</script>
