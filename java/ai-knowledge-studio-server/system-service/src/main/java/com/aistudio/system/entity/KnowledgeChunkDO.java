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
 * 知识分片元数据持久化对象，对应 knowledge_chunk 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_chunk")
public class KnowledgeChunkDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String chunkId;
    private Long knowledgeBaseId;
    private Long directoryId;
    private Long documentId;
    private String documentUid;
    private Long documentVersionId;
    private String documentVersionUid;
    private Long processTaskId;
    private String processTaskUid;
    private Integer chunkNo;
    private String chunkHash;
    private String titlePath;
    private String contentPreview;
    private String contentObjectKey;
    private Integer tokenCount;
    private Integer charCount;
    private Integer pageStart;
    private Integer pageEnd;
    private String sheetName;
    private Integer rowStart;
    private Integer rowEnd;
    private String esIndexName;
    private String esDocId;
    private String milvusCollectionName;
    private String milvusVectorId;
    private String publishStatus;
    private Integer enabled;
    private String metadataJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private Integer deleted;
}
