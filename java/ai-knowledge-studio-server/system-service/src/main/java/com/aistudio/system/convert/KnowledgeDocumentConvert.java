package com.aistudio.system.convert;

import com.aistudio.system.entity.FileResourceDO;
import com.aistudio.system.entity.KnowledgeChunkDO;
import com.aistudio.system.entity.KnowledgeDocumentDO;
import com.aistudio.system.entity.KnowledgeDocumentVersionDO;
import com.aistudio.system.entity.KnowledgeProcessTaskDO;
import com.aistudio.system.enums.DocumentIndexStatusEnum;
import com.aistudio.system.enums.DocumentParseStatusEnum;
import com.aistudio.system.enums.DocumentPublishStatusEnum;
import com.aistudio.system.enums.DocumentReviewStatusEnum;
import com.aistudio.system.enums.ProcessTaskStageEnum;
import com.aistudio.system.enums.ProcessTaskStatusEnum;
import com.aistudio.system.model.request.KnowledgeChunkPageRequest;
import com.aistudio.system.model.request.KnowledgeDocumentPageRequest;
import com.aistudio.system.model.response.KnowledgeChunkResponse;
import com.aistudio.system.model.response.KnowledgeDocumentUploadResponse;
import com.aistudio.system.model.response.KnowledgeDocumentResponse;
import com.aistudio.system.model.response.KnowledgeTaskProgressResponse;
import com.aistudio.system.query.KnowledgeChunkPageQuery;
import com.aistudio.system.query.KnowledgeDocumentPageQuery;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档上传对象转换器，集中处理 DO、Response 之间的转换。
 */
@Component
public class KnowledgeDocumentConvert {

    @Resource
    private ObjectMapper objectMapper;

    public FileResourceDO toFileResourceDO(String fileName, String fileExt, long fileSize, String fileHash,
                                           String bucketName, String objectKey, String contentType) {
        return FileResourceDO.builder()
                .fileName(fileName)
                .fileExt(fileExt)
                .fileSize(fileSize)
                .fileHash(fileHash)
                .bucketName(bucketName)
                .objectKey(objectKey)
                .contentType(contentType)
                .deleted(0)
                .build();
    }

    public KnowledgeDocumentDO toDocumentDO(Long knowledgeBaseId, Long directoryId, String documentName,
                                            FileResourceDO fileResourceDO) {
        return KnowledgeDocumentDO.builder()
                .knowledgeBaseId(knowledgeBaseId)
                .directoryId(directoryId)
                .name(documentName)
                .fileResourceId(fileResourceDO.getId())
                .fileName(fileResourceDO.getFileName())
                .fileExt(fileResourceDO.getFileExt())
                .fileSize(fileResourceDO.getFileSize())
                .fileHash(fileResourceDO.getFileHash())
                .parseStatus(DocumentParseStatusEnum.UPLOADED.getCode())
                .indexStatus(DocumentIndexStatusEnum.WAIT_INDEX.getCode())
                .reviewStatus(DocumentReviewStatusEnum.NOT_SUBMITTED.getCode())
                .publishStatus(DocumentPublishStatusEnum.UNPUBLISHED.getCode())
                .chunkCount(0)
                .deleted(0)
                .build();
    }

    public KnowledgeDocumentVersionDO toVersionDO(Long documentId, Integer versionNo, FileResourceDO fileResourceDO,
                                                  String chunkConfigSnapshot) {
        return KnowledgeDocumentVersionDO.builder()
                .documentId(documentId)
                .versionNo(versionNo)
                .fileResourceId(fileResourceDO.getId())
                .fileHash(fileResourceDO.getFileHash())
                .chunkConfigSnapshot(chunkConfigSnapshot)
                .parseStatus(DocumentParseStatusEnum.UPLOADED.getCode())
                .indexStatus(DocumentIndexStatusEnum.WAIT_INDEX.getCode())
                .chunkCount(0)
                .tokenCount(0)
                .deleted(0)
                .build();
    }

    public KnowledgeProcessTaskDO toProcessTaskDO(String taskNo, Long knowledgeBaseId, Long directoryId,
                                                  Long documentId, Long versionId) {
        return KnowledgeProcessTaskDO.builder()
                .taskNo(taskNo)
                .knowledgeBaseId(knowledgeBaseId)
                .directoryId(directoryId)
                .documentId(documentId)
                .documentVersionId(versionId)
                .taskType(ProcessTaskStageEnum.DOCUMENT_PARSE_CHUNK.getCode())
                .stageCode(ProcessTaskStageEnum.DOCUMENT_PARSE_CHUNK.getCode())
                .taskStatus(ProcessTaskStatusEnum.QUEUED.getCode())
                .progress(0)
                .retryCount(0)
                .deleted(0)
                .build();
    }

    public KnowledgeDocumentUploadResponse toUploadResponse(FileResourceDO fileResourceDO, KnowledgeDocumentDO documentDO,
                                                            KnowledgeDocumentVersionDO versionDO,
                                                            KnowledgeProcessTaskDO taskDO) {
        return KnowledgeDocumentUploadResponse.builder()
                .fileResourceId(fileResourceDO.getId())
                .documentId(documentDO.getId())
                .versionId(versionDO.getId())
                .taskId(taskDO.getId())
                .taskNo(taskDO.getTaskNo())
                .mqMessageId(taskDO.getMqMessageId())
                .fileHash(fileResourceDO.getFileHash())
                .parseStatus(documentDO.getParseStatus())
                .taskStatus(taskDO.getTaskStatus())
                .build();
    }

    public KnowledgeDocumentPageQuery toPageQuery(KnowledgeDocumentPageRequest request) {
        return KnowledgeDocumentPageQuery.builder()
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .directoryId(request.getDirectoryId())
                .name(request.getName())
                .parseStatus(request.getParseStatus())
                .publishStatus(request.getPublishStatus())
                .pageNo(request.getPageNo())
                .pageSize(Math.min(request.getPageSize(), 100L))
                .build();
    }

    public KnowledgeDocumentResponse toResponse(KnowledgeDocumentDO documentDO) {
        return KnowledgeDocumentResponse.builder()
                .id(documentDO.getId())
                .knowledgeBaseId(documentDO.getKnowledgeBaseId())
                .directoryId(documentDO.getDirectoryId())
                .name(documentDO.getName())
                .currentVersionId(documentDO.getCurrentVersionId())
                .fileName(documentDO.getFileName())
                .fileExt(documentDO.getFileExt())
                .fileSize(documentDO.getFileSize())
                .parseStatus(documentDO.getParseStatus())
                .indexStatus(documentDO.getIndexStatus())
                .reviewStatus(documentDO.getReviewStatus())
                .publishStatus(documentDO.getPublishStatus())
                .chunkCount(documentDO.getChunkCount())
                .errorMessage(documentDO.getErrorMessage())
                .createTime(documentDO.getCreateTime())
                .updateTime(documentDO.getUpdateTime())
                .build();
    }

    public KnowledgeTaskProgressResponse toTaskProgressResponse(KnowledgeProcessTaskDO taskDO) {
        return KnowledgeTaskProgressResponse.builder()
                .taskId(taskDO.getId())
                .taskNo(taskDO.getTaskNo())
                .documentId(taskDO.getDocumentId())
                .versionId(taskDO.getDocumentVersionId())
                .stageCode(taskDO.getStageCode())
                .taskStatus(taskDO.getTaskStatus())
                .progress(taskDO.getProgress())
                .errorCode(taskDO.getErrorCode())
                .errorMessage(taskDO.getErrorMessage())
                .startTime(taskDO.getStartTime())
                .finishTime(taskDO.getFinishTime())
                .updateTime(taskDO.getUpdateTime())
                .build();
    }

    public KnowledgeChunkPageQuery toChunkPageQuery(KnowledgeChunkPageRequest request) {
        return KnowledgeChunkPageQuery.builder()
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .directoryId(request.getDirectoryId())
                .documentId(request.getDocumentId())
                .versionId(request.getVersionId())
                .publishStatus(request.getPublishStatus())
                .enabled(request.getEnabled())
                .pageNo(request.getPageNo())
                .pageSize(Math.min(request.getPageSize(), 100L))
                .build();
    }

    public KnowledgeChunkResponse toChunkResponse(KnowledgeChunkDO chunkDO) {
        return KnowledgeChunkResponse.builder()
                .id(chunkDO.getId())
                .chunkId(chunkDO.getChunkId())
                .knowledgeBaseId(chunkDO.getKnowledgeBaseId())
                .directoryId(chunkDO.getDirectoryId())
                .documentId(chunkDO.getDocumentId())
                .documentVersionId(chunkDO.getDocumentVersionId())
                .chunkNo(chunkDO.getChunkNo())
                .titlePath(chunkDO.getTitlePath())
                .contentPreview(chunkDO.getContentPreview())
                .tokenCount(chunkDO.getTokenCount())
                .charCount(chunkDO.getCharCount())
                .pageStart(chunkDO.getPageStart())
                .pageEnd(chunkDO.getPageEnd())
                .blockIds(parseBlockIds(chunkDO.getMetadataJson()))
                .publishStatus(chunkDO.getPublishStatus())
                .enabled(chunkDO.getEnabled())
                .createTime(chunkDO.getCreateTime())
                .updateTime(chunkDO.getUpdateTime())
                .build();
    }

    private List<String> parseBlockIds(String metadataJson) {
        List<String> blockIds = new ArrayList<>();
        if (metadataJson == null || metadataJson.isBlank()) {
            return blockIds;
        }
        try {
            JsonNode blockNode = objectMapper.readTree(metadataJson).get("block_ids");
            if (blockNode != null && blockNode.isArray()) {
                blockNode.forEach(item -> blockIds.add(item.asText()));
            }
        } catch (Exception ignored) {
            return blockIds;
        }
        return blockIds;
    }
}
