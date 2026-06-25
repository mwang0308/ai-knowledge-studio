package com.aistudio.system.service.impl;

import com.aistudio.foundation.context.TraceContext;
import com.aistudio.foundation.domain.ErrorCode;
import com.aistudio.foundation.domain.PageResult;
import com.aistudio.foundation.exception.BusinessException;
import com.aistudio.system.convert.KnowledgeDocumentConvert;
import com.aistudio.system.entity.FileResourceDO;
import com.aistudio.system.entity.KnowledgeChunkDO;
import com.aistudio.system.entity.KnowledgeBaseDO;
import com.aistudio.system.entity.KnowledgeDirectoryDO;
import com.aistudio.system.entity.KnowledgeDocumentDO;
import com.aistudio.system.entity.KnowledgeDocumentVersionDO;
import com.aistudio.system.entity.KnowledgeProcessTaskDO;
import com.aistudio.system.entity.KnowledgePublishRecordDO;
import com.aistudio.system.entity.KnowledgeRetrievalTestDO;
import com.aistudio.system.entity.KnowledgeReviewRecordDO;
import com.aistudio.system.mapper.IFileResourceMapper;
import com.aistudio.system.mapper.IKnowledgeBaseMapper;
import com.aistudio.system.mapper.IKnowledgeChunkMapper;
import com.aistudio.system.mapper.IKnowledgeDirectoryMapper;
import com.aistudio.system.mapper.IKnowledgeDocumentMapper;
import com.aistudio.system.mapper.IKnowledgeDocumentVersionMapper;
import com.aistudio.system.mapper.IKnowledgePublishRecordMapper;
import com.aistudio.system.mapper.IKnowledgeProcessTaskMapper;
import com.aistudio.system.mapper.IKnowledgeRetrievalTestMapper;
import com.aistudio.system.mapper.IKnowledgeReviewRecordMapper;
import com.aistudio.system.model.request.DocumentProcessCallbackRequest;
import com.aistudio.system.model.request.DocumentProcessCallbackResult;
import com.aistudio.system.model.request.KnowledgeChunkPageRequest;
import com.aistudio.system.model.request.KnowledgeDocumentPageRequest;
import com.aistudio.system.model.request.KnowledgeDocumentUploadRequest;
import com.aistudio.system.model.request.PublishSubmitRequest;
import com.aistudio.system.model.request.RetrievalTestRequest;
import com.aistudio.system.model.request.ReviewSubmitRequest;
import com.aistudio.system.model.response.KnowledgeChunkResponse;
import com.aistudio.system.model.response.KnowledgeDocumentResponse;
import com.aistudio.system.model.response.KnowledgeDocumentUploadResponse;
import com.aistudio.system.model.response.KnowledgeTaskProgressResponse;
import com.aistudio.system.model.response.RetrievalHitResponse;
import com.aistudio.system.model.response.RetrievalTestResponse;
import com.aistudio.system.mq.DocumentProcessMessage;
import com.aistudio.system.mq.DocumentProcessProducer;
import com.aistudio.system.query.KnowledgeChunkPageQuery;
import com.aistudio.system.query.KnowledgeDocumentPageQuery;
import com.aistudio.system.query.RetrievalCandidateQuery;
import com.aistudio.system.service.IKnowledgeDocumentService;
import com.aistudio.system.storage.DocumentStorageService;
import com.aistudio.system.enums.DocumentIndexStatusEnum;
import com.aistudio.system.enums.DocumentParseStatusEnum;
import com.aistudio.system.enums.DocumentPublishStatusEnum;
import com.aistudio.system.enums.DocumentReviewStatusEnum;
import com.aistudio.system.enums.ProcessTaskStatusEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 知识文档服务实现，负责上传文件、落库并投递文档处理 MQ。
 */
@Slf4j
@Service
public class KnowledgeDocumentServiceImpl implements IKnowledgeDocumentService {

    private static final String DEFAULT_CHUNK_CONFIG_SNAPSHOT = "{\"source\":\"system-default\"}";
    private static final int CALLBACK_SUCCESS_PROGRESS = 100;
    private static final Set<String> TERMINAL_TASK_STATUS = Set.of(
            ProcessTaskStatusEnum.SUCCESS.getCode(), ProcessTaskStatusEnum.FAILED.getCode());

    @Resource
    private IKnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private IKnowledgeDirectoryMapper knowledgeDirectoryMapper;

    @Resource
    private IFileResourceMapper fileResourceMapper;

    @Resource
    private IKnowledgeDocumentMapper knowledgeDocumentMapper;

    @Resource
    private IKnowledgeDocumentVersionMapper knowledgeDocumentVersionMapper;

    @Resource
    private IKnowledgeProcessTaskMapper knowledgeProcessTaskMapper;

    @Resource
    private IKnowledgeChunkMapper knowledgeChunkMapper;

    @Resource
    private IKnowledgeRetrievalTestMapper knowledgeRetrievalTestMapper;

    @Resource
    private IKnowledgeReviewRecordMapper knowledgeReviewRecordMapper;

    @Resource
    private IKnowledgePublishRecordMapper knowledgePublishRecordMapper;

    @Resource
    private KnowledgeDocumentConvert knowledgeDocumentConvert;

    @Resource
    private DocumentStorageService documentStorageService;

    @Resource
    private DocumentProcessProducer documentProcessProducer;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${knowledge.document.max-file-size}")
    private long maxFileSize;

    @Value("${knowledge.document.allowed-ext}")
    private String allowedExtText;

    @Value("${knowledge.mq.callback-url}")
    private String callbackUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDocumentUploadResponse uploadDocument(KnowledgeDocumentUploadRequest request) {
        MultipartFile file = request.getFile();
        log.info("上传文档开始，knowledgeBaseId={}，directoryId={}，fileName={}，fileSize={}",
                request.getKnowledgeBaseId(), request.getDirectoryId(),
                file == null ? null : file.getOriginalFilename(), file == null ? null : file.getSize());

        validateRequest(request);
        KnowledgeBaseDO knowledgeBaseDO = getExistingKnowledgeBase(request.getKnowledgeBaseId());
        KnowledgeDirectoryDO directoryDO = getExistingDirectory(request.getDirectoryId(), request.getKnowledgeBaseId());
        FileMetadata fileMetadata = readAndCheckFile(file);

        Integer duplicateCount = knowledgeDocumentMapper.countDuplicateFile(
                request.getKnowledgeBaseId(), request.getDirectoryId(), fileMetadata.fileHash());
        if (duplicateCount != null && duplicateCount > 0) {
            log.warn("上传文档失败，同一知识库目录下文件重复，knowledgeBaseId={}，directoryId={}，fileHash={}",
                    request.getKnowledgeBaseId(), request.getDirectoryId(), fileMetadata.fileHash());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "同一知识库目录下已存在相同文件");
        }

        FileResourceDO fileResourceDO = getOrCreateFileResource(fileMetadata);
        KnowledgeDocumentDO documentDO = createDocument(request, fileMetadata, fileResourceDO);
        KnowledgeDocumentVersionDO versionDO = createDocumentVersion(documentDO, fileResourceDO);
        knowledgeDocumentMapper.updateCurrentVersionId(documentDO.getId(), versionDO.getId());
        knowledgeBaseMapper.increaseDocumentCount(request.getKnowledgeBaseId(), 1);
        documentDO.setCurrentVersionId(versionDO.getId());

        KnowledgeProcessTaskDO taskDO = createProcessTask(request, documentDO, versionDO);
        String messageId = sendParseChunkMessage(knowledgeBaseDO, directoryDO, fileResourceDO, documentDO, versionDO, taskDO);
        taskDO.setMqMessageId(messageId);
        knowledgeProcessTaskMapper.updateMqMessageId(taskDO.getId(), messageId);

        log.info("上传文档完成，documentId={}，versionId={}，taskId={}，mqMessageId={}",
                documentDO.getId(), versionDO.getId(), taskDO.getId(), messageId);
        return knowledgeDocumentConvert.toUploadResponse(fileResourceDO, documentDO, versionDO, taskDO);
    }

    @Override
    public PageResult<KnowledgeDocumentResponse> pageDocument(KnowledgeDocumentPageRequest request) {
        log.info("分页查询知识文档，knowledgeBaseId={}，directoryId={}，pageNo={}，pageSize={}",
                request.getKnowledgeBaseId(), request.getDirectoryId(), request.getPageNo(), request.getPageSize());
        KnowledgeDocumentPageQuery query = knowledgeDocumentConvert.toPageQuery(request);
        Page<KnowledgeDocumentDO> page = Page.of(query.getPageNo(), query.getPageSize());
        IPage<KnowledgeDocumentDO> documentPage = knowledgeDocumentMapper.selectDocumentPage(page, query);
        return new PageResult<>(
                documentPage.getTotal(),
                query.getPageNo(),
                query.getPageSize(),
                documentPage.getRecords().stream().map(knowledgeDocumentConvert::toResponse).toList()
        );
    }

    @Override
    public KnowledgeTaskProgressResponse getTaskProgress(Long taskId) {
        KnowledgeProcessTaskDO taskDO = knowledgeProcessTaskMapper.selectById(taskId);
        if (taskDO == null || Integer.valueOf(1).equals(taskDO.getDeleted())) {
            log.warn("查询任务进度失败，任务不存在，taskId={}", taskId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "任务不存在");
        }
        return knowledgeDocumentConvert.toTaskProgressResponse(taskDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleProcessCallback(DocumentProcessCallbackRequest request) {
        log.info("接收文档处理回调，taskId={}，stageCode={}，status={}，progress={}",
                request.getTaskId(), request.getStageCode(), request.getStatus(), request.getProgress());
        KnowledgeProcessTaskDO taskDO = getCallbackTask(request.getTaskId());
        validateCallback(request, taskDO);

        if (TERMINAL_TASK_STATUS.contains(taskDO.getTaskStatus())) {
            log.info("文档处理回调重复到达，按幂等成功处理，taskId={}，currentStatus={}",
                    taskDO.getId(), taskDO.getTaskStatus());
            return;
        }

        String callbackStatus = request.getStatus().trim().toUpperCase(Locale.ROOT);
        if (ProcessTaskStatusEnum.RUNNING.getCode().equals(callbackStatus) || "PROCESSING".equals(callbackStatus)) {
            handleRunningCallback(taskDO, request);
            return;
        }
        if (ProcessTaskStatusEnum.SUCCESS.getCode().equals(callbackStatus)) {
            handleSuccessCallback(taskDO, request);
            return;
        }
        if (ProcessTaskStatusEnum.FAILED.getCode().equals(callbackStatus)) {
            handleFailedCallback(taskDO, request);
            return;
        }
        log.warn("文档处理回调状态非法，taskId={}，status={}", request.getTaskId(), request.getStatus());
        throw new BusinessException(ErrorCode.PARAM_ERROR, "回调状态非法");
    }

    @Override
    public PageResult<KnowledgeChunkResponse> pageChunk(KnowledgeChunkPageRequest request) {
        log.info("分页查询知识分片，documentId={}，versionId={}，pageNo={}，pageSize={}",
                request.getDocumentId(), request.getVersionId(), request.getPageNo(), request.getPageSize());
        KnowledgeChunkPageQuery query = knowledgeDocumentConvert.toChunkPageQuery(request);
        Page<KnowledgeChunkDO> page = Page.of(query.getPageNo(), query.getPageSize());
        IPage<KnowledgeChunkDO> chunkPage = knowledgeChunkMapper.selectChunkPage(page, query);
        return new PageResult<>(
                chunkPage.getTotal(),
                query.getPageNo(),
                query.getPageSize(),
                chunkPage.getRecords().stream().map(knowledgeDocumentConvert::toChunkResponse).toList()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RetrievalTestResponse testRetrieval(RetrievalTestRequest request) {
        long start = System.currentTimeMillis();
        log.info("执行召回测试，knowledgeBaseId={}，directoryId={}，documentId={}，scope={}，topK={}",
                request.getKnowledgeBaseId(), request.getDirectoryId(), request.getDocumentId(),
                request.getTestScope(), request.getTopK());
        RetrievalCandidateQuery query = RetrievalCandidateQuery.builder()
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .directoryId(request.getDirectoryId())
                .documentId(request.getDocumentId())
                .testScope(request.getTestScope())
                .limitSize(500)
                .build();
        List<RetrievalHitResponse> hits = knowledgeChunkMapper.selectRetrievalCandidates(query).stream()
                .map(chunkDO -> toHitResponse(chunkDO, request.getQueryText()))
                .filter(hit -> hit.getScore() > 0)
                .sorted(Comparator.comparing(RetrievalHitResponse::getScore).reversed())
                .limit(normalizeTopK(request.getTopK()))
                .toList();
        List<RetrievalHitResponse> rankedHits = rankHits(hits);
        double topScore = rankedHits.isEmpty() ? 0D : rankedHits.get(0).getScore();
        long latency = System.currentTimeMillis() - start;

        KnowledgeRetrievalTestDO testDO = KnowledgeRetrievalTestDO.builder()
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .directoryId(request.getDirectoryId())
                .documentId(request.getDocumentId())
                .queryText(request.getQueryText())
                .topK(normalizeTopK(request.getTopK()))
                .testScope(request.getTestScope())
                .resultJson(toJson(rankedHits))
                .topScore(BigDecimal.valueOf(topScore).setScale(4, RoundingMode.HALF_UP))
                .passed(topScore >= 0.60D ? 1 : 0)
                .latencyMs(latency)
                .deleted(0)
                .build();
        knowledgeRetrievalTestMapper.insert(testDO);
        log.info("召回测试完成，testId={}，hitCount={}，topScore={}，latencyMs={}",
                testDO.getId(), rankedHits.size(), topScore, latency);
        return RetrievalTestResponse.builder()
                .testId(testDO.getId())
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .directoryId(request.getDirectoryId())
                .documentId(request.getDocumentId())
                .queryText(request.getQueryText())
                .testScope(request.getTestScope())
                .topK(normalizeTopK(request.getTopK()))
                .topScore(topScore)
                .latencyMs(latency)
                .hits(rankedHits)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void passReview(ReviewSubmitRequest request) {
        KnowledgeDocumentDO documentDO = getExistingDocument(request.getDocumentId());
        ensureParsedAndIndexed(documentDO);
        knowledgeDocumentMapper.updateReviewStatus(documentDO.getId(), DocumentReviewStatusEnum.AUDIT_PASSED.getCode());
        insertReviewRecord(documentDO, DocumentReviewStatusEnum.AUDIT_PASSED.getCode(), request.getReviewComment());
        log.info("文档审核通过，documentId={}，versionId={}", documentDO.getId(), documentDO.getCurrentVersionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectReview(ReviewSubmitRequest request) {
        KnowledgeDocumentDO documentDO = getExistingDocument(request.getDocumentId());
        knowledgeDocumentMapper.updateReviewStatus(documentDO.getId(), DocumentReviewStatusEnum.AUDIT_REJECTED.getCode());
        insertReviewRecord(documentDO, DocumentReviewStatusEnum.AUDIT_REJECTED.getCode(), request.getReviewComment());
        log.info("文档审核驳回，documentId={}，versionId={}", documentDO.getId(), documentDO.getCurrentVersionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishDocument(PublishSubmitRequest request) {
        KnowledgeDocumentDO documentDO = getExistingDocument(request.getDocumentId());
        if (!DocumentReviewStatusEnum.AUDIT_PASSED.getCode().equals(documentDO.getReviewStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "文档审核通过后才能发布");
        }
        knowledgeDocumentMapper.updatePublishStatus(documentDO.getId(), DocumentPublishStatusEnum.PUBLISHED.getCode());
        knowledgeChunkMapper.updatePublishStatus(documentDO.getId(), documentDO.getCurrentVersionId(),
                DocumentPublishStatusEnum.PUBLISHED.getCode(), 1);
        insertPublishRecord(documentDO, "PUBLISH", DocumentPublishStatusEnum.PUBLISHED.getCode());
        log.info("文档发布完成，documentId={}，versionId={}", documentDO.getId(), documentDO.getCurrentVersionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineDocument(PublishSubmitRequest request) {
        KnowledgeDocumentDO documentDO = getExistingDocument(request.getDocumentId());
        knowledgeDocumentMapper.updatePublishStatus(documentDO.getId(), DocumentPublishStatusEnum.OFFLINE.getCode());
        knowledgeChunkMapper.updatePublishStatus(documentDO.getId(), documentDO.getCurrentVersionId(),
                DocumentPublishStatusEnum.OFFLINE.getCode(), 0);
        insertPublishRecord(documentDO, "OFFLINE", DocumentPublishStatusEnum.OFFLINE.getCode());
        log.info("文档下架完成，documentId={}，versionId={}", documentDO.getId(), documentDO.getCurrentVersionId());
    }

    private KnowledgeProcessTaskDO getCallbackTask(Long taskId) {
        KnowledgeProcessTaskDO taskDO = knowledgeProcessTaskMapper.selectById(taskId);
        if (taskDO == null || Integer.valueOf(1).equals(taskDO.getDeleted())) {
            log.warn("文档处理回调失败，任务不存在，taskId={}", taskId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "任务不存在");
        }
        return taskDO;
    }

    private void validateCallback(DocumentProcessCallbackRequest request, KnowledgeProcessTaskDO taskDO) {
        if (!taskDO.getStageCode().equals(request.getStageCode())) {
            log.warn("文档处理回调失败，阶段不匹配，taskId={}，taskStage={}，callbackStage={}",
                    taskDO.getId(), taskDO.getStageCode(), request.getStageCode());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "回调阶段不匹配");
        }
        if (request.getDocumentId() != null && !taskDO.getDocumentId().equals(request.getDocumentId())) {
            log.warn("文档处理回调失败，文档不匹配，taskId={}，taskDocumentId={}，callbackDocumentId={}",
                    taskDO.getId(), taskDO.getDocumentId(), request.getDocumentId());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "回调文档不匹配");
        }
    }

    private void handleRunningCallback(KnowledgeProcessTaskDO taskDO, DocumentProcessCallbackRequest request) {
        int progress = normalizeProgress(request.getProgress(), 1, 99);
        knowledgeDocumentMapper.updateParseRunning(taskDO.getDocumentId(), taskDO.getDocumentVersionId());
        knowledgeDocumentVersionMapper.updateParseRunning(taskDO.getDocumentVersionId());
        knowledgeProcessTaskMapper.updateRunning(taskDO.getId(), progress);
        log.info("文档处理任务进入处理中，taskId={}，documentId={}，progress={}",
                taskDO.getId(), taskDO.getDocumentId(), progress);
    }

    private void handleSuccessCallback(KnowledgeProcessTaskDO taskDO, DocumentProcessCallbackRequest request) {
        DocumentProcessCallbackResult result = request.getResult();
        int chunkCount = result == null || result.getChunkCount() == null ? 0 : result.getChunkCount();
        int tokenCount = result == null || result.getTokenCount() == null ? 0 : result.getTokenCount();
        String parserName = result == null ? null : result.getParserName();

        // 解析分片成功后先落 MySQL 分片元数据，正文仍保存在 MinIO chunks.jsonl，后续可重建 ES/Milvus。
        persistChunksFromArtifact(taskDO, result);
        knowledgeDocumentMapper.updateParseSuccess(taskDO.getDocumentId(), taskDO.getDocumentVersionId(), chunkCount);
        knowledgeDocumentVersionMapper.updateParseSuccess(taskDO.getDocumentVersionId(), chunkCount, tokenCount, parserName);
        knowledgeDocumentMapper.updateIndexSuccess(taskDO.getDocumentId(), taskDO.getDocumentVersionId());
        knowledgeDocumentVersionMapper.updateIndexSuccess(taskDO.getDocumentVersionId(),
                result == null ? "local-preview" : result.getEmbeddingModelCode());
        knowledgeProcessTaskMapper.updateSuccess(taskDO.getId(), CALLBACK_SUCCESS_PROGRESS);
        log.info("文档处理任务成功，taskId={}，documentId={}，versionId={}，chunkCount={}，tokenCount={}",
                taskDO.getId(), taskDO.getDocumentId(), taskDO.getDocumentVersionId(), chunkCount, tokenCount);
    }

    private void persistChunksFromArtifact(KnowledgeProcessTaskDO taskDO, DocumentProcessCallbackResult result) {
        if (result == null || result.getChunksObjectKey() == null || result.getChunksObjectKey().isBlank()) {
            log.warn("文档处理成功但未返回 chunks 产物，taskId={}", taskDO.getId());
            return;
        }
        knowledgeChunkMapper.deleteByTaskId(taskDO.getId());
        String chunksText = documentStorageService.readTextObject(documentStorageService.getBucketName(), result.getChunksObjectKey());
        int savedCount = 0;
        for (String line : chunksText.split("\\R")) {
            if (line == null || line.isBlank()) {
                continue;
            }
            KnowledgeChunkDO chunkDO = toChunkDO(taskDO, result.getChunksObjectKey(), line);
            knowledgeChunkMapper.insert(chunkDO);
            savedCount++;
        }
        log.info("分片元数据入库完成，taskId={}，savedCount={}", taskDO.getId(), savedCount);
    }

    private KnowledgeChunkDO toChunkDO(KnowledgeProcessTaskDO taskDO, String chunksObjectKey, String jsonLine) {
        try {
            JsonNode node = objectMapper.readTree(jsonLine);
            String chunkText = textValue(node, "chunk_text", "");
            return KnowledgeChunkDO.builder()
                    .chunkId(textValue(node, "chunk_id", UUID.randomUUID().toString()))
                    .knowledgeBaseId(taskDO.getKnowledgeBaseId())
                    .directoryId(taskDO.getDirectoryId())
                    .documentId(taskDO.getDocumentId())
                    .documentVersionId(taskDO.getDocumentVersionId())
                    .processTaskId(taskDO.getId())
                    .chunkNo(intValue(node, "chunk_no", 0))
                    .chunkHash(sha256(chunkText.getBytes()))
                    .titlePath(textValue(node, "title_path", "root"))
                    .contentPreview(chunkText.length() > 1000 ? chunkText.substring(0, 1000) : chunkText)
                    .contentObjectKey(chunksObjectKey)
                    .tokenCount(intValue(node, "token_count", 0))
                    .charCount(intValue(node, "char_count", chunkText.length()))
                    .pageStart(intNullable(node, "page_start"))
                    .pageEnd(intNullable(node, "page_end"))
                    .sheetName(textNullable(node, "sheet_name"))
                    .rowStart(intNullable(node, "row_start"))
                    .rowEnd(intNullable(node, "row_end"))
                    .esIndexName("rag_chunk_index_v1")
                    .esDocId(textValue(node, "chunk_id", null))
                    .milvusCollectionName("rag_chunk_bge_m3_1024")
                    .milvusVectorId(textValue(node, "chunk_id", null))
                    .publishStatus(DocumentPublishStatusEnum.UNPUBLISHED.getCode())
                    .enabled(0)
                    .metadataJson(jsonLine)
                    .deleted(0)
                    .build();
        } catch (Exception exception) {
            log.error("解析分片 JSONL 失败，taskId={}，line={}", taskDO.getId(), jsonLine, exception);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析分片产物失败");
        }
    }

    private void handleFailedCallback(KnowledgeProcessTaskDO taskDO, DocumentProcessCallbackRequest request) {
        int progress = normalizeProgress(request.getProgress(), 0, 99);
        String errorMessage = trimErrorMessage(
                request.getErrorMessage() == null ? request.getMessage() : request.getErrorMessage());
        knowledgeDocumentMapper.updateParseFailed(taskDO.getDocumentId(), taskDO.getDocumentVersionId(), errorMessage);
        knowledgeDocumentVersionMapper.updateParseFailed(taskDO.getDocumentVersionId());
        knowledgeProcessTaskMapper.updateFailed(taskDO.getId(), progress, request.getErrorCode(), errorMessage);
        log.warn("文档处理任务失败，taskId={}，documentId={}，errorCode={}，errorMessage={}",
                taskDO.getId(), taskDO.getDocumentId(), request.getErrorCode(), errorMessage);
    }

    private int normalizeProgress(Integer progress, int min, int max) {
        int value = progress == null ? min : progress;
        return Math.max(min, Math.min(value, max));
    }

    private String trimErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "文档处理失败";
        }
        return errorMessage.length() > 512 ? errorMessage.substring(0, 512) : errorMessage;
    }

    private KnowledgeDocumentDO getExistingDocument(Long documentId) {
        KnowledgeDocumentDO documentDO = knowledgeDocumentMapper.selectById(documentId);
        if (documentDO == null || Integer.valueOf(1).equals(documentDO.getDeleted())) {
            log.warn("文档不存在或已删除，documentId={}", documentId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "文档不存在或已删除");
        }
        return documentDO;
    }

    private void ensureParsedAndIndexed(KnowledgeDocumentDO documentDO) {
        if (!DocumentParseStatusEnum.PARSE_CHUNKED.getCode().equals(documentDO.getParseStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "文档解析分片完成后才能审核");
        }
        if (!DocumentIndexStatusEnum.INDEXED.getCode().equals(documentDO.getIndexStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "文档索引完成后才能审核");
        }
    }

    private void insertReviewRecord(KnowledgeDocumentDO documentDO, String reviewStatus, String reviewComment) {
        knowledgeReviewRecordMapper.insert(KnowledgeReviewRecordDO.builder()
                .knowledgeBaseId(documentDO.getKnowledgeBaseId())
                .documentId(documentDO.getId())
                .documentVersionId(documentDO.getCurrentVersionId())
                .reviewStatus(reviewStatus)
                .reviewComment(reviewComment)
                .reviewTime(LocalDateTime.now())
                .deleted(0)
                .build());
    }

    private void insertPublishRecord(KnowledgeDocumentDO documentDO, String action, String publishStatus) {
        knowledgePublishRecordMapper.insert(KnowledgePublishRecordDO.builder()
                .knowledgeBaseId(documentDO.getKnowledgeBaseId())
                .documentId(documentDO.getId())
                .documentVersionId(documentDO.getCurrentVersionId())
                .publishAction(action)
                .publishStatus(publishStatus)
                .operateTime(LocalDateTime.now())
                .deleted(0)
                .build());
    }

    private RetrievalHitResponse toHitResponse(KnowledgeChunkDO chunkDO, String queryText) {
        return RetrievalHitResponse.builder()
                .chunkId(chunkDO.getChunkId())
                .documentId(chunkDO.getDocumentId())
                .titlePath(chunkDO.getTitlePath())
                .contentPreview(chunkDO.getContentPreview())
                .pageStart(chunkDO.getPageStart())
                .pageEnd(chunkDO.getPageEnd())
                .score(scoreChunk(queryText, chunkDO.getContentPreview()))
                .publishStatus(chunkDO.getPublishStatus())
                .enabled(chunkDO.getEnabled())
                .build();
    }

    private List<RetrievalHitResponse> rankHits(List<RetrievalHitResponse> hits) {
        List<RetrievalHitResponse> ranked = new ArrayList<>();
        for (int index = 0; index < hits.size(); index++) {
            RetrievalHitResponse hit = hits.get(index);
            ranked.add(RetrievalHitResponse.builder()
                    .rankNo(index + 1)
                    .chunkId(hit.getChunkId())
                    .documentId(hit.getDocumentId())
                    .titlePath(hit.getTitlePath())
                    .contentPreview(hit.getContentPreview())
                    .pageStart(hit.getPageStart())
                    .pageEnd(hit.getPageEnd())
                    .score(hit.getScore())
                    .publishStatus(hit.getPublishStatus())
                    .enabled(hit.getEnabled())
                    .build());
        }
        return ranked;
    }

    private double scoreChunk(String queryText, String contentPreview) {
        if (queryText == null || contentPreview == null) {
            return 0D;
        }
        Set<String> queryTerms = tokenize(queryText);
        Set<String> contentTerms = tokenize(contentPreview);
        if (queryTerms.isEmpty() || contentTerms.isEmpty()) {
            return 0D;
        }
        long matched = queryTerms.stream().filter(contentTerms::contains).count();
        double termScore = matched * 1.0D / queryTerms.size();
        double containsBoost = contentPreview.contains(queryText) ? 0.30D : 0D;
        return Math.min(1D, BigDecimal.valueOf(termScore + containsBoost)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue());
    }

    private Set<String> tokenize(String text) {
        Set<String> terms = new LinkedHashSet<>();
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[\\p{Punct}\\s]+", "");
        for (int index = 0; index < normalized.length(); index += 2) {
            int end = Math.min(normalized.length(), index + 2);
            terms.add(normalized.substring(index, end));
        }
        return terms;
    }

    private int normalizeTopK(Integer topK) {
        if (topK == null) {
            return 5;
        }
        return Math.max(1, Math.min(topK, 20));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            log.warn("对象序列化失败", exception);
            return "[]";
        }
    }

    private String textValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? defaultValue : value.asText();
    }

    private String textNullable(JsonNode node, String fieldName) {
        return textValue(node, fieldName, null);
    }

    private Integer intNullable(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private int intValue(JsonNode node, String fieldName, int defaultValue) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? defaultValue : value.asInt();
    }

    private void validateRequest(KnowledgeDocumentUploadRequest request) {
        if (request.getKnowledgeBaseId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "知识库 ID 不能为空");
        }
        if (request.getDirectoryId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "目录 ID 不能为空");
        }
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件不能为空");
        }
    }

    private KnowledgeBaseDO getExistingKnowledgeBase(Long knowledgeBaseId) {
        KnowledgeBaseDO knowledgeBaseDO = knowledgeBaseMapper.selectKnowledgeBaseById(knowledgeBaseId);
        if (knowledgeBaseDO == null) {
            log.warn("上传文档失败，知识库不存在或已删除，knowledgeBaseId={}", knowledgeBaseId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "知识库不存在或已删除");
        }
        return knowledgeBaseDO;
    }

    private KnowledgeDirectoryDO getExistingDirectory(Long directoryId, Long knowledgeBaseId) {
        KnowledgeDirectoryDO directoryDO = knowledgeDirectoryMapper.selectKnowledgeDirectoryById(directoryId);
        if (directoryDO == null) {
            log.warn("上传文档失败，目录不存在或已删除，directoryId={}", directoryId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "目录不存在或已删除");
        }
        if (!knowledgeBaseId.equals(directoryDO.getKnowledgeBaseId())) {
            log.warn("上传文档失败，目录不属于知识库，directoryId={}，directoryBaseId={}，requestBaseId={}",
                    directoryId, directoryDO.getKnowledgeBaseId(), knowledgeBaseId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "目录不属于当前知识库");
        }
        return directoryDO;
    }

    private FileMetadata readAndCheckFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名不能为空");
            }
            byte[] fileBytes = file.getBytes();
            if (fileBytes.length > maxFileSize) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "文件大小超过限制");
            }

            String fileExt = extractFileExt(fileName);
            Set<String> allowedExt = Arrays.stream(allowedExtText.split(","))
                    .map(item -> item.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            if (!allowedExt.contains(fileExt)) {
                log.warn("上传文档失败，文件类型不支持，fileName={}，fileExt={}", fileName, fileExt);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "文件类型不支持");
            }

            String fileHash = sha256(fileBytes);
            return new FileMetadata(fileName.trim(), fileExt, file.getSize(), fileHash, file.getContentType(), fileBytes);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("读取上传文件失败", exception);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取上传文件失败");
        }
    }

    private FileResourceDO getOrCreateFileResource(FileMetadata fileMetadata) {
        FileResourceDO existing = fileResourceMapper.selectByFileHash(fileMetadata.fileHash());
        if (existing != null) {
            log.info("文件资源已存在，复用原始文件，fileResourceId={}，fileHash={}", existing.getId(), existing.getFileHash());
            return existing;
        }

        String objectKey = documentStorageService.saveOriginalFile(
                fileMetadata.fileBytes(), fileMetadata.fileHash(), fileMetadata.fileName(), fileMetadata.contentType());
        FileResourceDO fileResourceDO = knowledgeDocumentConvert.toFileResourceDO(
                fileMetadata.fileName(), fileMetadata.fileExt(), fileMetadata.fileSize(), fileMetadata.fileHash(),
                documentStorageService.getBucketName(), objectKey, fileMetadata.contentType());
        fileResourceMapper.insert(fileResourceDO);
        log.info("文件资源入库完成，fileResourceId={}，fileHash={}", fileResourceDO.getId(), fileResourceDO.getFileHash());
        return fileResourceDO;
    }

    private KnowledgeDocumentDO createDocument(KnowledgeDocumentUploadRequest request, FileMetadata fileMetadata,
                                               FileResourceDO fileResourceDO) {
        KnowledgeDocumentDO documentDO = knowledgeDocumentConvert.toDocumentDO(
                request.getKnowledgeBaseId(), request.getDirectoryId(), removeExt(fileMetadata.fileName()), fileResourceDO);
        knowledgeDocumentMapper.insert(documentDO);
        return documentDO;
    }

    private KnowledgeDocumentVersionDO createDocumentVersion(KnowledgeDocumentDO documentDO, FileResourceDO fileResourceDO) {
        KnowledgeDocumentVersionDO versionDO = knowledgeDocumentConvert.toVersionDO(
                documentDO.getId(), 1, fileResourceDO, DEFAULT_CHUNK_CONFIG_SNAPSHOT);
        knowledgeDocumentVersionMapper.insert(versionDO);
        return versionDO;
    }

    private KnowledgeProcessTaskDO createProcessTask(KnowledgeDocumentUploadRequest request, KnowledgeDocumentDO documentDO,
                                                     KnowledgeDocumentVersionDO versionDO) {
        KnowledgeProcessTaskDO taskDO = knowledgeDocumentConvert.toProcessTaskDO(
                generateTaskNo(), request.getKnowledgeBaseId(), request.getDirectoryId(), documentDO.getId(), versionDO.getId());
        knowledgeProcessTaskMapper.insert(taskDO);
        return taskDO;
    }

    private String sendParseChunkMessage(KnowledgeBaseDO knowledgeBaseDO, KnowledgeDirectoryDO directoryDO,
                                         FileResourceDO fileResourceDO, KnowledgeDocumentDO documentDO,
                                         KnowledgeDocumentVersionDO versionDO, KnowledgeProcessTaskDO taskDO) {
        String messageId = "msg_" + UUID.randomUUID().toString().replace("-", "");
        DocumentProcessMessage message = DocumentProcessMessage.builder()
                .messageId(messageId)
                .taskId(taskDO.getId())
                .taskNo(taskDO.getTaskNo())
                .stageCode(taskDO.getStageCode())
                .knowledgeBaseId(knowledgeBaseDO.getId())
                .directoryId(directoryDO.getId())
                .documentId(documentDO.getId())
                .versionId(versionDO.getId())
                .fileResourceId(fileResourceDO.getId())
                .fileName(fileResourceDO.getFileName())
                .fileType(fileResourceDO.getFileExt())
                .bucketName(fileResourceDO.getBucketName())
                .objectKey(fileResourceDO.getObjectKey())
                .chunkConfigSnapshot(versionDO.getChunkConfigSnapshot())
                .callbackUrl(callbackUrl)
                .retryCount(taskDO.getRetryCount())
                .traceId(TraceContext.getTraceId().orElse(null))
                .createdTime(LocalDateTime.now().toString())
                .build();
        documentProcessProducer.sendParseChunkMessage(message);
        return messageId;
    }

    private String extractFileExt(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件扩展名不能为空");
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String removeExt(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String sha256(byte[] fileBytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);
        StringBuilder builder = new StringBuilder();
        for (byte hashByte : hashBytes) {
            builder.append(String.format("%02x", hashByte));
        }
        return builder.toString();
    }

    private String generateTaskNo() {
        return "TASK_" + UUID.randomUUID().toString().replace("-", "");
    }

    private record FileMetadata(String fileName, String fileExt, long fileSize, String fileHash,
                                String contentType, byte[] fileBytes) {
    }
}
