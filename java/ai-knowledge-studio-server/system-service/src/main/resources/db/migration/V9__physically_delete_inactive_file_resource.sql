-- file_resource 是可复用原文元数据，删除文档时在无活动引用后物理删除。
-- 清理历史软删除记录，释放 (file_hash, deleted) 唯一键占位，避免同 hash 再上传/再删除冲突。
delete from file_resource
where deleted <> 0;

alter table file_resource
    modify column deleted tinyint not null default 0 comment '逻辑删除：0 未删除，1 已删除';
