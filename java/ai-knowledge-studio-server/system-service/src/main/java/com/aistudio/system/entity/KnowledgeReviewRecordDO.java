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
 * 知识文档审核记录持久化对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_review_record")
public class KnowledgeReviewRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgeBaseId;
    private Long documentId;
    private Long documentVersionId;
    private String reviewStatus;
    private String reviewComment;
    private Long reviewUserId;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private Integer deleted;
}
