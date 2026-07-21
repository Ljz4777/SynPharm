package com.synpharm.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatisPlus自动填充配置
 * 
 * <p>配置MyBatisPlus的元对象处理器，实现自动填充创建时间和更新时间字段。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     * 
     * <p>在执行INSERT语句时，自动设置createdAt和updatedAt字段为当前时间。
     * 
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新操作时自动填充
     * 
     * <p>在执行UPDATE语句时，自动更新updatedAt字段为当前时间。
     * 
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}