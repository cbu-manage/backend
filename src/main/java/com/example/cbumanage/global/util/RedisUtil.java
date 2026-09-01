package com.example.cbumanage.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public boolean existData(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    /* 키를 1 증가시키고, 최초 생성일 때만 만료 시간을 건다. 반환값은 증가 후 값. */
    public long increaseWithExpire(String key, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Long count = valueOperations.increment(key);
        if (count == null) return 0L;
        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(duration));
        }
        return count;
    }

}