package com.touhouqing.crawllangai.config;

import com.alibaba.cloud.ai.autoconfigure.memory.redis.RedisChatMemoryProperties;
import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedisChatMemoryConfiguration {

    private final RedisChatMemoryProperties redisChatMemoryProperties;

    @Bean
    public RedissonRedisChatMemoryRepository redissonRedisChatMemoryRepository() {
        return RedissonRedisChatMemoryRepository.builder()
                .host(redisChatMemoryProperties.getHost())
                .port(redisChatMemoryProperties.getPort())
                .password(redisChatMemoryProperties.getPassword())
                .timeout(redisChatMemoryProperties.getTimeout())
                .build();
    }
}
