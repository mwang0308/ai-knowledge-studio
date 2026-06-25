package com.aistudio.system.service.impl;

import com.aistudio.foundation.domain.ErrorCode;
import com.aistudio.foundation.domain.PageResult;
import com.aistudio.foundation.exception.BusinessException;
import com.aistudio.system.convert.KnowledgeBaseConvert;
import com.aistudio.system.entity.KnowledgeBaseDO;
import com.aistudio.system.enums.KnowledgeBaseStatusEnum;
import com.aistudio.system.mapper.IKnowledgeBaseMapper;
import com.aistudio.system.mapper.IKnowledgeDirectoryMapper;
import com.aistudio.system.model.request.KnowledgeBaseCreateRequest;
import com.aistudio.system.model.request.KnowledgeBasePageRequest;
import com.aistudio.system.model.request.KnowledgeBaseUpdateRequest;
import com.aistudio.system.model.response.KnowledgeBaseResponse;
import com.aistudio.system.query.KnowledgeBasePageQuery;
import com.aistudio.system.service.IKnowledgeBaseService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库服务实现，负责知识库创建、编辑、查询和启停状态维护。
 */
@Slf4j
@Service
public class KnowledgeBaseServiceImpl implements IKnowledgeBaseService {

    @Resource
    private IKnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private IKnowledgeDirectoryMapper knowledgeDirectoryMapper;

    @Resource
    private KnowledgeBaseConvert knowledgeBaseConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createKnowledgeBase(KnowledgeBaseCreateRequest request) {
        log.info("创建知识库开始，name={}", request.getName());
        checkNameUnique(request.getName(), null);

        KnowledgeBaseDO knowledgeBaseDO = knowledgeBaseConvert.toCreateDO(request);
        knowledgeBaseMapper.insert(knowledgeBaseDO);
        log.info("创建知识库完成，id={}，name={}", knowledgeBaseDO.getId(), knowledgeBaseDO.getName());
        return knowledgeBaseDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeBase(KnowledgeBaseUpdateRequest request) {
        log.info("更新知识库开始，id={}，name={}", request.getId(), request.getName());
        KnowledgeBaseDO existing = getExistingKnowledgeBase(request.getId());
        checkNameUnique(request.getName(), request.getId());

        KnowledgeBaseDO updateDO = knowledgeBaseConvert.toUpdateDO(request);
        knowledgeBaseMapper.updateById(updateDO);
        log.info("更新知识库完成，id={}，oldName={}，newName={}", request.getId(), existing.getName(), request.getName());
    }

    @Override
    public KnowledgeBaseResponse getKnowledgeBase(Long id) {
        log.info("查询知识库详情，id={}", id);
        KnowledgeBaseDO knowledgeBaseDO = getExistingKnowledgeBase(id);
        return knowledgeBaseConvert.toResponse(knowledgeBaseDO);
    }

    @Override
    public PageResult<KnowledgeBaseResponse> pageKnowledgeBase(KnowledgeBasePageRequest request) {
        log.info("分页查询知识库开始，pageNo={}，pageSize={}，name={}，status={}，createStartTime={}，createEndTime={}",
                request.getPageNo(), request.getPageSize(), request.getName(), request.getStatus(),
                request.getCreateStartTime(), request.getCreateEndTime());

        if (request.getStatus() != null && !KnowledgeBaseStatusEnum.contains(request.getStatus())) {
            log.warn("分页查询知识库失败，非法状态，status={}", request.getStatus());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "知识库状态不合法");
        }

        // Request 属于接口层对象，进入数据库查询前必须转换为 Query。
        KnowledgeBasePageQuery query = knowledgeBaseConvert.toPageQuery(request);
        Page<KnowledgeBaseDO> page = Page.of(query.getPageNo(), query.getPageSize());
        IPage<KnowledgeBaseDO> doPage = knowledgeBaseMapper.selectKnowledgeBasePage(page, query);

        // Mapper 返回 DO，返回前统一转换为 Response，避免持久化对象泄露到接口层。
        List<KnowledgeBaseResponse> records = doPage.getRecords().stream()
                .map(knowledgeBaseConvert::toResponse)
                .toList();
        log.info("分页查询知识库完成，total={}", doPage.getTotal());
        return new PageResult<>(doPage.getTotal(), query.getPageNo(), query.getPageSize(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableKnowledgeBase(Long id) {
        log.info("启用知识库开始，id={}", id);
        updateStatus(id, KnowledgeBaseStatusEnum.ENABLED);
        log.info("启用知识库完成，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableKnowledgeBase(Long id) {
        log.info("停用知识库开始，id={}", id);
        updateStatus(id, KnowledgeBaseStatusEnum.DISABLED);
        log.info("停用知识库完成，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(Long id) {
        log.info("删除知识库开始，id={}", id);
        getExistingKnowledgeBase(id);

        // 知识库下已有目录时不允许删除，避免产生目录、文档等孤儿数据。
        Integer directoryCount = knowledgeDirectoryMapper.countByKnowledgeBaseId(id);
        if (directoryCount != null && directoryCount > 0) {
            log.warn("删除知识库失败，知识库下存在目录，id={}，directoryCount={}", id, directoryCount);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "知识库下存在目录，不能删除");
        }

        knowledgeBaseMapper.softDeleteById(id);
        log.info("删除知识库完成，id={}", id);
    }

    private void updateStatus(Long id, KnowledgeBaseStatusEnum statusEnum) {
        KnowledgeBaseDO existing = getExistingKnowledgeBase(id);
        if (statusEnum.getCode().equals(existing.getStatus())) {
            log.info("知识库状态无需变更，id={}，status={}", id, statusEnum.getCode());
            return;
        }

        KnowledgeBaseDO updateDO = KnowledgeBaseDO.builder()
                .id(id)
                .status(statusEnum.getCode())
                .build();
        knowledgeBaseMapper.updateById(updateDO);
    }

    private KnowledgeBaseDO getExistingKnowledgeBase(Long id) {
        KnowledgeBaseDO knowledgeBaseDO = knowledgeBaseMapper.selectKnowledgeBaseById(id);
        if (knowledgeBaseDO == null) {
            log.warn("知识库不存在或已删除，id={}", id);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "知识库不存在或已删除");
        }
        return knowledgeBaseDO;
    }

    private void checkNameUnique(String name, Long excludeId) {
        Integer count = knowledgeBaseMapper.countByName(name, excludeId);
        if (count != null && count > 0) {
            log.warn("知识库名称重复，name={}，excludeId={}", name, excludeId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "知识库名称已存在");
        }
    }
}
