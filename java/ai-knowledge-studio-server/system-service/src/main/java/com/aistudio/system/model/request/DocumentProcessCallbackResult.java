package com.aistudio.system.model.request;

import lombok.Data;

/**
 * Python 文档处理成功回调的产物摘要。
 */
@Data
public class DocumentProcessCallbackResult {

    private String sectionsObjectKey;

    private String structureObjectKey;

    private String chunksObjectKey;

    private Integer chunkCount;

    private Integer tokenCount;

    private String parserName;

    private String embeddingModelCode;
}
