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
}
