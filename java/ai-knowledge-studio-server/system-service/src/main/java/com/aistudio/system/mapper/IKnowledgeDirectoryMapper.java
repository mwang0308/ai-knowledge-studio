package com.aistudio.system.mapper;

import com.aistudio.system.entity.KnowledgeDirectoryDO;
import com.aistudio.system.query.KnowledgeDirectoryTreeQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库目录 Mapper，只负责数据库访问，不接收 Request，不返回 Response。
 */
public interface IKnowledgeDirectoryMapper extends BaseMapper<KnowledgeDirectoryDO> {

    /**
     * 根据 ID 查询未删除目录。
     *
     * @param id 目录 ID
     * @return 目录持久化对象
     */
    KnowledgeDirectoryDO selectKnowledgeDirectoryById(@Param("id") Long id);

    /**
     * 查询知识库目录树所需的平铺目录列表。
     *
     * @param query 数据库查询对象
     * @return 目录持久化对象列表
     */
    List<KnowledgeDirectoryDO> selectKnowledgeDirectoryTree(@Param("query") KnowledgeDirectoryTreeQuery query);

    /**
     * 根据知识库和路径统计目录数量，用于唯一性校验。
     *
     * @param knowledgeBaseId 知识库 ID
     * @param path 目录路径
     * @param excludeId 需要排除的目录 ID，创建时为空
     * @return 匹配数量
     */
    Integer countByPath(@Param("knowledgeBaseId") Long knowledgeBaseId,
                        @Param("path") String path,
                        @Param("excludeId") Long excludeId);

    /**
     * 统计未删除子目录数量，删除或移动目录前用于保护目录树。
     *
     * @param parentId 父目录 ID
     * @return 子目录数量
     */
    Integer countChildren(@Param("parentId") Long parentId);

    /**
     * 统计知识库下未删除目录数量。
     *
     * @param knowledgeBaseId 知识库 ID
     * @return 目录数量
     */
    Integer countByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);

    /**
     * 更新目录基础信息。使用 XML 显式更新 parent_id，支持把目录调整为一级目录。
     *
     * @param directoryDO 目录持久化对象
     * @return 影响行数
     */
    int updateKnowledgeDirectoryById(@Param("directory") KnowledgeDirectoryDO directoryDO);

    /**
     * 逻辑删除目录。
     *
     * @param id 目录 ID
     * @return 影响行数
     */
    int softDeleteById(@Param("id") Long id);
}
