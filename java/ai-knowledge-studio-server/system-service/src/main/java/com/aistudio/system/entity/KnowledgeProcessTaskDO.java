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
 * 知识文档处理任务持久化对象，对应 knowledge_process_task 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_process_task")
public class KnowledgeProcessTaskDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    private Long knowledgeBaseId;

    private Long directoryId;

    private Long documentId;

    private Long documentVersionId;

    private String taskType;

    private String stageCode;

    private String taskStatus;

    private Integer progress;

    private Integer retryCount;

    private String mqMessageId;

    private String errorCode;

    private String errorMessage;

    private LocalDateTime callbackTime;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private Integer deleted;
}
