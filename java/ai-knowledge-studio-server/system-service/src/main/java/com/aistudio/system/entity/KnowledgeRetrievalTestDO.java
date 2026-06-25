package com.aistudio.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 召回测试记录持久化对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_retrieval_test")
public class KnowledgeRetrievalTestDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgeBaseId;
    private Long directoryId;
    private Long documentId;
    private String queryText;
    private Integer topK;
    private String testScope;
    private String resultJson;
    private BigDecimal topScore;
    private Integer passed;
    private Long latencyMs;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private Integer deleted;
}
