package com.aistudio.system.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.minio.messages.Item;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * 文档对象存储服务，负责把原始文件保存到 MinIO。
 */
@Slf4j
@Component
public class DocumentStorageService {

    @Resource
    private MinioClient minioClient;

    @Value("${storage.minio.bucket-name}")
    private String bucketName;

    /**
     * 保存原始文件。调用方传入字节数组，避免 multipart 输入流被 hash 计算提前消费。
     */
    public String saveOriginalFile(byte[] fileBytes, String fileHash, String fileName, String contentType) {
        String objectKey = "original/" + fileHash + "/" + fileName;
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .contentType(contentType)
                    .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                    .build());
            log.info("原始文件已上传 MinIO，bucketName={}，objectKey={}，size={}", bucketName, objectKey, fileBytes.length);
            return objectKey;
        } catch (Exception exception) {
            log.error("上传原始文件到 MinIO 失败，bucketName={}，objectKey={}", bucketName, objectKey, exception);
            throw new IllegalStateException("上传原始文件失败", exception);
        }
    }

    public String getBucketName() {
        return bucketName;
    }

    public void deleteObjectIfExists(String bucketName, String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
            log.info("MinIO 对象已删除，bucketName={}，objectKey={}", bucketName, objectKey);
        } catch (Exception exception) {
            log.error("删除 MinIO 对象失败，bucketName={}，objectKey={}", bucketName, objectKey, exception);
            throw new IllegalStateException("删除对象存储文件失败", exception);
        }
    }

    public void deleteObjectsByPrefix(String bucketName, String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .recursive(true)
                    .build());
            int deletedCount = 0;
            for (Result<Item> result : results) {
                Item item = result.get();
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(item.objectName())
                        .build());
                deletedCount++;
            }
            log.info("MinIO 前缀对象清理完成，bucketName={}，prefix={}，deletedCount={}", bucketName, prefix, deletedCount);
        } catch (Exception exception) {
            log.error("按前缀删除 MinIO 对象失败，bucketName={}，prefix={}", bucketName, prefix, exception);
            throw new IllegalStateException("删除处理产物失败", exception);
        }
    }

    /**
     * 读取文本产物。仅用于读取 Python 归档的结构和分片产物，不读取原文大内容到日志。
     */
    public String readTextObject(String bucketName, String objectKey) {
        try (var inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build())) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            log.error("读取 MinIO 文本产物失败，bucketName={}，objectKey={}", bucketName, objectKey, exception);
            throw new IllegalStateException("读取处理产物失败", exception);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("MinIO bucket 不存在，已自动创建，bucketName={}", bucketName);
        }
    }
}
