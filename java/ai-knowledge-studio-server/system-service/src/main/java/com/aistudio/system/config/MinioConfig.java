package com.aistudio.system.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置。
 */
@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(@Value("${storage.minio.endpoint}") String endpoint,
                                   @Value("${storage.minio.access-key}") String accessKey,
                                   @Value("${storage.minio.secret-key}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
