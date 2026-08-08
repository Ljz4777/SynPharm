package com.synpharm.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 批量任务消息生产者。
 *
 * <p>上传落库后调用，向 synpharm.exchange / batch.task 发送消息，
 * 由 BatchTaskConsumer 异步消费执行。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTaskProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送批量任务消息（RabbitTemplate 默认持久化消息）
     *
     * @param batchId  批次ID
     * @param algoType 算法类型 DTI/PPI/DDI
     */
    public void sendBatchTask(String batchId, String algoType) {
        BatchTaskMessage message = BatchTaskMessage.builder()
                .batchId(batchId)
                .algoType(algoType)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitConfig.BATCH_EXCHANGE,
                RabbitConfig.BATCH_ROUTING_KEY,
                message
        );
        log.info("批量任务消息已发送: batchId={}, algoType={}", batchId, algoType);
    }
}
