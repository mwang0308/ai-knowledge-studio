<template>
  <div class="app">
    <aside class="side">
      <div class="brand"><span class="brand-mark">知</span>知识库平台</div>
      <nav class="nav">
        <RouterLink v-for="item in navItems" :key="item.path" :to="item.path">
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>
    <main>
      <header class="top">
        <div class="crumb">{{ route.meta.crumb }}</div>
        <div>admin</div>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();

const navItems = [
  { path: '/', label: '首页' },
  { path: '/knowledge', label: '知识库' },
  { path: '/directory', label: '目录' },
  { path: '/upload', label: '文档上传' },
  { path: '/document', label: '文档处理' },
  { path: '/retrieval', label: '检索测试' },
  { path: '/publish', label: '审核发布' },
];
</script>
