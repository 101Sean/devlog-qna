package com.example.devlogqna.config;

import com.example.devlogqna.service.EmailNotificationListener;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> serializer = RedisSerializer.json();

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("publicQuestions", ttl(1, serializer));
        configs.put("questionDetail", ttl(5, serializer));
        configs.put("unansweredCount", ttl(5, serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(ttl(10, serializer))
                .withInitialCacheConfigurations(configs)
                .build();
    }

    private RedisCacheConfiguration ttl(long minutes, RedisSerializer<Object> serializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(minutes))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("email:notification"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(EmailNotificationListener listener) {
        return new MessageListenerAdapter(listener);
    }
}
