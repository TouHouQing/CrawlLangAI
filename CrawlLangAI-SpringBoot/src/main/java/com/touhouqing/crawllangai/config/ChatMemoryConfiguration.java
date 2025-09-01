package com.touhouqing.crawllangai.config;

import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ChatMemoryConfiguration {

    @Bean
    public ChatMemory redisChatMemory(RedissonRedisChatMemoryRepository redissonRedisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redissonRedisChatMemoryRepository)
                .maxMessages(10)
                .build();
    }
}
