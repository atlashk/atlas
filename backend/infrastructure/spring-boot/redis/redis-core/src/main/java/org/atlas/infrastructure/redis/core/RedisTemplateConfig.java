package org.atlas.infrastructure.redis.core;

import org.atlas.framework.json.jackson.JacksonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

  private static final StringRedisSerializer keySerializer = new StringRedisSerializer();
  private static final GenericJackson2JsonRedisSerializer valueSerializer =
      new GenericJackson2JsonRedisSerializer(JacksonService.objectMapper);

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);

    redisTemplate.setKeySerializer(keySerializer);
    redisTemplate.setValueSerializer(valueSerializer);
    redisTemplate.setHashKeySerializer(keySerializer);
    redisTemplate.setHashValueSerializer(valueSerializer);

    return redisTemplate;
  }

  @Bean
  public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
    RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
        .<String, Object>newSerializationContext(keySerializer)
        .value(valueSerializer)
        .hashKey(keySerializer)
        .hashValue(valueSerializer)
        .build();

    return new ReactiveRedisTemplate<>(factory, serializationContext);
  }
}
