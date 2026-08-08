package com.synpharm.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置。
 *
 * <p>统一交换机 synpharm.exchange（direct, durable），
 * 业务队列 batch.task.queue 声明死信（x-dead-letter-*），
 * 处理失败的消息自动进入 batch.task.dlq。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    public static final String BATCH_EXCHANGE = "synpharm.exchange";
    public static final String BATCH_QUEUE = "batch.task.queue";
    public static final String BATCH_DLQ = "batch.task.dlq";
    public static final String BATCH_ROUTING_KEY = "batch.task";
    public static final String BATCH_DLQ_ROUTING_KEY = "batch.task.dlq";

    @Bean
    public DirectExchange synpharmExchange() {
        return new DirectExchange(BATCH_EXCHANGE, true, false);
    }

    /**
     * 消息转换器：POJO &lt;-&gt; JSON。
     * <p>使 RabbitTemplate 与 @RabbitListener 均以 JSON 序列化 BatchTaskMessage，
     * 避免默认 SimpleMessageConverter 无法转换 POJO（消息体跨语言友好）。
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue batchQueue() {
        return QueueBuilder.durable(BATCH_QUEUE)
                .deadLetterExchange(BATCH_EXCHANGE)
                .deadLetterRoutingKey(BATCH_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue batchDlq() {
        return QueueBuilder.durable(BATCH_DLQ).build();
    }

    @Bean
    public Binding batchBinding() {
        return BindingBuilder.bind(batchQueue()).to(synpharmExchange()).with(BATCH_ROUTING_KEY);
    }

    @Bean
    public Binding batchDlqBinding() {
        return BindingBuilder.bind(batchDlq()).to(synpharmExchange()).with(BATCH_DLQ_ROUTING_KEY);
    }
}
