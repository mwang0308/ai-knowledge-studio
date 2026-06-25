<template>
  <section class="main">
    <div class="title"><div><h1>审核发布</h1><div class="sub">只处理已完成文档处理和检索测试的发布审核；测试未通过的文档需要先回到检索测试。</div></div></div>
    <section class="panel">
      <div class="panel-head"><div class="filter-bar"><select class="select"><option>全部知识库</option><option>财务共享知识库</option></select><select class="select"><option>全部目录</option><option>报销制度 / 费用标准</option></select><select class="select"><option>全部审核状态</option><option>待审核</option><option>已驳回</option><option>已通过</option></select><select class="select"><option>全部发布状态</option><option>待发布</option><option>已发布</option><option>已下架</option></select><input class="input" type="date" aria-label="发布时间"></div><div class="btns"><span class="status amber">待审核 18</span><span class="status blue">待发布 6</span><span class="status green">已发布 210</span></div></div>
      <div class="panel-body"><table class="table"><thead><tr><th>文档</th><th>知识库</th><th>目录</th><th>版本</th><th>处理状态</th><th>检索测试</th><th>测试时间</th><th>审核状态</th><th>发布状态</th><th>发布时间</th><th>操作</th></tr></thead><tbody><tr v-for="item in rows" :key="item.name" :class="{ active: item.active }"><td>{{ item.name }}</td><td>{{ item.kb }}</td><td>{{ item.dir }}</td><td>{{ item.ver }}</td><td><span class="status green">完成</span></td><td><span class="status" :class="item.testTone">{{ item.test }}</span></td><td>{{ item.testTime }}</td><td><span class="status" :class="item.auditTone">{{ item.audit }}</span></td><td><span class="status" :class="item.publishTone">{{ item.publish }}</span></td><td>{{ item.publishTime }}</td><td><span class="ops"><a class="link" @click="reviewVisible = true">{{ item.reviewAction }}</a><a v-if="item.canPublish" class="link" @click="publishVisible = true">发布</a><RouterLink v-if="item.needTest" class="link" to="/retrieval">去测试</RouterLink></span></td></tr></tbody></table></div>
    </section>
    <el-dialog v-model="reviewVisible" title="审核详情" width="860px">
      <table class="table"><thead><tr><th>检查项</th><th>结果说明</th><th>状态</th></tr></thead><tbody><tr><td>原文版本</td><td>v2，原始文件与处理产物可追溯</td><td><span class="status green">通过</span></td></tr><tr><td>结构完整</td><td>章节、页码、结构块完整</td><td><span class="status green">通过</span></td></tr><tr><td>分片可追溯</td><td>128 个分片均带来源页码与 block_id</td><td><span class="status green">通过</span></td></tr><tr><td>检索测试</td><td>Top1 命中 chunk_003，相似度 0.92</td><td><span class="status green">通过</span></td></tr></tbody></table>
      <div class="form block-gap"><div class="field"><label>审核意见</label><textarea class="textarea" placeholder="请输入审核意见，驳回时必填。"></textarea></div></div>
      <template #footer><button class="btn" @click="reviewVisible = false">关闭</button><button class="btn red">驳回</button><button class="btn green" @click="reviewVisible = false">审核通过</button></template>
    </el-dialog>
    <el-dialog v-model="publishVisible" title="发布确认" width="620px">
      <table class="kv"><tbody><tr><td>文档</td><td>财务共享报账手册V2.0.docx</td></tr><tr><td>版本</td><td>v2</td></tr><tr><td>发布范围</td><td>财务共享知识库 / 报销制度 / 费用标准</td></tr><tr><td>索引状态</td><td>ES / Milvus 已写入，enabled=false</td></tr></tbody></table>
      <div class="detail-card block-gap">发布后该版本分片会启用正式检索，旧版本保持可追溯但不再命中。</div>
      <template #footer><button class="btn" @click="publishVisible = false">取消</button><button class="btn primary" @click="publishVisible = false">确认发布</button></template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const reviewVisible = ref(false);
const publishVisible = ref(false);
const rows = [
  { active: true, name: '财务共享报账手册V2.0.docx', kb: '财务共享知识库', dir: '报销制度 / 费用标准', ver: 'v2', test: '通过', testTone: 'green', testTime: '2026-06-24 10:18', audit: '待审核', auditTone: 'amber', publish: '未发布', publishTone: 'amber', publishTime: '-', reviewAction: '审核', canPublish: false, needTest: false },
  { name: '差旅审批说明.pdf', kb: '财务共享知识库', dir: '报销制度 / 审批流程', ver: 'v1', test: '通过', testTone: 'green', testTime: '2026-06-23 15:12', audit: '已通过', auditTone: 'green', publish: '待发布', publishTone: 'blue', publishTime: '-', reviewAction: '查看', canPublish: true, needTest: false },
  { name: '住宿标准.csv', kb: '财务共享知识库', dir: '报销制度 / 费用标准', ver: 'v1', test: '未通过', testTone: 'red', testTime: '2026-06-23 16:40', audit: '待处理', auditTone: 'amber', publish: '不可发布', publishTone: 'amber', publishTime: '-', reviewAction: '查看', canPublish: false, needTest: true },
  { name: '交通补贴说明.docx', kb: '财务共享知识库', dir: '报销制度 / 审批流程', ver: 'v1', test: '通过', testTone: 'green', testTime: '2026-06-22 11:18', audit: '已通过', auditTone: 'green', publish: '已发布', publishTone: 'green', publishTime: '2026-06-22 14:30', reviewAction: '查看', canPublish: false, needTest: false },
];
</script>
