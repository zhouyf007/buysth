package com.shop.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI shopOpenApi() {
        return new OpenAPI().info(new Info()
                .title("数码商城接口文档")
                .version("1.0.0")
                .description("数码产品网上购物系统微服务接口"));
    }
}

