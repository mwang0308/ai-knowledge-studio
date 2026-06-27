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
 * 知识文档解析块持久化对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_document_parse_block")
public class KnowledgeDocumentParseBlockDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String parseBlockUid;
    private Long knowledgeBaseId;
    private Long directoryId;
    private Long documentId;
    private String documentUid;
    private Long documentVersionId;
    private String documentVersionUid;
    private Long processTaskId;
    private String processTaskUid;
    private String blockName;
    private Integer pageStart;
    private Integer pageEnd;
    private String sectionIdsJson;
    private String sectionTitlesJson;
    private String textPreview;
    private Integer sortOrder;
    private String metadataJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private Integer deleted;
}
