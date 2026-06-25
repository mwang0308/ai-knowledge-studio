# Python 编码规范

## 适用范围

适用于文档解析、视觉解析、模型网关、Agent 编排等 Python 服务。

## 命名

- 文件名使用小写下划线。
- 类名使用 UpperCamelCase。
- 函数和变量使用 lower_snake_case。

## 接口

- FastAPI 接口必须定义 Pydantic 模型。
- 入参和出参不能直接使用 dict 透传。

## 日志

- 任务开始、任务结束、失败重试、外部服务调用必须记录日志。
- 日志必须包含 `task_id`、`document_id`、`stage`。
- 不记录文件正文和敏感数据。

## 异常

- 可恢复异常要返回明确错误码。
- 不吞异常。
- 回调 Java 时必须带上失败原因。
