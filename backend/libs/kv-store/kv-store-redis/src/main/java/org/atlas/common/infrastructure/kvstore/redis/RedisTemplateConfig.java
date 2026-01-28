package org.atlas.common.infrastructure.kvstore.redis;

import org.atlas.common.framework.json.jackson.JacksonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

  private static final StringRedisSerializer keySerializer = new StringRedisSerializer();
  private static final GenericJackson2JsonRedisSerializer valueSerializer =
      new GenericJackson2JsonRedisSerializer(JacksonService.OBJECT_MAPPER);

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
}
