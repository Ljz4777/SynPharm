package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 批量任务明细实体（修复方案 5.6）。
 *
 * <p>映射数据库表 batch_task_item，保存批量 CSV 每一行的
 * 输入、处理状态、错误信息与关联结果 ID。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@TableName("batch_task_item")
public class BatchTaskItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("batch_id")
    private String batchId;

    /**
     * CSV 行号（从 1 开始）。
     *
     * <p>列名必须带反引号：{@code ROW_NUMBER} 是 MySQL 8.0 的保留字
     * （窗口函数）。建表脚本里已经加了反引号，但 MyBatis-Plus 是按本注解原样拼 SQL 的，
     * 少了反引号会生成 {@code INSERT ... ( batch_id, row_number, ... )}，
     * 直接报语法错误，导致批量上传 100% 失败。
     */
    @TableField("`row_number`")
    private Integer rowNumber;

    @TableField("input_value")
    private String inputValue;

    @TableField("status")
    private Integer status;

    @TableField("result_id")
    private Long resultId;

    @TableField("error_code")
    private String errorCode;

    @TableField("error_message")
    private String errorMessage;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
