package com.aistudio.system.convert;

import com.aistudio.system.entity.KnowledgeBaseDO;
import com.aistudio.system.enums.KnowledgeBasePublishedStatusEnum;
import com.aistudio.system.enums.KnowledgeBaseStatusEnum;
import com.aistudio.system.model.request.KnowledgeBaseCreateRequest;
import com.aistudio.system.model.request.KnowledgeBasePageRequest;
import com.aistudio.system.model.request.KnowledgeBaseUpdateRequest;
import com.aistudio.system.model.response.KnowledgeBaseResponse;
import com.aistudio.system.query.KnowledgeBasePageQuery;
import org.springframework.stereotype.Component;

/**
 * 知识库对象转换器，集中处理 Request、Query、DO、Response 之间的转换。
 */
@Component
public class KnowledgeBaseConvert {

    public KnowledgeBaseDO toCreateDO(KnowledgeBaseCreateRequest request) {
        return KnowledgeBaseDO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(KnowledgeBaseStatusEnum.ENABLED.getCode())
                .publishedStatus(KnowledgeBasePublishedStatusEnum.UNPUBLISHED.getCode())
                .documentCount(0)
                .chunkCount(0)
                .deleted(0)
                .build();
    }

    public KnowledgeBaseDO toUpdateDO(KnowledgeBaseUpdateRequest request) {
        return KnowledgeBaseDO.builder()
                .id(request.getId())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public KnowledgeBasePageQuery toPageQuery(KnowledgeBasePageRequest request) {
        return KnowledgeBasePageQuery.builder()
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .name(request.getName())
                .status(request.getStatus())
                .createStartTime(request.getCreateStartTime())
                .createEndTime(request.getCreateEndTime())
                .build();
    }

    public KnowledgeBaseResponse toResponse(KnowledgeBaseDO knowledgeBaseDO) {
        return KnowledgeBaseResponse.builder()
                .id(knowledgeBaseDO.getId())
                .name(knowledgeBaseDO.getName())
                .description(knowledgeBaseDO.getDescription())
                .status(knowledgeBaseDO.getStatus())
                .statusName(KnowledgeBaseStatusEnum.descriptionOf(knowledgeBaseDO.getStatus()))
                .publishedStatus(knowledgeBaseDO.getPublishedStatus())
                .publishedStatusName(KnowledgeBasePublishedStatusEnum.descriptionOf(knowledgeBaseDO.getPublishedStatus()))
                .documentCount(knowledgeBaseDO.getDocumentCount())
                .chunkCount(knowledgeBaseDO.getChunkCount())
                .createTime(knowledgeBaseDO.getCreateTime())
                .updateTime(knowledgeBaseDO.getUpdateTime())
                .build();
    }
}
