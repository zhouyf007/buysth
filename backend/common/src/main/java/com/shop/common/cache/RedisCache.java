package com.shop.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCache {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void set(String key, Object value) {
        set(key, value, 0);
    }

    public void set(String key, Object value, long seconds) {
        try {
            String json = value instanceof String ? (String) value : objectMapper.writeValueAsString(value);
            if (seconds > 0) {
                redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(seconds));
            } else {
                redisTemplate.opsForValue().set(key, json);
            }
        } catch (Exception e) {
            log.warn("Redis set failed, key={}", key, e);
        }
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        String json = get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Redis read failed, key={}", key, e);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> type) {
        String json = get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Redis read failed, key={}", key, e);
            return null;
        }
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public boolean setIfAbsent(String key, String value, long seconds) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(seconds)));
    }

    public boolean tryLock(String key, String value, long seconds) {
        return setIfAbsent(key, value, seconds);
    }

    public void releaseLock(String key, String value) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
                Long.class);
        redisTemplate.execute(script, List.of(key), value);
    }

    public Object execute(DefaultRedisScript<?> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }

    public void expire(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }
}
