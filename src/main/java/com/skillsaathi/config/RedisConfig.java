package com.skillsaathi.config;

import com.skillsaathi.websocket.ChatEventPublisher;
import com.skillsaathi.websocket.ChatEventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Bean
    public ChannelTopic chatEventsTopic() {
        return new ChannelTopic(ChatEventPublisher.CHANNEL);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ChatEventSubscriber chatEventSubscriber,
            ChannelTopic chatEventsTopic) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(chatEventSubscriber, chatEventsTopic);
        return container;
    }
}
