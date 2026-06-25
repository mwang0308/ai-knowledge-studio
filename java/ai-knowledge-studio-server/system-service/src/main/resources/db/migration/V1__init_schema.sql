-- 第一阶段数据库初始化入口。
-- knowledge_base 是知识治理顶层空间表，后续目录、文档、分片配置都挂在该表之下。
create table if not exists knowledge_base
(
    id               bigint       not null auto_increment comment '主键 ID',
    name             varchar(128) not null comment '知识库名称',
    description      varchar(512) null comment '知识库描述',
    embedding_model  varchar(64)  not null default 'bge-m3' comment 'Embedding 模型编码',
    retrieval_mode   varchar(32)  not null default 'VECTOR' comment '检索模式',
    status           tinyint      not null default 1 comment '状态：1 启用，0 停用',
    published_status tinyint      not null default 0 comment '发布状态：0 未发布，1 已发布',
    document_count   int          not null default 0 comment '文档数量',
    chunk_count      int          not null default 0 comment '分片数量',
    create_time      datetime     not null default current_timestamp comment '创建时间',
    update_time      datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by        bigint       null comment '创建人',
    update_by        bigint       null comment '更新人',
    deleted          tinyint      not null default 0 comment '逻辑删除：0 未删除，1 已删除',
    primary key (id),
    unique key uk_knowledge_base_name_deleted (name, deleted),
    key idx_knowledge_base_status (status),
    key idx_knowledge_base_update_time (update_time)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识库主表';
