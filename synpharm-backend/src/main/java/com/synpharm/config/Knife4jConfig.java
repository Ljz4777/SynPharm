package com.synpharm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 * 
 * <p>配置OpenAPI文档信息，集成Knife4j增强Swagger UI功能。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置OpenAPI文档信息
     * 
     * <p>设置API文档的标题、描述、版本和联系信息。
     * 
     * @return OpenAPI OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SynPharm API文档")
                        .description("SynPharm AI药物发现平台接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SynPharm Team")
                                .email("support@synpharm.com")));
    }
}