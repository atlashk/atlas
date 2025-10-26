package org.atlas.infrastructure.cache.redis;

import org.atlas.framework.cache.CacheService;
import org.atlas.framework.cache.DefaultCacheService;
import org.atlas.framework.kv.KvService;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheService extends DefaultCacheService implements CacheService {

  public RedisCacheService(KvService kvService) {
    super(kvService);
  }
}
