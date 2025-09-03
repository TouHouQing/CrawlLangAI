package com.touhouqing.crawllangai.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.touhouqing.crawllangai.tool.AnnounceDetailTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class ChatClientConfiguration {

    @Bean
    public ChatClient aiClient(DashScopeChatModel chatModel, ChatMemory redisChatMemory, AnnounceDetailTool announceDetailTool) {
        return ChatClient.builder(chatModel)
                .defaultSystem("用户会给你提供一个markdown文本，你需要提取公告的发布日期，公告标题，项目编号，发布来源，预算总金额单位万元，所有采购需求描述等信息保存到数据库中,不需要回答任何内容,只允许保存一次")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build()
                )
                .defaultTools(announceDetailTool)
                .build();

    }

}
