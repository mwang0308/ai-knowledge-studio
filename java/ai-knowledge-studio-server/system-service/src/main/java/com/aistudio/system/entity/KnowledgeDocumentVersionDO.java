package com.aistudio.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识文档版本持久化对象，对应 knowledge_document_version 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_document_version")
public class KnowledgeDocumentVersionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String versionUid;

    private Long documentId;

    private Integer versionNo;

    private Long fileResourceId;

    private String fileHash;

    private String chunkConfigSnapshot;

    private String parseStatus;

    private String indexStatus;

    private Integer chunkCount;

    private Integer tokenCount;

    private String parserName;

    private String embeddingModel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private Integer deleted;
}
