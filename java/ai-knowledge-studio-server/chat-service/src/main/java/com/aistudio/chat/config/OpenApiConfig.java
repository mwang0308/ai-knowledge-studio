package com.aistudio.chat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * chat-service 接口文档配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chatOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ai-knowledge-studio chat-service")
                        .description("聊天服务接口")
                        .version("0.1.0"));
    }
}
