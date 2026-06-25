package com.aistudio.system.mapper;

import com.aistudio.system.entity.KnowledgeBaseDO;
import com.aistudio.system.query.KnowledgeBasePageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库 Mapper，只负责数据库访问，不接收 Request，不返回 Response。
 */
public interface IKnowledgeBaseMapper extends BaseMapper<KnowledgeBaseDO> {

    /**
     * 根据 ID 查询未删除知识库。
     *
     * @param id 知识库 ID
     * @return 知识库持久化对象
     */
    KnowledgeBaseDO selectKnowledgeBaseById(@Param("id") Long id);

    /**
     * 分页查询知识库。
     *
     * @param page MyBatis-Plus 分页对象
     * @param query 数据库查询对象
     * @return 知识库分页持久化对象
     */
    IPage<KnowledgeBaseDO> selectKnowledgeBasePage(Page<KnowledgeBaseDO> page,
                                                   @Param("query") KnowledgeBasePageQuery query);

    /**
     * 根据名称统计未删除知识库数量，用于创建和更新时做唯一性校验。
     *
     * @param name 知识库名称
     * @param excludeId 需要排除的知识库 ID，创建时为空
     * @return 匹配数量
     */
    Integer countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 逻辑删除知识库。
     *
     * @param id 知识库 ID
     * @return 影响行数
     */
    int softDeleteById(@Param("id") Long id);
}
