package com.synpharm.mq;

import com.rabbitmq.client.Channel;
import com.synpharm.service.BatchProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 批量任务消息消费者。
 *
 * <p>监听 batch.task.queue，手动 ack：
 * <ul>
 *   <li>处理成功 → basicAck</li>
 *   <li>处理失败 → basicNack(requeue=false)，消息进入死信队列 batch.task.dlq</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTaskConsumer {

    private final BatchProcessService batchProcessService;

    @RabbitListener(queues = RabbitConfig.BATCH_QUEUE, ackMode = "MANUAL")
    public void onBatchTask(BatchTaskMessage message, Channel channel, Message amqpMessage) throws IOException {
        String batchId = message.getBatchId();
        String algoType = message.getAlgoType();
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        log.info("消费批量任务消息: batchId={}, algoType={}", batchId, algoType);
        try {
            batchProcessService.processBatch(batchId, algoType);
            channel.basicAck(deliveryTag, false);
            log.info("批量任务处理成功并确认: batchId={}", batchId);
        } catch (Exception e) {
            log.error("批量任务处理失败, 进入死信队列: batchId={}, error={}", batchId, e.getMessage());
            // 不 requeue，进死信队列（由补偿/人工处理）
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
