# 开发代理工作说明

本文用于约束后续 AI 代理、自动化脚本和开发人员在本项目中的工作方式。

## 项目定位

本项目是企业级 RAG 知识库入库前治理平台，第一阶段目标是跑通知识库、目录、文档上传、解析任务、分片预览、召回测试、审核发布的完整闭环。

## 开发原则

- 修改代码前必须先阅读相关需求、方案和工程约定文档。
- 不要随意新增服务、模块或抽象层，必须符合既有方案设计。
- Java 包路径不增加项目代号层级，统一使用 `com.aistudio`。
- 知识治理能力归入 `system-service`，不要单独创建 `knowledge-service`。
- 数据库访问使用 MyBatis-Plus Mapper，接口名使用 `IxxxMapper`，XML 文件名不加 `I`。
- ServiceImpl 使用 `@Resource` 注入依赖，不使用构造方法注入。
- Request、Response 只属于接口层；Mapper 不接收 Request，不返回 Response。
- Request 进入数据库查询前必须转换为 Query；Mapper 返回 DO；DO 转换为 Response 后返回前端。
- 复杂逻辑、状态流转、异常分支必须写必要注释。
- Service 入口、异步任务、回调、状态变更、审核发布、异常分支必须记录日志。

## 必读文档

- `docs/工程目录与文件规划.md`
- `docs/企业级RAG知识库入库前治理平台方案设计文档.md`
- `docs/企业级RAG知识库入库前治理平台需求分析文档.md`
- `docs/原型图与需求合并说明.md`
- `ARCHITECTURE.md`
- `docs/architecture/模块边界.md`
- `docs/architecture/Java与Python契约.md`
- `docs/coding-rules/Java编码规范.md`
- `docs/test/验收清单.md`

## 工作流程

1. 明确本次任务对应的业务模块。
2. 阅读 `docs/product/` 中对应产品说明。
3. 阅读 `docs/architecture/` 中对应架构和契约说明。
4. 阅读 `docs/coding-rules/` 中对应语言规范。
5. 修改代码。
6. 补充必要注释和日志。
7. 执行对应测试脚本。
8. 在 `docs/plans/active/` 或 `docs/plans/completed/` 维护计划和完成记录。

## 禁止事项

- 禁止把 DO、Entity 直接返回给前端。
- 禁止 Controller 写业务逻辑。
- 禁止用 `Map` 代替明确对象。
- 禁止在 Mapper 中写业务编排。
- 禁止把测试、回放、验收逻辑混进生产业务逻辑。
- 禁止在日志中输出密码、Token、文件原文、大段文档内容等敏感信息。
