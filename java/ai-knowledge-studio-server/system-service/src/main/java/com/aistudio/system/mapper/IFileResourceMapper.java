package com.aistudio.system.mapper;

import com.aistudio.system.entity.FileResourceDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文件资源 Mapper，只负责数据库访问，不接收 Request，不返回 Response。
 */
public interface IFileResourceMapper extends BaseMapper<FileResourceDO> {

    /**
     * 根据文件 hash 查询未删除文件资源。
     *
     * @param fileHash 文件 SHA-256
     * @return 文件资源持久化对象
     */
    FileResourceDO selectByFileHash(@Param("fileHash") String fileHash);

    int countActiveReference(@Param("fileResourceId") Long fileResourceId, @Param("excludeDocumentId") Long excludeDocumentId);

    /**
     * 物理删除已确认无引用的文件资源元数据。
     *
     * @param fileResourceId 文件资源 ID
     * @return 删除行数
     */
    int deleteIfUnreferenced(@Param("fileResourceId") Long fileResourceId);
}
