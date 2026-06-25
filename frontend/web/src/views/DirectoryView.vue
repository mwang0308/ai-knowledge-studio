<template>
  <section class="main">
    <div class="title">
      <div><h1>目录管理</h1><div class="sub">当前知识库：财务共享知识库。这里保留目录级操作：上传文档、文档列表、编辑、停用、删除。</div></div>
      <div class="btns"><button class="btn primary" @click="dialogVisible = true">新增目录</button><RouterLink class="btn" to="/knowledge">返回知识库</RouterLink></div>
    </div>
    <section class="panel">
      <div class="panel-head">
        <div class="filter-bar"><input class="input" placeholder="搜索目录路径"><select class="select"><option>全部状态</option><option>启用</option><option>停用</option></select><select class="select"><option>全部负责人</option><option>财务标准管理员</option><option>财务流程管理员</option></select><input class="input" type="date" aria-label="创建时间"></div>
        <div class="btns"><span class="status blue">财务共享知识库</span><span class="status amber">6 个目录待审核</span></div>
      </div>
      <div class="panel-body">
        <table class="table">
          <thead><tr><th>目录路径</th><th>负责人</th><th>文档</th><th>已发布</th><th>待审核</th><th>默认分片</th><th>审核规则</th><th>状态</th><th>创建时间</th><th>修改时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="item in rows" :key="item.path" :class="{ active: item.active }">
              <td>{{ item.path }}</td><td>{{ item.owner }}</td><td>{{ item.docs }}</td><td>{{ item.published }}</td><td>{{ item.audit }}</td><td>{{ item.chunk }}</td><td>{{ item.rule }}</td>
              <td><span class="status green">启用</span></td><td>{{ item.created }}</td><td>{{ item.updated }}</td>
              <td><span class="ops"><RouterLink class="link" to="/upload">上传文档</RouterLink><RouterLink class="link" to="/document">文档列表</RouterLink><a class="link" @click="dialogVisible = true">编辑</a><a class="link">停用</a><a class="link">删除</a></span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="目录配置" width="620px">
      <div class="form">
        <div class="field"><label>所属知识库</label><input class="input" value="财务共享知识库"></div>
        <div class="field"><label>目录路径</label><input class="input" value="报销制度 / 费用标准"></div>
        <div class="field"><label>目录负责人</label><input class="input" value="财务标准管理员"></div>
        <div class="field"><label>默认分片配置</label><select class="select"><option>表格文档分片配置</option></select></div>
        <div class="field"><label>默认审核规则</label><select class="select"><option>需要审核</option></select></div>
        <div class="field"><label>审核人/组</label><input class="input" value="财务制度审核组"></div>
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
  { active: true, path: '报销制度 / 费用标准', owner: '财务标准管理员', docs: 27, published: 21, audit: 6, chunk: '表格文档分片配置', rule: '需要审核', created: '2026-05-19', updated: '2026-06-24' },
  { path: '报销制度 / 审批流程', owner: '财务流程管理员', docs: 22, published: 18, audit: 3, chunk: '制度文档分片', rule: '需要审核', created: '2026-05-20', updated: '2026-06-21' },
  { path: '报销制度 / 供应商管理', owner: '供应商运营', docs: 19, published: 12, audit: 4, chunk: '制度文档分片', rule: '需要审核', created: '2026-05-22', updated: '2026-06-18' },
  { path: '预算制度 / 年度预算', owner: '预算管理员', docs: 31, published: 26, audit: 2, chunk: '财务制度默认', rule: '继承知识库', created: '2026-04-10', updated: '2026-06-19' },
];
</script>
