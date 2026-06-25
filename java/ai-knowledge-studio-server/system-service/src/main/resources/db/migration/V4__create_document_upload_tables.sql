-- 文件资源表：按 file_hash 复用原始文件，原文存 MinIO，MySQL 只存元数据。
create table if not exists file_resource
(
    id           bigint        not null auto_increment comment '主键 ID',
    file_name    varchar(255)  not null comment '原始文件名',
    file_ext     varchar(32)   not null comment '文件扩展名',
    file_size    bigint        not null comment '文件大小，单位字节',
    file_hash    varchar(128)  not null comment '文件 SHA-256',
    bucket_name  varchar(128)  not null comment 'MinIO bucket',
    object_key   varchar(1024) not null comment 'MinIO object key',
    content_type varchar(128)  null comment '文件 content type',
    create_time  datetime      not null default current_timestamp comment '创建时间',
    update_time  datetime      not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by    bigint        null comment '创建人',
    update_by    bigint        null comment '更新人',
    deleted      tinyint       not null default 0 comment '逻辑删除：0 未删除，1 已删除',
    primary key (id),
    unique key uk_file_resource_hash_deleted (file_hash, deleted),
    key idx_file_resource_create_time (create_time)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '文件资源表';

-- 文档主表：挂知识库和目录，保存当前版本、处理状态和发布状态。
create table if not exists knowledge_document
(
    id                 bigint       not null auto_increment comment '主键 ID',
    knowledge_base_id  bigint       not null comment '知识库 ID',
    directory_id       bigint       not null comment '目录 ID',
    name               varchar(255) not null comment '文档名称',
    file_resource_id   bigint       not null comment '当前文件资源 ID',
    current_version_id bigint       null comment '当前文档版本 ID',
    file_name          varchar(255) not null comment '原始文件名',
    file_ext           varchar(32)  not null comment '文件扩展名',
    file_size          bigint       not null comment '文件大小，单位字节',
    file_hash          varchar(128) not null comment '文件 SHA-256',
    parse_status       varchar(32)  not null comment '解析状态',
    index_status       varchar(32)  not null comment '索引状态',
    review_status      varchar(32)  not null comment '审核状态',
    publish_status     varchar(32)  not null comment '发布状态',
    chunk_count        int          not null default 0 comment '分片数量',
    error_message      varchar(512) null comment '错误信息',
    create_time        datetime     not null default current_timestamp comment '创建时间',
    update_time        datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by          bigint       null comment '创建人',
    update_by          bigint       null comment '更新人',
    deleted            tinyint      not null default 0 comment '逻辑删除：0 未删除，1 已删除',
    primary key (id),
    key idx_document_base_directory (knowledge_base_id, directory_id),
    key idx_document_hash (knowledge_base_id, directory_id, file_hash, deleted),
    key idx_document_status (parse_status, index_status, publish_status)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识文档主表';

-- 文档版本表：每次重新上传不同文件或重新处理时生成版本，保留处理快照。
create table if not exists knowledge_document_version
(
    id                    bigint       not null auto_increment comment '主键 ID',
    document_id           bigint       not null comment '文档 ID',
    version_no            int          not null comment '版本号',
    file_resource_id      bigint       not null comment '文件资源 ID',
    file_hash             varchar(128) not null comment '文件 SHA-256',
    chunk_config_snapshot text         null comment '分片配置快照 JSON',
    parse_status          varchar(32)  not null comment '解析状态',
    index_status          varchar(32)  not null comment '索引状态',
    chunk_count           int          not null default 0 comment '分片数量',
    token_count           int          not null default 0 comment 'Token 数量',
    parser_name           varchar(64)  null comment '解析器名称',
    embedding_model       varchar(64)  null comment '向量模型编码',
    create_time           datetime     not null default current_timestamp comment '创建时间',
    update_time           datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by             bigint       null comment '创建人',
    update_by             bigint       null comment '更新人',
    deleted               tinyint      not null default 0 comment '逻辑删除：0 未删除，1 已删除',
    primary key (id),
    unique key uk_document_version (document_id, version_no, deleted),
    key idx_document_version_status (parse_status, index_status)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识文档版本表';

-- 文档处理任务表：记录 Java 投递给 Python 的大阶段任务。
create table if not exists knowledge_process_task
(
    id                  bigint       not null auto_increment comment '主键 ID',
    task_no             varchar(64)  not null comment '任务编号',
    knowledge_base_id   bigint       not null comment '知识库 ID',
    directory_id        bigint       not null comment '目录 ID',
    document_id         bigint       not null comment '文档 ID',
    document_version_id bigint       not null comment '文档版本 ID',
    task_type           varchar(64)  not null comment '任务类型',
    stage_code          varchar(64)  not null comment '阶段编码',
    task_status         varchar(32)  not null comment '任务状态',
    progress            int          not null default 0 comment '任务进度',
    retry_count         int          not null default 0 comment '重试次数',
    mq_message_id       varchar(128) null comment 'MQ 消息 ID',
    error_code          varchar(64)  null comment '错误码',
    error_message       varchar(512) null comment '错误信息',
    callback_time       datetime     null comment '回调时间',
    start_time          datetime     null comment '开始时间',
    finish_time         datetime     null comment '完成时间',
    create_time         datetime     not null default current_timestamp comment '创建时间',
    update_time         datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by           bigint       null comment '创建人',
    update_by           bigint       null comment '更新人',
    deleted             tinyint      not null default 0 comment '逻辑删除：0 未删除，1 已删除',
    primary key (id),
    unique key uk_process_task_no (task_no),
    key idx_process_task_document (document_id, document_version_id),
    key idx_process_task_status (stage_code, task_status)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识文档处理任务表';
