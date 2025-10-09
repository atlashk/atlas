package org.atlas.infrastructure.cache.redis;

import org.atlas.framework.cache.CacheService;
import org.atlas.framework.cache.DefaultKvCacheService;
import org.atlas.framework.kv.KvService;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheService extends DefaultKvCacheService implements CacheService {

  public RedisCacheService(KvService kvService) {
    super(kvService);
  }
}
