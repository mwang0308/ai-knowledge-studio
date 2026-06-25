<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>知识库工作台</h1>
        <div class="sub">首页仅展示整体状态、流程进度和待办概览，不承载具体操作。</div>
      </div>
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
        <span class="status blue">层级：知识库 → 目录 → 文档 → 结构块 → 分片</span>
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
      <div class="panel-head">
        <h2>待办列表</h2>
        <span class="status amber">选择文档后进入对应页面处理</span>
      </div>
      <div class="panel-body">
        <table class="table">
          <thead>
            <tr><th>文档</th><th>知识库</th><th>目录</th><th>当前状态</th><th>下一步</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in todos" :key="item.document">
              <td>{{ item.document }}</td>
              <td>{{ item.kb }}</td>
              <td>{{ item.dir }}</td>
              <td><span class="status" :class="item.tone">{{ item.status }}</span></td>
              <td>{{ item.next }}</td>
              <td><RouterLink class="link" :to="item.path">{{ item.action }}</RouterLink></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
const metrics = [
  { label: '知识库', value: '8' },
  { label: '文档总量', value: '1,286' },
  { label: '待审核', value: '18', tone: 'amber-text' },
  { label: '处理失败', value: '6', tone: 'red-text' },
];

const flows = [
  { no: '01', name: '知识库', desc: '先创建知识空间', path: '/knowledge' },
  { no: '02', name: '目录规则', desc: '在知识库下建目录', path: '/directory' },
  { no: '03', name: '文档上传', desc: '挂到具体目录', path: '/upload' },
  { no: '04', name: '解析处理', desc: 'Parser Router', path: '/document' },
  { no: '05', name: '分片预览', desc: '结构块与 chunk', path: '/document' },
  { no: '06', name: '检索测试', desc: '验证命中来源', path: '/retrieval' },
  { no: '07', name: '审核发布', desc: '启用正式检索', path: '/publish' },
];

const todos = [
  { document: '财务共享报账手册V2.0.docx', kb: '财务共享知识库', dir: '报销制度', status: '待审核', tone: 'amber', next: '审核发布', action: '处理', path: '/publish' },
  { document: '住宿标准.csv', kb: '财务共享知识库', dir: '费用标准', status: '待检索测试', tone: 'amber', next: '检索测试', action: '测试', path: '/retrieval' },
  { document: '历史费用标准.pdf', kb: '财务共享知识库', dir: '费用标准', status: '处理失败', tone: 'red', next: '查看日志', action: '查看', path: '/document' },
];
</script>
