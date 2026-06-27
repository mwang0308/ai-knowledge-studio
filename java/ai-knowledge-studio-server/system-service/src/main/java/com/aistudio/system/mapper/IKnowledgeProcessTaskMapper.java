package com.aistudio.system.mapper;

import com.aistudio.system.entity.KnowledgeProcessTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识文档处理任务 Mapper，只负责数据库访问，不接收 Request，不返回 Response。
 */
public interface IKnowledgeProcessTaskMapper extends BaseMapper<KnowledgeProcessTaskDO> {

    /**
     * 更新任务 MQ 消息 ID。
     *
     * @param id 任务 ID
     * @param mqMessageId MQ 消息 ID
     * @return 影响行数
     */
    int updateMqMessageId(@Param("id") Long id, @Param("mqMessageId") String mqMessageId);

    KnowledgeProcessTaskDO selectByTaskUid(@Param("taskUid") String taskUid);

    /**
     * 更新任务处理中进度。
     */
    int updateRunning(@Param("id") Long id, @Param("progress") Integer progress);

    /**
     * 更新任务成功结果。
     */
    int updateSuccess(@Param("id") Long id, @Param("progress") Integer progress);

    /**
     * 更新任务失败结果。
     */
    int updateFailed(@Param("id") Long id,
                     @Param("progress") Integer progress,
                     @Param("errorCode") String errorCode,
                     @Param("errorMessage") String errorMessage);

    int countRunningByDocumentId(@Param("documentId") Long documentId);

    int softDeleteByDocumentId(@Param("documentId") Long documentId);
}
