<template>
  <section class="main">
    <div class="title"><div><h1>检索测试</h1><div class="sub">对已完成索引的文档做发布前命中验证，测试通过后进入审核发布。</div></div></div>
    <section class="panel">
      <div class="panel-head">
        <div class="filter-bar"><select class="select"><option>全部知识库</option><option>财务共享知识库</option></select><select class="select"><option>全部目录</option><option>报销制度 / 费用标准</option></select><select class="select"><option>全部测试状态</option><option>待测试</option><option>未通过</option><option>已通过</option></select><input class="input" placeholder="搜索文档名称"><input class="input" type="date" aria-label="测试时间"></div>
        <div class="btns"><span class="status amber">待测试 5</span><span class="status red">未通过 2</span><span class="status green">已通过 12</span></div>
      </div>
      <div class="panel-body"><table class="table"><thead><tr><th>文档</th><th>知识库</th><th>目录</th><th>版本</th><th>分片</th><th>索引状态</th><th>测试状态</th><th>最近得分</th><th>测试时间</th><th>修改时间</th><th>操作</th></tr></thead><tbody><tr v-for="item in rows" :key="item.name" :class="{ active: item.active }"><td>{{ item.name }}</td><td>{{ item.kb }}</td><td>{{ item.dir }}</td><td>{{ item.ver }}</td><td>{{ item.chunks }}</td><td><span class="status" :class="item.indexTone">{{ item.indexStatus }}</span></td><td><span class="status" :class="item.testTone">{{ item.testStatus }}</span></td><td>{{ item.score }}</td><td>{{ item.testTime }}</td><td>{{ item.updated }}</td><td><span class="ops"><a v-if="item.canTest" class="link" @click="testVisible = true">{{ item.testAction }}</a><a class="link" @click="recordVisible = true">查看记录</a></span></td></tr></tbody></table></div>
    </section>
    <el-dialog v-model="testVisible" title="检索测试" width="860px">
      <div class="form"><div class="field"><label>测试范围</label><input class="input" value="当前文档 / 当前目录 / 当前知识库"></div><div class="field"><label>测试问题</label><input class="input" value="审批通过后进入哪个环节？"></div><div class="field"><label>TopK</label><select class="select"><option>Top 5</option><option>Top 10</option></select></div></div>
      <div class="btns end-actions"><button class="btn primary">开始测试</button></div>
      <table class="table"><thead><tr><th>排名</th><th>命中分片</th><th>所属结构块</th><th>来源章节</th><th>页码</th><th>相似度</th></tr></thead><tbody><tr class="active"><td>1</td><td>chunk_003</td><td>block_002</td><td>3.2 审批流程</td><td>21-22</td><td>0.92</td></tr><tr><td>2</td><td>chunk_004</td><td>block_002</td><td>3.4 付款处理</td><td>23-24</td><td>0.81</td></tr><tr><td>3</td><td>chunk_016</td><td>block_003</td><td>4.1 财务审核</td><td>36</td><td>0.76</td></tr></tbody></table>
      <div class="detail-card block-gap"><strong>命中内容</strong><br>审批通过后，系统将自动进入财务审核环节。财务审核完成后进入付款处理。</div>
      <template #footer><button class="btn" @click="testVisible = false">关闭</button><button class="btn green" @click="testVisible = false">标记通过</button></template>
    </el-dialog>
    <el-dialog v-model="recordVisible" title="测试记录" width="860px">
      <table class="table"><thead><tr><th>测试时间</th><th>测试问题</th><th>Top1 分片</th><th>得分</th><th>结论</th></tr></thead><tbody><tr class="active"><td>2026-06-24 10:18</td><td>审批通过后进入哪个环节？</td><td>chunk_003</td><td>0.92</td><td><span class="status green">通过</span></td></tr><tr><td>2026-06-23 16:40</td><td>住宿标准如何判断？</td><td>chunk_021</td><td>0.61</td><td><span class="status red">未通过</span></td></tr></tbody></table>
      <template #footer><button class="btn" @click="recordVisible = false">关闭</button></template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const testVisible = ref(false);
const recordVisible = ref(false);
const rows = [
  { active: true, name: '财务共享报账手册V2.0.docx', kb: '财务共享知识库', dir: '报销制度 / 费用标准', ver: 'v2', chunks: 128, indexStatus: '已写入', indexTone: 'green', testStatus: '待测试', testTone: 'amber', score: '-', testTime: '-', updated: '2026-06-24 10:06', canTest: true, testAction: '测试' },
  { name: '住宿标准.csv', kb: '财务共享知识库', dir: '报销制度 / 费用标准', ver: 'v1', chunks: 18, indexStatus: '已写入', indexTone: 'green', testStatus: '未通过', testTone: 'red', score: '0.61', testTime: '2026-06-23 16:40', updated: '2026-06-23 16:42', canTest: true, testAction: '复测' },
  { name: '交通补贴说明.docx', kb: '财务共享知识库', dir: '报销制度 / 审批流程', ver: 'v1', chunks: 26, indexStatus: '已写入', indexTone: 'green', testStatus: '已通过', testTone: 'green', score: '0.88', testTime: '2026-06-22 11:18', updated: '2026-06-22 11:19', canTest: false, testAction: '' },
  { name: '历史费用标准.pdf', kb: '财务共享知识库', dir: '报销制度 / 费用标准', ver: 'v1', chunks: 44, indexStatus: '写入中', indexTone: 'amber', testStatus: '不可测试', testTone: 'amber', score: '-', testTime: '-', updated: '2026-06-24 09:28', canTest: false, testAction: '' },
];
</script>
