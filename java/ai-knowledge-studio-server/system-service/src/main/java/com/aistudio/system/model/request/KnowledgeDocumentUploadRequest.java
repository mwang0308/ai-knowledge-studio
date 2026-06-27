package com.aistudio.system.model.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档上传请求对象，只用于接口入参和 Service 编排，不进入 Mapper。
 */
@Data
@Builder
public class KnowledgeDocumentUploadRequest {

    private Long knowledgeBaseId;

    private Long directoryId;

    /** PDF 解析器类型：docling、mineru、pdf；非 PDF 文件忽略。 */
    private String parserType;

    private MultipartFile file;
}
