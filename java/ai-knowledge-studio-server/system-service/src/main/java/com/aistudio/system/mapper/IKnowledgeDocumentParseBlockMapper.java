package com.aistudio.system.mapper;

import com.aistudio.system.entity.KnowledgeDocumentParseBlockDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识文档解析块 Mapper，只负责数据库访问。
 */
public interface IKnowledgeDocumentParseBlockMapper extends BaseMapper<KnowledgeDocumentParseBlockDO> {

    List<KnowledgeDocumentParseBlockDO> selectByDocumentVersion(@Param("documentId") Long documentId,
                                                                    @Param("versionId") Long versionId);

    int deleteByDocumentVersion(@Param("documentId") Long documentId, @Param("versionId") Long versionId);

    int deleteByDocumentId(@Param("documentId") Long documentId);
}
