alter table knowledge_document
    add column document_uid varchar(32) null comment '文档业务 ID，32 位无横杠 UUID' after id;

update knowledge_document
set document_uid = replace(uuid(), '-', '')
where document_uid is null or document_uid = '';

alter table knowledge_document
    modify column document_uid varchar(32) not null comment '文档业务 ID，32 位无横杠 UUID',
    add unique key uk_document_uid (document_uid);

alter table knowledge_document_version
    add column version_uid varchar(32) null comment '文档版本业务 ID，32 位无横杠 UUID' after id;

update knowledge_document_version
set version_uid = replace(uuid(), '-', '')
where version_uid is null or version_uid = '';

alter table knowledge_document_version
    modify column version_uid varchar(32) not null comment '文档版本业务 ID，32 位无横杠 UUID',
    add unique key uk_document_version_uid (version_uid);

alter table knowledge_process_task
    add column task_uid varchar(32) null comment '处理任务业务 ID，32 位无横杠 UUID' after id;

update knowledge_process_task
set task_uid = replace(uuid(), '-', '')
where task_uid is null or task_uid = '';

alter table knowledge_process_task
    modify column task_uid varchar(32) not null comment '处理任务业务 ID，32 位无横杠 UUID',
    add unique key uk_process_task_uid (task_uid);

alter table knowledge_chunk
    add column document_uid varchar(32) null comment '文档业务 ID' after document_id,
    add column document_version_uid varchar(32) null comment '文档版本业务 ID' after document_version_id,
    add column process_task_uid varchar(32) null comment '处理任务业务 ID' after process_task_id;

update knowledge_chunk c
    join knowledge_document d on c.document_id = d.id
set c.document_uid = d.document_uid
where c.document_uid is null or c.document_uid = '';

update knowledge_chunk c
    join knowledge_document_version v on c.document_version_id = v.id
set c.document_version_uid = v.version_uid
where c.document_version_uid is null or c.document_version_uid = '';

update knowledge_chunk c
    join knowledge_process_task t on c.process_task_id = t.id
set c.process_task_uid = t.task_uid
where c.process_task_uid is null or c.process_task_uid = '';

alter table knowledge_chunk
    modify column document_uid varchar(32) not null comment '文档业务 ID',
    modify column document_version_uid varchar(32) not null comment '文档版本业务 ID',
    modify column process_task_uid varchar(32) not null comment '处理任务业务 ID',
    add key idx_chunk_document_uid (document_uid, document_version_uid, deleted),
    add key idx_chunk_task_uid (process_task_uid, deleted);
