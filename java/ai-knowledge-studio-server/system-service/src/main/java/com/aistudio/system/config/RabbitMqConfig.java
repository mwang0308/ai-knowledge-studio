package com.aistudio.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置，声明文档处理任务交换机和队列。
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange documentProcessExchange(@Value("${knowledge.mq.document-process-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue documentParseChunkQueue(@Value("${knowledge.mq.document-parse-chunk-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding documentParseChunkBinding(Queue documentParseChunkQueue,
                                             DirectExchange documentProcessExchange,
                                             @Value("${knowledge.mq.document-parse-chunk-routing-key}") String routingKey) {
        return BindingBuilder.bind(documentParseChunkQueue).to(documentProcessExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
