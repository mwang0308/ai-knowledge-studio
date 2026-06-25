package com.aistudio.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件资源持久化对象，对应 file_resource 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_resource")
public class FileResourceDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String fileExt;

    private Long fileSize;

    private String fileHash;

    private String bucketName;

    private String objectKey;

    private String contentType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private Integer deleted;
}
