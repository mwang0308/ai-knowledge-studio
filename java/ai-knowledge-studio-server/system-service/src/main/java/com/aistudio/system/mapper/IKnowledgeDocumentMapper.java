package com.aistudio.system.mapper;

import com.aistudio.system.entity.KnowledgeDocumentDO;
import com.aistudio.system.query.DashboardTodoPageQuery;
import com.aistudio.system.query.KnowledgeDocumentPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识文档 Mapper，只负责数据库访问，不接收 Request，不返回 Response。
 */
public interface IKnowledgeDocumentMapper extends BaseMapper<KnowledgeDocumentDO> {

    /**
     * 统计同一知识库、同一目录下相同 hash 的未删除文档数量。
     *
     * @param knowledgeBaseId 知识库 ID
     * @param directoryId 目录 ID
     * @param fileHash 文件 SHA-256
     * @return 文档数量
     */
    Integer countDuplicateFile(@Param("knowledgeBaseId") Long knowledgeBaseId,
                               @Param("directoryId") Long directoryId,
                               @Param("fileHash") String fileHash);

    /**
     * 更新文档当前版本 ID。
     *
     * @param documentId 文档 ID
     * @param versionId 版本 ID
     * @return 影响行数
     */
    int updateCurrentVersionId(@Param("documentId") Long documentId, @Param("versionId") Long versionId);

    /**
     * 分页查询知识文档。
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 文档分页
     */
    IPage<KnowledgeDocumentDO> selectDocumentPage(Page<KnowledgeDocumentDO> page,
                                                  @Param("query") KnowledgeDocumentPageQuery query);

    /**
     * 更新文档解析成功状态。
     */
    int updateParseSuccess(@Param("documentId") Long documentId,
                           @Param("versionId") Long versionId,
                           @Param("chunkCount") Integer chunkCount);

    /**
     * 更新文档解析处理中状态。
     */
    int updateParseRunning(@Param("documentId") Long documentId, @Param("versionId") Long versionId);

    /**
     * 更新文档解析失败状态。
     */
    int updateParseFailed(@Param("documentId") Long documentId,
                          @Param("versionId") Long versionId,
                          @Param("errorMessage") String errorMessage);

    int updateIndexSuccess(@Param("documentId") Long documentId, @Param("versionId") Long versionId);

    int updateReviewStatus(@Param("documentId") Long documentId,
                           @Param("reviewStatus") String reviewStatus);

    int updatePublishStatus(@Param("documentId") Long documentId,
                            @Param("publishStatus") String publishStatus);

    Long countByDashboardStatus(@Param("parseStatus") String parseStatus,
                                @Param("reviewStatus") String reviewStatus,
                                @Param("publishStatus") String publishStatus);

    Long sumDashboardChunkCount();

    List<KnowledgeDocumentDO> selectDashboardTodos(@Param("limitSize") Integer limitSize);

    IPage<KnowledgeDocumentDO> selectDashboardTodoPage(Page<KnowledgeDocumentDO> page,
                                                       @Param("query") DashboardTodoPageQuery query);
}
