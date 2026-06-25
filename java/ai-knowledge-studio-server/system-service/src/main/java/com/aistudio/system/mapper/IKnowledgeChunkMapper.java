package com.aistudio.system.mapper;

import com.aistudio.system.entity.KnowledgeChunkDO;
import com.aistudio.system.query.KnowledgeChunkPageQuery;
import com.aistudio.system.query.RetrievalCandidateQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识分片 Mapper，只负责数据库访问。
 */
public interface IKnowledgeChunkMapper extends BaseMapper<KnowledgeChunkDO> {

    IPage<KnowledgeChunkDO> selectChunkPage(Page<KnowledgeChunkDO> page, @Param("query") KnowledgeChunkPageQuery query);

    List<KnowledgeChunkDO> selectRetrievalCandidates(@Param("query") RetrievalCandidateQuery query);

    int deleteByTaskId(@Param("taskId") Long taskId);

    int updatePublishStatus(@Param("documentId") Long documentId,
                            @Param("versionId") Long versionId,
                            @Param("publishStatus") String publishStatus,
                            @Param("enabled") Integer enabled);
}
