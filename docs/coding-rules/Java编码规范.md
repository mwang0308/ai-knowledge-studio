# Java 编码规范

## 包和类

- 根包名：`com.aistudio`。
- 包名全小写。
- 类名使用 UpperCamelCase。
- 方法和变量使用 lowerCamelCase。
- 常量使用 UPPER_UNDERSCORE_CASE。

## 注入

ServiceImpl 使用 `@Resource` 字段注入，不使用构造方法注入。

## 分层

- Controller 不写业务逻辑。
- Service 负责业务编排。
- Mapper 只做数据库访问。
- Convert 负责对象转换。

## 日志和注释

- ServiceImpl 必须加 `@Slf4j`。
- 关键业务入口必须打日志。
- 状态流转、异步任务、回调、审核发布、异常分支必须打日志。
- 接口、实现类、核心方法必须写清楚职责。
- 复杂分支必须有必要注释。

## 数据访问

- 使用 MyBatis-Plus。
- Mapper 接口使用 `IxxxMapper`。
- XML 文件使用 `XxxMapper.xml`。
- Mapper 接收 Query，不接收 Request。
- Mapper 返回 DO，不返回 Response。
