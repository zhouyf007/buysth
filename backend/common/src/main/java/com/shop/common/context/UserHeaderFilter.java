package com.shop.common.context;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UserHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String userId = httpRequest.getHeader(HeaderNames.USER_ID);
        String username = httpRequest.getHeader(HeaderNames.USERNAME);
        String roles = httpRequest.getHeader(HeaderNames.ROLES);
        try {
            if (userId != null && !userId.isBlank()) {
                List<String> roleList = roles == null ? List.of() :
                        Arrays.stream(roles.split(",")).filter(s -> !s.isBlank()).collect(Collectors.toList());
                UserContext.set(Long.valueOf(userId), username, roleList);
            }
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}

