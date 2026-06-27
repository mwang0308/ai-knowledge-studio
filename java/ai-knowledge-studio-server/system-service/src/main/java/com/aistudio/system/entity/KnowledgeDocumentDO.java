package com.aistudio.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识文档持久化对象，对应 knowledge_document 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_document")
public class KnowledgeDocumentDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String documentUid;

    private Long knowledgeBaseId;

    private Long directoryId;

    private String name;

    private Long fileResourceId;

    private Long currentVersionId;

    @TableField(exist = false)
    private String currentVersionUid;

    private String fileName;

    private String fileExt;

    private Long fileSize;

    private String fileHash;

    private String parseStatus;

    private String indexStatus;

    private String reviewStatus;

    private String publishStatus;

    private Integer chunkCount;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private Integer deleted;
}
