package com.aistudio.system.service.impl;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.system.convert.DashboardConvert;
import com.aistudio.system.entity.KnowledgeDocumentDO;
import com.aistudio.system.enums.DocumentParseStatusEnum;
import com.aistudio.system.enums.DocumentPublishStatusEnum;
import com.aistudio.system.enums.DocumentReviewStatusEnum;
import com.aistudio.system.mapper.IKnowledgeBaseMapper;
import com.aistudio.system.mapper.IKnowledgeDocumentMapper;
import com.aistudio.system.model.request.DashboardTodoPageRequest;
import com.aistudio.system.model.response.DashboardSummaryResponse;
import com.aistudio.system.model.response.DashboardTodoResponse;
import com.aistudio.system.query.DashboardTodoPageQuery;
import com.aistudio.system.service.IDashboardService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工作台统计服务实现，负责汇总知识治理待办与核心指标。
 */
@Slf4j
@Service
public class DashboardServiceImpl implements IDashboardService {

    @Resource
    private IKnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private IKnowledgeDocumentMapper knowledgeDocumentMapper;

    @Resource
    private DashboardConvert dashboardConvert;

    @Override
    public DashboardSummaryResponse getSummary() {
        log.info("查询知识治理工作台统计");
        List<DashboardTodoResponse> todos = knowledgeDocumentMapper.selectDashboardTodos(8).stream()
                .map(this::toTodo)
                .toList();
        return DashboardSummaryResponse.builder()
                .knowledgeBaseCount(knowledgeBaseMapper.countEnabledDashboardBase())
                .documentCount(knowledgeDocumentMapper.countByDashboardStatus(null, null, null))
                .waitAuditCount(knowledgeDocumentMapper.countByDashboardStatus(null, DocumentReviewStatusEnum.WAIT_AUDIT.getCode(), null))
                .processFailedCount(knowledgeDocumentMapper.countByDashboardStatus(DocumentParseStatusEnum.PROCESS_FAILED.getCode(), null, null))
                .publishedCount(knowledgeDocumentMapper.countByDashboardStatus(null, null, DocumentPublishStatusEnum.PUBLISHED.getCode()))
                .chunkCount(knowledgeDocumentMapper.sumDashboardChunkCount())
                .todos(todos)
                .build();
    }

    @Override
    public PageResult<DashboardTodoResponse> pageTodos(DashboardTodoPageRequest request) {
        log.info("分页查询工作台待办列表, pageNo={}, pageSize={}", request.getPageNo(), request.getPageSize());
        DashboardTodoPageQuery query = dashboardConvert.toTodoPageQuery(request);
        Page<KnowledgeDocumentDO> page = Page.of(query.getPageNo(), query.getPageSize());
        IPage<KnowledgeDocumentDO> todoPage = knowledgeDocumentMapper.selectDashboardTodoPage(page, query);
        return new PageResult<>(
                todoPage.getTotal(),
                todoPage.getCurrent(),
                todoPage.getSize(),
                todoPage.getRecords().stream().map(this::toTodo).toList()
        );
    }

    private DashboardTodoResponse toTodo(KnowledgeDocumentDO documentDO) {
        String nextAction = "查看处理";
        String actionPath = "/document";
        if (DocumentParseStatusEnum.PROCESS_FAILED.getCode().equals(documentDO.getParseStatus())) {
            nextAction = "查看失败";
            actionPath = "/document";
        } else if (DocumentReviewStatusEnum.WAIT_AUDIT.getCode().equals(documentDO.getReviewStatus())) {
            nextAction = "审核发布";
            actionPath = "/publish";
        } else if (DocumentReviewStatusEnum.AUDIT_PASSED.getCode().equals(documentDO.getReviewStatus())) {
            nextAction = "发布上线";
            actionPath = "/publish";
        } else if (DocumentParseStatusEnum.PARSE_CHUNKED.getCode().equals(documentDO.getParseStatus())) {
            nextAction = "召回测试";
            actionPath = "/retrieval";
        }
        return DashboardTodoResponse.builder()
                .documentId(documentDO.getDocumentUid())
                .documentName(documentDO.getName())
                .knowledgeBaseId(documentDO.getKnowledgeBaseId())
                .directoryId(documentDO.getDirectoryId())
                .currentStatus(statusText(documentDO.getParseStatus(), documentDO.getReviewStatus(), documentDO.getPublishStatus()))
                .parseStatus(parseStatusText(documentDO.getParseStatus()))
                .reviewStatus(reviewStatusText(documentDO.getReviewStatus()))
                .publishStatus(publishStatusText(documentDO.getPublishStatus()))
                .nextAction(nextAction)
                .actionPath(actionPath)
                .createTime(documentDO.getCreateTime())
                .updateTime(documentDO.getUpdateTime())
                .build();
    }

    private String statusText(String parseStatus, String reviewStatus, String publishStatus) {
        return parseStatusText(parseStatus) + " / " + reviewStatusText(reviewStatus) + " / " + publishStatusText(publishStatus);
    }

    private String parseStatusText(String status) {
        for (DocumentParseStatusEnum item : DocumentParseStatusEnum.values()) {
            if (item.getCode().equals(status)) {
                return item.getDescription();
            }
        }
        return status;
    }

    private String reviewStatusText(String status) {
        if (DocumentReviewStatusEnum.NOT_SUBMITTED.getCode().equals(status)) {
            return "未提交";
        }
        if (DocumentReviewStatusEnum.WAIT_AUDIT.getCode().equals(status)) {
            return "待审核";
        }
        if (DocumentReviewStatusEnum.AUDIT_PASSED.getCode().equals(status)) {
            return "审核通过";
        }
        if (DocumentReviewStatusEnum.AUDIT_REJECTED.getCode().equals(status)) {
            return "审核驳回";
        }
        return status;
    }

    private String publishStatusText(String status) {
        if (DocumentPublishStatusEnum.UNPUBLISHED.getCode().equals(status)) {
            return "未发布";
        }
        if (DocumentPublishStatusEnum.PUBLISHING.getCode().equals(status)) {
            return "发布中";
        }
        if (DocumentPublishStatusEnum.PUBLISHED.getCode().equals(status)) {
            return "已发布";
        }
        if (DocumentPublishStatusEnum.OFFLINE.getCode().equals(status)) {
            return "已下线";
        }
        return status;
    }
}
