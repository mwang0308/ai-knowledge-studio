package com.aistudio.system.convert;

import com.aistudio.system.entity.KnowledgeDirectoryDO;
import com.aistudio.system.enums.KnowledgeDirectoryStatusEnum;
import com.aistudio.system.model.request.KnowledgeDirectoryCreateRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryTreeRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryUpdateRequest;
import com.aistudio.system.model.response.KnowledgeDirectoryResponse;
import com.aistudio.system.query.KnowledgeDirectoryTreeQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 知识库目录对象转换器，集中处理 Request、Query、DO、Response 之间的转换。
 */
@Component
public class KnowledgeDirectoryConvert {

    public KnowledgeDirectoryDO toCreateDO(KnowledgeDirectoryCreateRequest request, String path, Integer level) {
        return KnowledgeDirectoryDO.builder()
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .parentId(request.getParentId())
                .name(request.getName())
                .description(request.getDescription())
                .path(path)
                .level(level)
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .status(KnowledgeDirectoryStatusEnum.ENABLED.getCode())
                .deleted(0)
                .build();
    }

    public KnowledgeDirectoryDO toUpdateDO(KnowledgeDirectoryUpdateRequest request, String path, Integer level) {
        return KnowledgeDirectoryDO.builder()
                .id(request.getId())
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .parentId(request.getParentId())
                .name(request.getName())
                .description(request.getDescription())
                .path(path)
                .level(level)
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .build();
    }

    public KnowledgeDirectoryTreeQuery toTreeQuery(KnowledgeDirectoryTreeRequest request) {
        return KnowledgeDirectoryTreeQuery.builder()
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .description(request.getDescription())
                .status(request.getStatus())
                .createStartTime(request.getCreateStartTime())
                .createEndTime(request.getCreateEndTime())
                .build();
    }

    public KnowledgeDirectoryResponse toResponse(KnowledgeDirectoryDO directoryDO) {
        return KnowledgeDirectoryResponse.builder()
                .id(directoryDO.getId())
                .knowledgeBaseId(directoryDO.getKnowledgeBaseId())
                .parentId(directoryDO.getParentId())
                .name(directoryDO.getName())
                .description(directoryDO.getDescription())
                .path(directoryDO.getPath())
                .level(directoryDO.getLevel())
                .sortOrder(directoryDO.getSortOrder())
                .status(directoryDO.getStatus())
                .statusName(KnowledgeDirectoryStatusEnum.descriptionOf(directoryDO.getStatus()))
                .createTime(directoryDO.getCreateTime())
                .updateTime(directoryDO.getUpdateTime())
                .children(new ArrayList<>())
                .build();
    }
}
