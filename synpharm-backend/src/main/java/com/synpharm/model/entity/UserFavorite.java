package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体
 *
 * <p>对应用户收藏表 user_favorite，记录用户对预测结果的收藏。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@TableName("user_favorite")
public class UserFavorite {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("result_id")
    private Long resultId;

    private String note;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
