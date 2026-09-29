package com.synpharm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring 上下文冒烟测试（H-05 务实版）
 *
 * <p>用 H2 内存库（MySQL 模式）+ 禁用 RabbitMQ 监听 + mock 外部依赖，
 * 验证整个 Spring 容器（安全链、MyBatis-Plus、Redis 配置、FastAPI 客户端装配）能正常启动。
 *
 * <p>注意：不覆盖端到端业务链路（仍依赖真实 MySQL/Redis/RabbitMQ），
 * 但能兜住"改配置后容器起不来"这类回归。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:smoke;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "jwt.secret=Smoke-Test-Only-Secret-Key-At-Least-32-Bytes-Long-123",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "app.doc.open=true"
})
@ActiveProfiles("smoke")
class SynPharmApplicationSmokeTest {

    @Test
    void contextLoads() {
        // 容器能启动即通过
    }
}
