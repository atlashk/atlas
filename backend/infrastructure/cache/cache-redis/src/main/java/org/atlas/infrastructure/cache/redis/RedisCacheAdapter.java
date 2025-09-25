package org.atlas.infrastructure.cache.redis;

import org.atlas.framework.cache.CachePort;
import org.atlas.framework.cache.DefaultKvCacheAdapter;
import org.atlas.framework.kv.KvPort;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheAdapter extends DefaultKvCacheAdapter implements CachePort {

  public RedisCacheAdapter(KvPort kvPort) {
    super(kvPort);
  }
}
