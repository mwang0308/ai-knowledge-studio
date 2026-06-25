package com.aistudio.system.mapper;

import com.aistudio.system.entity.KnowledgeDocumentVersionDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识文档版本 Mapper，只负责数据库访问，不接收 Request，不返回 Response。
 */
public interface IKnowledgeDocumentVersionMapper extends BaseMapper<KnowledgeDocumentVersionDO> {

    /**
     * 更新文档版本解析成功结果。
     */
    int updateParseSuccess(@Param("versionId") Long versionId,
                           @Param("chunkCount") Integer chunkCount,
                           @Param("tokenCount") Integer tokenCount,
                           @Param("parserName") String parserName);

    /**
     * 更新文档版本解析处理中状态。
     */
    int updateParseRunning(@Param("versionId") Long versionId);

    /**
     * 更新文档版本解析失败状态。
     */
    int updateParseFailed(@Param("versionId") Long versionId);

    int updateIndexSuccess(@Param("versionId") Long versionId, @Param("embeddingModel") String embeddingModel);
}
