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
                <td>{{ isPdfFile ? `PDF / ${selectedParserLabel}` : 'Parser Router 自动识别' }}</td>
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
            <section v-if="isPdfFile" class="parser-config parser-config-side" aria-labelledby="pdf-parser-title">
              <div class="parser-config-copy">
                <div class="parser-config-kicker">
                  <span>PDF 分片配置</span>
                  <span class="status blue">{{ selectedParserLabel }}</span>
                </div>
                <h3 id="pdf-parser-title">选择解析器</h3>
                <p>本次选择会随文档版本保存，并统一生成目录、解析块和分片。</p>
              </div>
              <div class="parser-options" role="radiogroup" aria-label="PDF 解析器">
                <label
                  v-for="item in parserOptions"
                  :key="item.value"
                  class="parser-option"
                  :class="{ selected: form.parserType === item.value }"
                >
                  <input v-model="form.parserType" type="radio" name="parserType" :value="item.value" />
                  <span class="parser-option-main">
                    <b>{{ item.label }}</b>
                    <small>{{ item.description }}</small>
                  </span>
                  <span v-if="item.recommended" class="parser-default-mark">推荐</span>
                </label>
              </div>
            </section>
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
            <div v-if="uploadResult || taskProgress" class="summary-box task-progress-card block-gap">
              <div class="task-progress-heading">
                <h3>处理进度</h3>
                <span class="task-live-dot" :class="{ done: taskProgress?.taskStatus === 'SUCCESS', failed: taskProgress?.taskStatus === 'FAILED' }"></span>
              </div>
              <div class="task-progress">
                <div class="task-progress-track" :class="{ active: progressRunning }">
                  <span :style="{ width: `${visualProgress}%` }"></span>
                </div>
                <div class="task-progress-meta">
                  <b>{{ progressStatusText }}</b>
                  <span>{{ visualProgress }}%</span>
                </div>
                <p class="task-progress-message">{{ progressMessage }}</p>
              </div>
              <table class="kv">
                <tbody>
                  <tr><td>阶段</td><td>{{ taskProgress?.stageCode || '等待任务消费' }}</td></tr>
                  <tr><td>错误</td><td>{{ taskProgress?.errorMessage || pollingError || '-' }}</td></tr>
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
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { UploadFile } from 'element-plus';
import { treeKnowledgeDirectory } from '../api/directory';
import { getKnowledgeTaskProgress, reprocessKnowledgeDocument, uploadKnowledgeDocument } from '../api/document';
import { pageKnowledgeBase } from '../api/knowledge';
import type { KnowledgeDirectoryResponse } from '../types/directory';
import type { KnowledgeDocumentUploadResponse, KnowledgeTaskProgressResponse } from '../types/document';
import type { KnowledgeBaseResponse } from '../types/knowledge';

const route = useRoute();
const router = useRouter();
const uploading = ref(false);
const selectedFile = ref<File>();
const uploadResult = ref<KnowledgeDocumentUploadResponse>();
const taskProgress = ref<KnowledgeTaskProgressResponse>();
const knowledgeBases = ref<KnowledgeBaseResponse[]>([]);
const directoryRows = ref<KnowledgeDirectoryResponse[]>([]);
const visualProgress = ref(0);
const pollingError = ref('');
let progressTimer: number | undefined;
let progressAnimationTimer: number | undefined;
let pollingInFlight = false;

const parserOptions = [
  { value: 'docling', label: 'Docling', description: '结构与表格识别均衡，作为企业文档默认方案', recommended: true },
  { value: 'mineru', label: 'MinerU', description: '适合复杂版面、公式或扫描型 PDF', recommended: false },
  { value: 'pdf', label: 'PDF 原生', description: '本地版面文本提取，适合文本型 PDF', recommended: false },
] as const;

const form = reactive<{ knowledgeBaseId?: number; directoryId?: number; parserType: string }>({
  knowledgeBaseId: Number(route.query.knowledgeBaseId) || undefined,
  directoryId: Number(route.query.directoryId) || undefined,
  parserType: 'docling',
});

const directoryOptions = computed(() => flattenDirectories(directoryRows.value));
const selectedKnowledgeBaseName = computed(() => knowledgeBases.value.find((item) => item.id === form.knowledgeBaseId)?.name || '-');
const selectedDirectoryPath = computed(() => directoryOptions.value.find((item) => item.id === form.directoryId)?.path || '-');
const isPdfFile = computed(() => selectedFile.value?.name.toLowerCase().endsWith('.pdf') ?? false);
const selectedParserLabel = computed(() => parserOptions.find((item) => item.value === form.parserType)?.label || 'Docling');
const progressRunning = computed(() => !taskProgress.value || !['SUCCESS', 'FAILED'].includes(taskProgress.value.taskStatus));
const progressStatusText = computed(() => {
  const status = taskProgress.value?.taskStatus;
  if (status === 'SUCCESS') return '处理完成';
  if (status === 'FAILED') return '处理失败';
  if (status === 'RUNNING' || status === 'PROCESSING') return '正在解析文档';
  return '任务排队中';
});
const progressMessage = computed(() => {
  if (pollingError.value) return '进度同步暂时中断，系统正在自动重试。';
  if (taskProgress.value?.taskStatus === 'SUCCESS') return '目录结构、解析块和分片数据已生成。';
  if (taskProgress.value?.taskStatus === 'FAILED') return '处理未完成，可查看错误后重新提交。';
  if (visualProgress.value >= 92) return '正在保存解析产物并完成任务回调…';
  if (visualProgress.value >= 68) return '结构识别完成，正在生成分片数据…';
  if (visualProgress.value >= 20) return `${selectedParserLabel.value} 正在识别版面与目录结构…`;
  return '任务已投递，正在等待处理服务消费…';
});
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
  form.parserType = 'docling';
  uploadResult.value = undefined;
}

function clearFile() {
  selectedFile.value = undefined;
  uploadResult.value = undefined;
  taskProgress.value = undefined;
  visualProgress.value = 0;
  pollingError.value = '';
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
    if (uploadResult.value && taskProgress.value?.taskStatus === 'FAILED') {
      const result = await reprocessKnowledgeDocument(uploadResult.value.documentId);
      uploadResult.value = result.data;
      taskProgress.value = undefined;
      visualProgress.value = 4;
      startProgressPolling(result.data.taskId);
      ElMessage.success('已重新提交处理任务');
      return;
    }

    const data = new FormData();
    data.append('knowledgeBaseId', String(form.knowledgeBaseId));
    data.append('directoryId', String(form.directoryId));
    data.append('file', selectedFile.value);
    if (isPdfFile.value) {
      data.append('parserType', form.parserType);
    }
    const result = await uploadKnowledgeDocument(data);
    uploadResult.value = result.data;
    taskProgress.value = undefined;
    visualProgress.value = 4;
    startProgressPolling(result.data.taskId);
    ElMessage.success('文档已上传，处理任务已发送到 MQ');
  } finally {
    uploading.value = false;
  }
}

function startProgressPolling(taskId: string) {
  stopProgressPolling();
  startProgressAnimation();
  void loadTaskProgress(taskId);
  progressTimer = window.setInterval(() => void loadTaskProgress(taskId), 2000);
}

function stopProgressPolling() {
  if (progressTimer) {
    window.clearInterval(progressTimer);
    progressTimer = undefined;
  }
  if (progressAnimationTimer) {
    window.clearInterval(progressAnimationTimer);
    progressAnimationTimer = undefined;
  }
}

async function loadTaskProgress(taskId: string) {
  if (pollingInFlight) return;
  pollingInFlight = true;
  try {
    const result = await getKnowledgeTaskProgress(taskId);
    pollingError.value = '';
    taskProgress.value = result.data;
    visualProgress.value = Math.max(visualProgress.value, result.data.progress);
    if (result.data.taskStatus === 'SUCCESS') {
      visualProgress.value = 100;
      stopProgressPolling();
      ElMessage.success('文档处理完成，正在进入文档列表');
      window.setTimeout(() => {
        void router.push({
          path: '/document',
          query: {
            knowledgeBaseId: form.knowledgeBaseId,
            directoryId: form.directoryId,
          },
        });
      }, 650);
      return;
    }
    if (result.data.taskStatus === 'FAILED') {
      stopProgressPolling();
    }
  } catch (error) {
    pollingError.value = error instanceof Error ? error.message : '获取任务进度失败';
  } finally {
    pollingInFlight = false;
  }
}

function startProgressAnimation() {
  progressAnimationTimer = window.setInterval(() => {
    if (!progressRunning.value) return;
    const actual = taskProgress.value?.progress ?? 4;
    const cap = taskProgress.value?.taskStatus === 'RUNNING' ? Math.min(96, actual + 8) : Math.min(16, actual + 6);
    if (visualProgress.value < cap) visualProgress.value += 1;
  }, 650);
}

onMounted(async () => {
  await loadKnowledgeBases();
  await loadDirectories();
});

onBeforeUnmount(() => {
  stopProgressPolling();
});
</script>
