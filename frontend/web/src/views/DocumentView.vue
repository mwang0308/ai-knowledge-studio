<template>
  <section class="main">
    <div class="title">
      <div><h1>文档详情 / 结构块与知识分片</h1><div class="sub">结构块是大文件处理单元，知识分片是结构块处理后的检索单元；两者通过 block_id 关联。</div></div>
      <div class="btns"><RouterLink class="btn" to="/upload">重新上传</RouterLink><button class="btn">重新处理</button><RouterLink class="btn primary" to="/retrieval">检索测试</RouterLink></div>
    </div>

    <section class="panel section-gap-bottom">
      <div class="panel-head"><h2>财务共享报账手册V2.0.docx</h2><div class="btns"><span class="status green">DOCUMENT_EMBEDDING_INDEX 完成</span><span class="status amber">待审核</span><span class="status blue">当前版本 v2</span><span class="status blue">结构块 4 / 分片 128</span></div></div>
    </section>

    <div class="grid tri">
      <section class="panel">
        <div class="panel-head"><h2>文档结构</h2></div>
        <div class="panel-body"><div class="tree"><div class="tree-title">▼ 1. 总则</div><div class="tree-item">1.1 目的</div><div class="tree-item">1.2 适用范围</div><div class="tree-title">▼ 3. 报销流程</div><div class="tree-item">3.1 报销申请</div><div class="tree-item active">3.2 审批流程</div><div class="tree-item">3.3 财务审核</div><div class="tree-title">▼ 4. 费用标准</div><div class="tree-item">表格：差旅费用标准</div></div></div>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>处理单元与分片产物</h2><div class="view-switch"><button :class="{ 'is-active': activeView === 'blocks' }" @click="activeView = 'blocks'">结构块记录</button><button :class="{ 'is-active': activeView === 'chunks' }" @click="activeView = 'chunks'">知识分片</button></div></div>
        <div class="panel-body">
          <div class="relation-note">处理链路：文档解析后先形成结构块 block，再由结构块生成一个或多个 chunk。异常通常先定位到结构块，再查看受影响的分片。</div>
          <table v-if="activeView === 'blocks'" class="table">
            <thead><tr><th>结构块</th><th>拆分方式</th><th>来源范围</th><th>状态</th><th>关联分片</th><th>操作</th></tr></thead>
            <tbody><tr v-for="item in blocks" :key="item.id" :class="{ active: item.active }"><td><a class="link" @click="activeView = 'blocks'">{{ item.id }}</a></td><td>页码范围</td><td>{{ item.range }}</td><td><span class="status" :class="item.tone">{{ item.status }}</span></td><td>{{ item.chunks }}</td><td><a class="link" @click="activeView = item.status === '失败' ? 'blocks' : 'chunks'">{{ item.action }}</a></td></tr></tbody>
          </table>
          <table v-else class="table">
            <thead><tr><th>分片</th><th>所属结构块</th><th>来源章节</th><th>摘要</th><th>Token</th></tr></thead>
            <tbody><tr v-for="item in chunks" :key="item.id" :class="{ active: item.active }"><td><a class="link" @click="activeView = 'chunks'">{{ item.id }}</a></td><td>{{ item.block }}</td><td>{{ item.chapter }}</td><td>{{ item.summary }}</td><td>{{ item.token }}</td></tr></tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>{{ detail.title }}</h2></div>
        <div class="panel-body">
          <table class="kv"><tbody><tr v-for="row in detail.rows" :key="row.label"><td>{{ row.label }}</td><td>{{ row.value }}</td></tr></tbody></table>
          <div class="chunk-text block-gap">{{ detail.text }}</div>
          <div class="detail-card block-gap"><strong>版本与任务</strong><br>重新上传相同 file_hash 不生成新版本；修改分片配置后重新处理会生成新版本。</div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

const activeView = ref<'blocks' | 'chunks'>('blocks');
const blocks = [
  { id: 'block_001', range: '第 1-20 页', status: '成功', tone: 'green', chunks: 36, action: '看分片' },
  { id: 'block_002', range: '第 21-40 页', status: '成功', tone: 'green', chunks: 42, action: '看分片', active: true },
  { id: 'block_003', range: '第 41-60 页', status: '成功', tone: 'green', chunks: 31, action: '看分片' },
  { id: 'block_004', range: '第 61-80 页', status: '失败', tone: 'red', chunks: 0, action: '重试' },
];
const chunks = [
  { id: 'chunk_003', block: 'block_002', chapter: '3.2 审批流程', summary: '审批通过后进入财务审核...', token: 486, active: true },
  { id: 'chunk_004', block: 'block_002', chapter: '3.2 审批流程', summary: '审批驳回后补充材料...', token: 398 },
  { id: 'chunk_005', block: 'block_002', chapter: '4. 费用标准', summary: '表格：城市、岗位、住宿标准...', token: 520 },
];
const detail = computed(() => activeView.value === 'blocks'
  ? { title: '结构块详情', rows: [{ label: '结构块', value: 'block_002' }, { label: '来源范围', value: '第 21-40 页' }, { label: '识别结构', value: '3. 报销流程 / 3.2 审批流程' }, { label: '关联分片', value: 'chunk_003 / chunk_004 / chunk_005' }, { label: '处理状态', value: '成功' }], text: 'block_002 是大文件处理过程中的中间单元，用于定位解析范围和处理异常。它生成多个知识分片，但不会直接进入正式问答检索。' }
  : { title: '知识分片详情', rows: [{ label: '分片 ID', value: 'chunk_003' }, { label: '所属结构块', value: 'block_002' }, { label: '来源章节', value: '3.2 审批流程' }, { label: '来源页码', value: '第 21-22 页' }, { label: '索引状态', value: 'ES / Milvus 已写入，enabled=false' }], text: '审批通过后，系统将自动进入财务审核环节。财务审核人员需要核对票据真实性、费用标准、审批链路是否完整。' });
</script>
