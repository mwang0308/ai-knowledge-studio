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
 * 知识文档目录结构持久化对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_document_structure")
public class KnowledgeDocumentStructureDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String sectionUid;
    private String parentSectionUid;
    private Long knowledgeBaseId;
    private Long directoryId;
    private Long documentId;
    private String documentUid;
    private Long documentVersionId;
    private String documentVersionUid;
    private Long processTaskId;
    private String processTaskUid;
    private String title;
    private String titlePath;
    private Integer level;
    private Integer pageStart;
    private Integer pageEnd;
    private Integer blockCount;
    private Integer sortOrder;
    private String metadataJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private Integer deleted;
}
