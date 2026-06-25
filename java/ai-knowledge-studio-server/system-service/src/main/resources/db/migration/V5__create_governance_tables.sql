-- 分片元数据表：正文完整内容不进入 MySQL，仅保存预览、来源和索引定位字段。
create table if not exists knowledge_chunk
(
    id                     bigint        not null auto_increment comment '主键 ID',
    chunk_id               varchar(128)  not null comment '稳定业务分片 ID',
    knowledge_base_id      bigint        not null comment '知识库 ID',
    directory_id           bigint        not null comment '目录 ID',
    document_id            bigint        not null comment '文档 ID',
    document_version_id    bigint        not null comment '文档版本 ID',
    process_task_id        bigint        not null comment '处理任务 ID',
    chunk_no               int           not null comment '分片序号',
    chunk_hash             varchar(128)  null comment '分片 hash',
    title_path             varchar(512)  null comment '标题路径',
    content_preview        varchar(1000) null comment '分片内容预览',
    content_object_key     varchar(1024) null comment '分片产物对象 key',
    token_count            int           not null default 0 comment 'Token 数量',
    char_count             int           not null default 0 comment '字符数量',
    page_start             int           null comment '起始页',
    page_end               int           null comment '结束页',
    sheet_name             varchar(128)  null comment 'Sheet 名',
    row_start              int           null comment '起始行',
    row_end                int           null comment '结束行',
    es_index_name          varchar(128)  null comment 'ES 索引名',
    es_doc_id              varchar(128)  null comment 'ES 文档 ID',
    milvus_collection_name varchar(128)  null comment 'Milvus Collection',
    milvus_vector_id       varchar(128)  null comment 'Milvus 向量 ID',
    publish_status         varchar(32)   not null comment '发布状态',
    enabled                tinyint       not null default 0 comment '是否正式检索启用',
    metadata_json          text          null comment '扩展元数据 JSON',
    create_time            datetime      not null default current_timestamp comment '创建时间',
    update_time            datetime      not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by              bigint        null comment '创建人',
    update_by              bigint        null comment '更新人',
    deleted                tinyint       not null default 0 comment '逻辑删除',
    primary key (id),
    unique key uk_knowledge_chunk_id (chunk_id, deleted),
    key idx_chunk_document (document_id, document_version_id, deleted),
    key idx_chunk_scope (knowledge_base_id, directory_id, publish_status, enabled)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识分片元数据表';

-- 召回测试记录表：保存测试请求和结果摘要，便于发布前追溯。
create table if not exists knowledge_retrieval_test
(
    id                bigint       not null auto_increment comment '主键 ID',
    knowledge_base_id bigint       not null comment '知识库 ID',
    directory_id      bigint       null comment '目录 ID',
    document_id       bigint       null comment '文档 ID',
    query_text        varchar(512) not null comment '测试问题',
    top_k             int          not null comment 'TopK',
    test_scope        varchar(32)  not null comment '测试范围',
    result_json       text         null comment '召回结果 JSON',
    top_score         decimal(8,4) null comment '最高得分',
    passed            tinyint      not null default 0 comment '是否标记通过',
    latency_ms        bigint       not null default 0 comment '耗时毫秒',
    error_message     varchar(512) null comment '错误信息',
    create_time       datetime     not null default current_timestamp comment '创建时间',
    update_time       datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by         bigint       null comment '创建人',
    update_by         bigint       null comment '更新人',
    deleted           tinyint      not null default 0 comment '逻辑删除',
    primary key (id),
    key idx_retrieval_scope (knowledge_base_id, directory_id, document_id, deleted),
    key idx_retrieval_time (create_time)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '召回测试记录表';

create table if not exists knowledge_review_record
(
    id                  bigint       not null auto_increment comment '主键 ID',
    knowledge_base_id   bigint       not null comment '知识库 ID',
    document_id         bigint       not null comment '文档 ID',
    document_version_id bigint       not null comment '文档版本 ID',
    review_status       varchar(32)  not null comment '审核状态',
    review_comment      varchar(512) null comment '审核意见',
    review_user_id      bigint       null comment '审核人 ID',
    review_time         datetime     not null default current_timestamp comment '审核时间',
    create_time         datetime     not null default current_timestamp comment '创建时间',
    update_time         datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by           bigint       null comment '创建人',
    update_by           bigint       null comment '更新人',
    deleted             tinyint      not null default 0 comment '逻辑删除',
    primary key (id),
    key idx_review_document (document_id, document_version_id, deleted)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识文档审核记录表';

create table if not exists knowledge_publish_record
(
    id                  bigint       not null auto_increment comment '主键 ID',
    knowledge_base_id   bigint       not null comment '知识库 ID',
    document_id         bigint       not null comment '文档 ID',
    document_version_id bigint       not null comment '文档版本 ID',
    publish_action      varchar(32)  not null comment '发布动作',
    publish_status      varchar(32)  not null comment '发布状态',
    operator_id         bigint       null comment '操作人 ID',
    operate_time        datetime     not null default current_timestamp comment '操作时间',
    error_message       varchar(512) null comment '错误信息',
    create_time         datetime     not null default current_timestamp comment '创建时间',
    update_time         datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by           bigint       null comment '创建人',
    update_by           bigint       null comment '更新人',
    deleted             tinyint      not null default 0 comment '逻辑删除',
    primary key (id),
    key idx_publish_document (document_id, document_version_id, deleted)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识文档发布记录表';
