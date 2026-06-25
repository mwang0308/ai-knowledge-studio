package com.aistudio.system.service.impl;

import com.aistudio.foundation.domain.ErrorCode;
import com.aistudio.foundation.exception.BusinessException;
import com.aistudio.system.convert.KnowledgeDirectoryConvert;
import com.aistudio.system.entity.KnowledgeBaseDO;
import com.aistudio.system.entity.KnowledgeDirectoryDO;
import com.aistudio.system.enums.KnowledgeDirectoryStatusEnum;
import com.aistudio.system.mapper.IKnowledgeBaseMapper;
import com.aistudio.system.mapper.IKnowledgeDirectoryMapper;
import com.aistudio.system.model.request.KnowledgeDirectoryCreateRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryTreeRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryUpdateRequest;
import com.aistudio.system.model.response.KnowledgeDirectoryResponse;
import com.aistudio.system.query.KnowledgeDirectoryTreeQuery;
import com.aistudio.system.service.IKnowledgeDirectoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 知识库目录服务实现，负责目录树维护和目录状态流转。
 */
@Slf4j
@Service
public class KnowledgeDirectoryServiceImpl implements IKnowledgeDirectoryService {

    private static final String PATH_SEPARATOR = " / ";

    @Resource
    private IKnowledgeDirectoryMapper knowledgeDirectoryMapper;

    @Resource
    private IKnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private KnowledgeDirectoryConvert knowledgeDirectoryConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createKnowledgeDirectory(KnowledgeDirectoryCreateRequest request) {
        log.info("创建知识库目录开始，knowledgeBaseId={}，parentId={}，name={}",
                request.getKnowledgeBaseId(), request.getParentId(), request.getName());
        checkKnowledgeBaseExists(request.getKnowledgeBaseId());

        String directoryName = request.getName().trim();
        request.setName(directoryName);
        DirectoryPath pathInfo = buildPathInfo(request.getKnowledgeBaseId(), request.getParentId(), directoryName);
        checkPathUnique(request.getKnowledgeBaseId(), pathInfo.path(), null);

        KnowledgeDirectoryDO directoryDO = knowledgeDirectoryConvert.toCreateDO(request, pathInfo.path(), pathInfo.level());
        knowledgeDirectoryMapper.insert(directoryDO);
        log.info("创建知识库目录完成，id={}，path={}", directoryDO.getId(), directoryDO.getPath());
        return directoryDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeDirectory(KnowledgeDirectoryUpdateRequest request) {
        log.info("更新知识库目录开始，id={}，knowledgeBaseId={}，parentId={}，name={}",
                request.getId(), request.getKnowledgeBaseId(), request.getParentId(), request.getName());
        KnowledgeDirectoryDO existing = getExistingDirectory(request.getId());
        checkKnowledgeBaseExists(request.getKnowledgeBaseId());
        checkDirectoryBelongsToKnowledgeBase(existing, request.getKnowledgeBaseId());
        preventSelfParent(request.getId(), request.getParentId());

        String directoryName = request.getName().trim();
        request.setName(directoryName);
        DirectoryPath pathInfo = buildPathInfo(request.getKnowledgeBaseId(), request.getParentId(), directoryName);
        boolean pathChanged = !Objects.equals(existing.getPath(), pathInfo.path())
                || !Objects.equals(existing.getParentId(), request.getParentId());
        if (pathChanged && countChildren(request.getId()) > 0) {
            log.warn("更新知识库目录失败，存在子目录时不允许修改父级或名称，id={}", request.getId());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "目录存在子目录，不能修改父级或名称");
        }
        checkPathUnique(request.getKnowledgeBaseId(), pathInfo.path(), request.getId());

        KnowledgeDirectoryDO updateDO = knowledgeDirectoryConvert.toUpdateDO(request, pathInfo.path(), pathInfo.level());
        knowledgeDirectoryMapper.updateKnowledgeDirectoryById(updateDO);
        log.info("更新知识库目录完成，id={}，oldPath={}，newPath={}", request.getId(), existing.getPath(), pathInfo.path());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeDirectory(Long id) {
        log.info("删除知识库目录开始，id={}", id);
        getExistingDirectory(id);
        if (countChildren(id) > 0) {
            log.warn("删除知识库目录失败，目录存在子目录，id={}", id);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "目录存在子目录，不能删除");
        }
        knowledgeDirectoryMapper.softDeleteById(id);
        log.info("删除知识库目录完成，id={}", id);
    }

    @Override
    public List<KnowledgeDirectoryResponse> treeKnowledgeDirectory(KnowledgeDirectoryTreeRequest request) {
        log.info("查询知识库目录树开始，knowledgeBaseId={}，description={}，status={}，createStartTime={}，createEndTime={}",
                request.getKnowledgeBaseId(), request.getDescription(), request.getStatus(),
                request.getCreateStartTime(), request.getCreateEndTime());
        if (request.getKnowledgeBaseId() != null) {
            checkKnowledgeBaseExists(request.getKnowledgeBaseId());
        }
        if (request.getStatus() != null && !KnowledgeDirectoryStatusEnum.contains(request.getStatus())) {
            log.warn("查询知识库目录树失败，非法状态，status={}", request.getStatus());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "目录状态不合法");
        }

        // Request 属于接口层对象，进入数据库查询前必须转换为 Query。
        KnowledgeDirectoryTreeQuery query = knowledgeDirectoryConvert.toTreeQuery(request);
        List<KnowledgeDirectoryDO> directoryDOList = knowledgeDirectoryMapper.selectKnowledgeDirectoryTree(query);

        // Mapper 返回 DO，返回前统一转换成 Response，并在 Service 层组装树结构。
        List<KnowledgeDirectoryResponse> tree = buildTree(directoryDOList);
        log.info("查询知识库目录树完成，count={}", directoryDOList.size());
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableKnowledgeDirectory(Long id) {
        log.info("启用知识库目录开始，id={}", id);
        updateStatus(id, KnowledgeDirectoryStatusEnum.ENABLED);
        log.info("启用知识库目录完成，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableKnowledgeDirectory(Long id) {
        log.info("停用知识库目录开始，id={}", id);
        updateStatus(id, KnowledgeDirectoryStatusEnum.DISABLED);
        log.info("停用知识库目录完成，id={}", id);
    }

    private List<KnowledgeDirectoryResponse> buildTree(List<KnowledgeDirectoryDO> directoryDOList) {
        Map<Long, KnowledgeDirectoryResponse> responseMap = new LinkedHashMap<>();
        for (KnowledgeDirectoryDO directoryDO : directoryDOList) {
            responseMap.put(directoryDO.getId(), knowledgeDirectoryConvert.toResponse(directoryDO));
        }

        List<KnowledgeDirectoryResponse> roots = new ArrayList<>();
        for (KnowledgeDirectoryResponse response : responseMap.values()) {
            if (response.getParentId() == null || !responseMap.containsKey(response.getParentId())) {
                roots.add(response);
                continue;
            }
            responseMap.get(response.getParentId()).getChildren().add(response);
        }
        return roots;
    }

    private void updateStatus(Long id, KnowledgeDirectoryStatusEnum statusEnum) {
        KnowledgeDirectoryDO existing = getExistingDirectory(id);
        if (statusEnum.getCode().equals(existing.getStatus())) {
            log.info("知识库目录状态无需变更，id={}，status={}", id, statusEnum.getCode());
            return;
        }

        KnowledgeDirectoryDO updateDO = KnowledgeDirectoryDO.builder()
                .id(id)
                .status(statusEnum.getCode())
                .build();
        knowledgeDirectoryMapper.updateById(updateDO);
    }

    private DirectoryPath buildPathInfo(Long knowledgeBaseId, Long parentId, String name) {
        if (parentId == null) {
            return new DirectoryPath(name, 1);
        }

        KnowledgeDirectoryDO parent = getExistingDirectory(parentId);
        checkDirectoryBelongsToKnowledgeBase(parent, knowledgeBaseId);
        return new DirectoryPath(parent.getPath() + PATH_SEPARATOR + name, parent.getLevel() + 1);
    }

    private void checkKnowledgeBaseExists(Long knowledgeBaseId) {
        KnowledgeBaseDO knowledgeBaseDO = knowledgeBaseMapper.selectKnowledgeBaseById(knowledgeBaseId);
        if (knowledgeBaseDO == null) {
            log.warn("知识库不存在或已删除，knowledgeBaseId={}", knowledgeBaseId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "知识库不存在或已删除");
        }
    }

    private KnowledgeDirectoryDO getExistingDirectory(Long id) {
        KnowledgeDirectoryDO directoryDO = knowledgeDirectoryMapper.selectKnowledgeDirectoryById(id);
        if (directoryDO == null) {
            log.warn("知识库目录不存在或已删除，id={}", id);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "目录不存在或已删除");
        }
        return directoryDO;
    }

    private void checkDirectoryBelongsToKnowledgeBase(KnowledgeDirectoryDO directoryDO, Long knowledgeBaseId) {
        if (!Objects.equals(directoryDO.getKnowledgeBaseId(), knowledgeBaseId)) {
            log.warn("目录不属于当前知识库，directoryId={}，directoryBaseId={}，requestBaseId={}",
                    directoryDO.getId(), directoryDO.getKnowledgeBaseId(), knowledgeBaseId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "目录不属于当前知识库");
        }
    }

    private void preventSelfParent(Long id, Long parentId) {
        if (Objects.equals(id, parentId)) {
            log.warn("目录不能挂到自身下面，id={}", id);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "父目录不能是当前目录");
        }
    }

    private void checkPathUnique(Long knowledgeBaseId, String path, Long excludeId) {
        Integer count = knowledgeDirectoryMapper.countByPath(knowledgeBaseId, path, excludeId);
        if (count != null && count > 0) {
            log.warn("知识库目录路径重复，knowledgeBaseId={}，path={}，excludeId={}", knowledgeBaseId, path, excludeId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "目录路径已存在");
        }
    }

    private int countChildren(Long parentId) {
        Integer count = knowledgeDirectoryMapper.countChildren(parentId);
        return count == null ? 0 : count;
    }

    private record DirectoryPath(String path, Integer level) {
    }
}
