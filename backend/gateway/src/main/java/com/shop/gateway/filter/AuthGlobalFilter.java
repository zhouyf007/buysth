package com.shop.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.gateway.security.GatewayJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> ADMIN_ROLES = List.of("SUPER_ADMIN", "OPERATOR");

    private final GatewayJwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        String authorization = request.getHeaders().getFirst("Authorization");

        boolean publicPath = isPublicPath(path, method);
        if (publicPath && (authorization == null || authorization.isBlank())) {
            return chain.filter(exchange);
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, 401, "请先登录");
        }

        String token = authorization.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, 401, "登录状态无效或已过期");
        }

        String whitelistKey = "auth:token:" + claims.getId();
        Boolean exists = redisTemplate.hasKey(whitelistKey);
        if (!Boolean.TRUE.equals(exists)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, 401, "登录状态已失效");
        }

        if (path.startsWith("/api/admin/")) {
            List<String> roles = jwtUtil.getRoles(claims);
            if (roles.stream().noneMatch(ADMIN_ROLES::contains)) {
                return writeError(exchange, HttpStatus.FORBIDDEN, 403, "无权限访问");
            }
        }

        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-Username", String.valueOf(claims.get("username")))
                .header("X-Roles", String.join(",", jwtUtil.getRoles(claims)))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublicPath(String path, HttpMethod method) {
        if (method == HttpMethod.POST && path.startsWith("/api/auth/login")) {
            return true;
        }
        if (method == HttpMethod.POST && path.startsWith("/api/auth/register")) {
            return true;
        }
        if (method == HttpMethod.POST && path.startsWith("/api/auth/refresh")) {
            return true;
        }
        if (method == HttpMethod.GET && path.startsWith("/api/product/")) {
            return true;
        }
        if (method == HttpMethod.GET && path.startsWith("/api/seckill/activities")) {
            return true;
        }
        if (method == HttpMethod.GET && path.startsWith("/api/notify/announcements")) {
            return true;
        }
        if (method == HttpMethod.GET && path.startsWith("/api/pay/mock/")) {
            return true;
        }
        if (method == HttpMethod.POST && path.startsWith("/api/pay/mock/notify/")) {
            return true;
        }
        return false;
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "code", code,
                    "message", message,
                    "data", (Object) null));
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
