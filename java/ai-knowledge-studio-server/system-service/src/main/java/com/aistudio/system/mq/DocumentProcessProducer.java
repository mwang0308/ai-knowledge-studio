package com.aistudio.system.mq;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 文档处理任务生产者，负责把 Java 创建的处理任务投递到 RabbitMQ。
 */
@Slf4j
@Component
public class DocumentProcessProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${knowledge.mq.document-process-exchange}")
    private String exchange;

    @Value("${knowledge.mq.document-parse-chunk-routing-key}")
    private String parseChunkRoutingKey;

    /**
     * 发送文档解析分片任务。
     */
    public void sendParseChunkMessage(DocumentProcessMessage message) {
        log.info("发送文档解析分片 MQ 开始，messageId={}，taskId={}，documentId={}，versionId={}",
                message.getMessageId(), message.getTaskId(), message.getDocumentId(), message.getVersionId());
        rabbitTemplate.convertAndSend(exchange, parseChunkRoutingKey, message, rabbitMessage -> {
            rabbitMessage.getMessageProperties().setMessageId(message.getMessageId());
            rabbitMessage.getMessageProperties().setContentType("application/json");
            return rabbitMessage;
        });
        log.info("发送文档解析分片 MQ 完成，messageId={}，exchange={}，routingKey={}",
                message.getMessageId(), exchange, parseChunkRoutingKey);
    }
}
