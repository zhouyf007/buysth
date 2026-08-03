package com.shop.api.config;

import com.shop.common.context.HeaderNames;
import com.shop.common.context.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor userHeaderInterceptor() {
        return template -> {
            if (UserContext.getUserId() != null) {
                template.header(HeaderNames.USER_ID, String.valueOf(UserContext.getUserId()));
                template.header(HeaderNames.USERNAME, UserContext.getUsername() == null ? "" : UserContext.getUsername());
                template.header(HeaderNames.ROLES, String.join(",", UserContext.getRoles()));
            }
        };
    }
}
