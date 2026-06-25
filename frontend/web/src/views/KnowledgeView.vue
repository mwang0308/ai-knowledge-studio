<template>
  <section class="main">
    <div class="title">
      <div>
        <h1>知识库管理</h1>
        <div class="sub">知识库是顶层空间。这里仅保留知识库级操作：目录管理、编辑、停用、删除。</div>
      </div>
      <div class="btns"><button class="btn primary" @click="dialogVisible = true">新建知识库</button></div>
    </div>

    <section class="panel">
      <div class="panel-head">
        <div class="filter-bar">
          <input class="input" placeholder="搜索知识库名称">
          <select class="select"><option>全部状态</option><option>启用</option><option>配置中</option><option>停用</option></select>
          <select class="select"><option>全部负责人</option><option>AI 平台负责人</option><option>人事制度管理员</option></select>
          <input class="input" type="date" aria-label="创建时间">
        </div>
        <div class="btns"><span class="status blue">共 4 个知识库</span><span class="status amber">58 个待审核文档</span></div>
      </div>
      <div class="panel-body">
        <table class="table">
          <thead>
            <tr><th>知识库名称</th><th>负责人</th><th>目录</th><th>文档</th><th>已发布</th><th>待审核</th><th>默认分片</th><th>审核</th><th>状态</th><th>创建时间</th><th>修改时间</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in rows" :key="item.name" :class="{ active: item.active }">
              <td>{{ item.name }}</td><td>{{ item.owner }}</td><td>{{ item.dirs }}</td><td>{{ item.docs }}</td><td>{{ item.published }}</td><td>{{ item.audit }}</td><td>{{ item.chunk }}</td><td>{{ item.rule }}</td>
              <td><span class="status" :class="item.tone">{{ item.status }}</span></td>
              <td>{{ item.created }}</td><td>{{ item.updated }}</td>
              <td><span class="ops"><RouterLink class="link" to="/directory">目录管理</RouterLink><a class="link" @click="dialogVisible = true">编辑</a><a class="link">停用</a><a class="link">删除</a></span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="知识库配置" width="620px">
      <div class="form">
        <div class="field"><label>知识库名称</label><input class="input" value="财务共享知识库"></div>
        <div class="field"><label>负责人</label><input class="input" value="AI 平台负责人"></div>
        <div class="field"><label>默认分片配置</label><select class="select"><option>财务制度默认</option></select></div>
        <div class="field"><label>默认审核规则</label><select class="select"><option>需要审核</option></select></div>
        <div class="field"><label>发布策略</label><input class="input" value="处理完成后先写索引但 enabled=false，审核发布后启用"></div>
      </div>
      <template #footer>
        <button class="btn" @click="dialogVisible = false">取消</button>
        <button class="btn primary" @click="dialogVisible = false">保存配置</button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const dialogVisible = ref(false);

const rows = [
  { active: true, name: '财务共享知识库', owner: 'AI 平台负责人', dirs: 12, docs: 286, published: 210, audit: 18, chunk: '财务制度默认', rule: '需要审核', status: '启用', tone: 'green', created: '2026-05-18', updated: '2026-06-24' },
  { name: '人事制度知识库', owner: '人事制度管理员', dirs: 9, docs: 164, published: 121, audit: 7, chunk: '制度文档分片', rule: '需要审核', status: '启用', tone: 'green', created: '2026-04-26', updated: '2026-06-20' },
  { name: '法务合同知识库', owner: '法务运营', dirs: 5, docs: 72, published: 41, audit: 12, chunk: '合同条款分片', rule: '需要审核', status: '配置中', tone: 'amber', created: '2026-06-02', updated: '2026-06-21' },
  { name: '客服知识库', owner: '客服知识管理员', dirs: 18, docs: 764, published: 562, audit: 21, chunk: '问答文档分片', rule: '抽检审核', status: '启用', tone: 'green', created: '2026-03-12', updated: '2026-06-23' },
];
</script>
