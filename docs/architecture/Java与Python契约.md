# Java 与 Python 契约

## 任务投递

Java 通过 RabbitMQ 投递文档处理任务。

消息必须包含：

- `taskId`
- `documentId`
- `documentVersionId`
- `fileUri`
- `fileHash`
- `stage`
- `chunkConfigSnapshot`
- `callbackUrl`

## 回调要求

Python 处理完成后回调 Java。

回调必须包含：

- `taskId`
- `documentId`
- `stage`
- `status`
- `message`
- `artifactUri`
- `chunkCount`
- `errorCode`

## 状态约定

- `PENDING`
- `PROCESSING`
- `SUCCESS`
- `FAILED`
- `CANCELED`

## 约束

- 回调必须幂等。
- Java 以 `taskId` 做任务定位。
- Python 不直接修改 Java MySQL。
- Java 不直接依赖 Python 内部实现。
