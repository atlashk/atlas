package org.atlas.infrastructure.kvstore.redis.ops.reactive;

import org.atlas.framework.json.jackson.JacksonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ReactiveRedisTemplateConfig {

  private static final StringRedisSerializer keySerializer = new StringRedisSerializer();
  private static final GenericJackson2JsonRedisSerializer valueSerializer =
      new GenericJackson2JsonRedisSerializer(JacksonService.OBJECT_MAPPER);

  @Bean
  public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory factory) {
    RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
        .<String, Object>newSerializationContext(keySerializer)
        .value(valueSerializer)
        .hashKey(keySerializer)
        .hashValue(valueSerializer)
        .build();

    return new ReactiveRedisTemplate<>(factory, serializationContext);
  }
}
