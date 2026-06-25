<template>
  <section class="main">
    <div class="title">
      <div><h1>文档上传</h1><div class="sub">上传文档到当前目录，右侧规则来自目录默认配置，可在上传前确认。</div></div>
      <div class="btns"><RouterLink class="btn" to="/directory">返回目录</RouterLink></div>
    </div>

    <div class="upload-layout">
      <section class="panel">
        <div class="panel-head"><h2>选择文件</h2><span class="status green">已选择 1 个文件</span></div>
        <div class="panel-body">
          <div class="upload ready">
            <div>
              <strong>点击或拖拽文件到这里</strong>
              <p>支持 Word / PDF / Markdown / TXT / Excel / CSV</p>
              <div class="file-card"><b>财务共享报账手册V2.0.docx</b><span>Word / 4.8MB / file_hash 未重复 / 格式校验通过</span></div>
            </div>
          </div>
          <table class="table table-gap">
            <thead><tr><th>待处理文件</th><th>校验状态</th><th>处理方式</th><th>上传时间</th><th>下一步</th></tr></thead>
            <tbody><tr class="active"><td>财务共享报账手册V2.0.docx</td><td><span class="status green">通过</span></td><td>Parser Router 自动识别</td><td>2026-06-24 10:06</td><td>解析结构并生成分片</td></tr></tbody>
          </table>
          <div class="btns end-actions"><button class="btn">清空</button><RouterLink class="btn primary" to="/document">开始处理</RouterLink></div>
        </div>
      </section>
      <aside class="side-summary">
        <section class="panel">
          <div class="panel-head"><h2>归属与规则</h2></div>
          <div class="panel-body">
            <div class="summary-box">
              <h3>上传位置</h3>
              <table class="kv"><tbody><tr><td>知识库</td><td>财务共享知识库</td></tr><tr><td>目录</td><td>报销制度 / 费用标准</td></tr><tr><td>分类</td><td>费用标准</td></tr><tr><td>标签</td><td>报销、费用、制度</td></tr></tbody></table>
            </div>
            <div class="summary-box block-gap">
              <h3>处理规则</h3>
              <table class="kv"><tbody><tr><td>分片配置</td><td>表格文档分片配置</td></tr><tr><td>审核规则</td><td>需要审核</td></tr><tr><td>处理任务</td><td>DOCUMENT_PARSE_CHUNK</td></tr></tbody></table>
            </div>
          </div>
        </section>
      </aside>
    </div>
  </section>
</template>
