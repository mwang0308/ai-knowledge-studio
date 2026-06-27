package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 知识文档结构响应对象。
 */
@Data
@Builder
public class KnowledgeDocumentStructureResponse {

    private String documentId;
    private String versionId;
    private List<DirectoryNode> directoryTree;
    private List<ParseBlock> parseBlocks;

    @Data
    @Builder
    public static class DirectoryNode {
        private String sectionId;
        private String parentSectionId;
        private String title;
        private String titlePath;
        private Integer level;
        private Integer pageStart;
        private Integer pageEnd;
        private Integer blockCount;
        private List<DirectoryNode> children;
    }

    @Data
    @Builder
    public static class ParseBlock {
        private String parseBlockId;
        private String parseBlockName;
        private Integer pageStart;
        private Integer pageEnd;
        private List<String> sectionIds;
        private List<String> sectionTitles;
        private String textPreview;
    }
}
