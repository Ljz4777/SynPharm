package com.synpharm.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量任务消息体。
 *
 * <p>消息只携带投递触发信息（batchId + algoType），
 * 任务明细以 batch_task 表（DB）为权威，消息小且可靠。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskMessage {

    /** 批次ID */
    private String batchId;

    /** 算法类型 DTI/PPI/DDI */
    private String algoType;
}
