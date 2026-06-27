create table if not exists knowledge_document_parse_block
(
    id                    bigint        not null auto_increment comment '主键 ID',
    parse_block_uid       varchar(32)   not null comment '解析块业务 ID',
    knowledge_base_id     bigint        not null comment '知识库 ID',
    directory_id          bigint        not null comment '目录 ID',
    document_id           bigint        not null comment '文档 ID',
    document_uid          varchar(32)   not null comment '文档业务 ID',
    document_version_id   bigint        not null comment '文档版本 ID',
    document_version_uid  varchar(32)   not null comment '文档版本业务 ID',
    process_task_id       bigint        not null comment '处理任务 ID',
    process_task_uid      varchar(32)   not null comment '处理任务业务 ID',
    block_name            varchar(512)  not null comment '解析块名称',
    page_start            int           null comment '起始页',
    page_end              int           null comment '结束页',
    section_ids_json      text          null comment '关联目录节点 ID JSON',
    section_titles_json   text          null comment '关联目录标题 JSON',
    text_preview          varchar(1000) null comment '文本预览',
    sort_order            int           not null comment '排序号',
    metadata_json         text          null comment '扩展元数据 JSON',
    create_time           datetime      not null default current_timestamp comment '创建时间',
    update_time           datetime      not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by             bigint        null comment '创建人',
    update_by             bigint        null comment '更新人',
    deleted               tinyint       not null default 0 comment '逻辑删除',
    primary key (id),
    key idx_doc_parse_block_uid (parse_block_uid, deleted),
    key idx_doc_parse_block_version (document_id, document_version_id, deleted)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识文档解析块表';
