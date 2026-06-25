-- knowledge_directory 是知识库下的目录树，用于承载文档归属关系。
create table if not exists knowledge_directory
(
    id                bigint       not null auto_increment comment '主键 ID',
    knowledge_base_id bigint       not null comment '知识库 ID',
    parent_id         bigint       null comment '父目录 ID，一级目录为空',
    name              varchar(128) not null comment '目录名称',
    path              varchar(512) not null comment '目录完整路径',
    level             int          not null default 1 comment '目录层级，从 1 开始',
    sort_order        int          not null default 0 comment '排序值',
    status            tinyint      not null default 1 comment '状态：1 启用，0 停用',
    create_time       datetime     not null default current_timestamp comment '创建时间',
    update_time       datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    create_by         bigint       null comment '创建人',
    update_by         bigint       null comment '更新人',
    deleted           tinyint      not null default 0 comment '逻辑删除：0 未删除，1 已删除',
    primary key (id),
    unique key uk_directory_base_path_deleted (knowledge_base_id, path, deleted),
    key idx_directory_base_parent (knowledge_base_id, parent_id),
    key idx_directory_status (status)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '知识库目录表';
