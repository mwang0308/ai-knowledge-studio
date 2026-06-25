<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>文档上传</h1>
        <div class="sub">上传文档后创建文档版本和处理任务，并投递 DOCUMENT_PARSE_CHUNK 消息。</div>
      </div>
      <div class="btns"><RouterLink class="btn" to="/directory">返回目录</RouterLink></div>
    </div>

    <div class="upload-layout">
      <section class="panel">
        <div class="panel-head">
          <h2>选择文件</h2>
          <span class="status" :class="selectedFile ? 'green' : 'amber'">{{ selectedFile ? '已选择 1 个文件' : '未选择文件' }}</span>
        </div>
        <div class="panel-body">
          <div class="form-grid upload-form-grid">
            <div class="field">
              <label>知识库</label>
              <select v-model="form.knowledgeBaseId" class="select" @change="loadDirectories">
                <option v-for="item in knowledgeBases" :key="item.id" :value="item.id">{{ item.name }}</option>
              </select>
            </div>
            <div class="field">
              <label>目录</label>
              <select v-model="form.directoryId" class="select">
                <option v-for="item in directoryOptions" :key="item.id" :value="item.id">{{ item.path }}</option>
              </select>
            </div>
          </div>

          <el-upload
            class="upload-drop"
            drag
            :auto-upload="false"
            :limit="1"
            :show-file-list="false"
            :on-change="handleFileChange"
          >
            <div class="upload ready">
              <div>
                <strong>点击或拖拽文件到这里</strong>
                <p>支持 Word / PDF / Markdown / TXT / Excel / CSV，单文件最大 100MB</p>
                <div v-if="selectedFile" class="file-card">
                  <b>{{ selectedFile.name }}</b>
                  <span>{{ fileInfoText }}</span>
                </div>
              </div>
            </div>
          </el-upload>

          <table class="table table-gap">
            <thead><tr><th>待处理文件</th><th>校验状态</th><th>处理方式</th><th>下一步</th></tr></thead>
            <tbody>
              <tr v-if="selectedFile" class="active">
                <td>{{ selectedFile.name }}</td>
                <td><span class="status green">待上传</span></td>
                <td>Parser Router 自动识别</td>
                <td>创建 DOCUMENT_PARSE_CHUNK 任务并发送 MQ</td>
              </tr>
              <tr v-else>
                <td colspan="4">请选择要上传的文档</td>
              </tr>
            </tbody>
          </table>

          <div class="btns end-actions">
            <button class="btn" @click="clearFile">清空</button>
            <button class="btn primary" :disabled="uploading" @click="submitUpload">{{ uploading ? '上传中' : '开始处理' }}</button>
          </div>
        </div>
      </section>

      <aside class="side-summary">
        <section class="panel">
          <div class="panel-head"><h2>归属与任务</h2></div>
          <div class="panel-body">
            <div class="summary-box">
              <h3>上传位置</h3>
              <table class="kv"><tbody><tr><td>知识库</td><td>{{ selectedKnowledgeBaseName }}</td></tr><tr><td>目录</td><td>{{ selectedDirectoryPath }}</td></tr></tbody></table>
            </div>
            <div class="summary-box block-gap">
              <h3>处理任务</h3>
              <table class="kv"><tbody><tr><td>阶段</td><td>DOCUMENT_PARSE_CHUNK</td></tr><tr><td>文件去重</td><td>同知识库同目录按 file_hash 校验</td></tr><tr><td>原始文件</td><td>MinIO original/{file_hash}/</td></tr></tbody></table>
            </div>
            <div v-if="uploadResult" class="summary-box block-gap">
              <h3>投递结果</h3>
              <table class="kv">
                <tbody>
                  <tr><td>文档 ID</td><td>{{ uploadResult.documentId }}</td></tr>
                  <tr><td>版本 ID</td><td>{{ uploadResult.versionId }}</td></tr>
                  <tr><td>任务 ID</td><td>{{ uploadResult.taskId }}</td></tr>
                  <tr><td>MQ 消息</td><td>{{ uploadResult.mqMessageId }}</td></tr>
                  <tr><td>任务状态</td><td>{{ uploadResult.taskStatus }}</td></tr>
                </tbody>
              </table>
            </div>
            <div v-if="taskProgress" class="summary-box block-gap">
              <h3>处理进度</h3>
              <div class="task-progress">
                <div class="task-progress-track"><span :style="{ width: `${taskProgress.progress}%` }"></span></div>
                <div class="task-progress-meta">
                  <b>{{ taskProgress.taskStatus }}</b>
                  <span>{{ taskProgress.progress }}%</span>
                </div>
              </div>
              <table class="kv">
                <tbody>
                  <tr><td>阶段</td><td>{{ taskProgress.stageCode }}</td></tr>
                  <tr><td>错误</td><td>{{ taskProgress.errorMessage || '-' }}</td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { UploadFile } from 'element-plus';
import { treeKnowledgeDirectory } from '../api/directory';
import { getKnowledgeTaskProgress, uploadKnowledgeDocument } from '../api/document';
import { pageKnowledgeBase } from '../api/knowledge';
import type { KnowledgeDirectoryResponse } from '../types/directory';
import type { KnowledgeDocumentUploadResponse, KnowledgeTaskProgressResponse } from '../types/document';
import type { KnowledgeBaseResponse } from '../types/knowledge';

const route = useRoute();
const uploading = ref(false);
const selectedFile = ref<File>();
const uploadResult = ref<KnowledgeDocumentUploadResponse>();
const taskProgress = ref<KnowledgeTaskProgressResponse>();
const knowledgeBases = ref<KnowledgeBaseResponse[]>([]);
const directoryRows = ref<KnowledgeDirectoryResponse[]>([]);
let progressTimer: number | undefined;

const form = reactive<{ knowledgeBaseId?: number; directoryId?: number }>({
  knowledgeBaseId: Number(route.query.knowledgeBaseId) || undefined,
  directoryId: Number(route.query.directoryId) || undefined,
});

const directoryOptions = computed(() => flattenDirectories(directoryRows.value));
const selectedKnowledgeBaseName = computed(() => knowledgeBases.value.find((item) => item.id === form.knowledgeBaseId)?.name || '-');
const selectedDirectoryPath = computed(() => directoryOptions.value.find((item) => item.id === form.directoryId)?.path || '-');
const fileInfoText = computed(() => {
  if (!selectedFile.value) {
    return '';
  }
  return `${selectedFile.value.type || '未知类型'} / ${formatSize(selectedFile.value.size)} / 上传后计算 file_hash`;
});

function flattenDirectories(rows: KnowledgeDirectoryResponse[]) {
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

function formatSize(size: number) {
  if (size >= 1024 * 1024) {
    return `${(size / 1024 / 1024).toFixed(2)}MB`;
  }
  if (size >= 1024) {
    return `${(size / 1024).toFixed(2)}KB`;
  }
  return `${size}B`;
}

function handleFileChange(uploadFile: UploadFile) {
  selectedFile.value = uploadFile.raw;
  uploadResult.value = undefined;
}

function clearFile() {
  selectedFile.value = undefined;
  uploadResult.value = undefined;
  taskProgress.value = undefined;
  stopProgressPolling();
}

async function loadKnowledgeBases() {
  const result = await pageKnowledgeBase({ pageNo: 1, pageSize: 100, status: 1 });
  knowledgeBases.value = result.data.records;
  form.knowledgeBaseId = form.knowledgeBaseId || knowledgeBases.value[0]?.id;
}

async function loadDirectories() {
  form.directoryId = undefined;
  directoryRows.value = [];
  if (!form.knowledgeBaseId) {
    return;
  }
  const result = await treeKnowledgeDirectory({ knowledgeBaseId: form.knowledgeBaseId, status: 1 });
  directoryRows.value = result.data;
  form.directoryId = Number(route.query.directoryId) || directoryOptions.value[0]?.id;
}

async function submitUpload() {
  if (!form.knowledgeBaseId) {
    ElMessage.warning('请选择知识库');
    return;
  }
  if (!form.directoryId) {
    ElMessage.warning('请选择目录');
    return;
  }
  if (!selectedFile.value) {
    ElMessage.warning('请选择上传文件');
    return;
  }

  uploading.value = true;
  try {
    const data = new FormData();
    data.append('knowledgeBaseId', String(form.knowledgeBaseId));
    data.append('directoryId', String(form.directoryId));
    data.append('file', selectedFile.value);
    const result = await uploadKnowledgeDocument(data);
    uploadResult.value = result.data;
    taskProgress.value = undefined;
    startProgressPolling(result.data.taskId);
    ElMessage.success('文档已上传，处理任务已发送到 MQ');
  } finally {
    uploading.value = false;
  }
}

function startProgressPolling(taskId: number) {
  stopProgressPolling();
  loadTaskProgress(taskId);
  progressTimer = window.setInterval(() => loadTaskProgress(taskId), 3000);
}

function stopProgressPolling() {
  if (progressTimer) {
    window.clearInterval(progressTimer);
    progressTimer = undefined;
  }
}

async function loadTaskProgress(taskId: number) {
  const result = await getKnowledgeTaskProgress(taskId);
  taskProgress.value = result.data;
  if (['SUCCESS', 'FAILED'].includes(result.data.taskStatus)) {
    stopProgressPolling();
  }
}

onMounted(async () => {
  await loadKnowledgeBases();
  await loadDirectories();
});

onBeforeUnmount(() => {
  stopProgressPolling();
});
</script>
