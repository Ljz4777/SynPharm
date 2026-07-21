package com.synpharm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智互药研后端应用启动类
 * 
 * <p>Spring Boot应用的入口类，负责启动整个后端服务。
 * 使用MyBatisPlus的Mapper扫描注解扫描Repository包。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.synpharm.repository.mapper")
public class SynPharmApplication {

    /**
     * 应用主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SynPharmApplication.class, args);
    }
}