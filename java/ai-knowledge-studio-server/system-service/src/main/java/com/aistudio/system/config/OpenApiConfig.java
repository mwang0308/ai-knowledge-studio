package com.aistudio.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system-service 接口文档配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI systemOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ai-knowledge-studio system-service")
                        .description("后台系统与知识库治理服务接口")
                        .version("0.1.0"));
    }
}
