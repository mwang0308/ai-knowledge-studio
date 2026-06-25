-- 目录描述面向页面展示和筛选，目录路径继续作为后端内部层级定位字段。
alter table knowledge_directory
    add column description varchar(512) null comment '目录描述' after name;
