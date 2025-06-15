package org.atlas.infrastructure.lock.redisson.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.SslVerificationMode;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

  private final RedisProperties redisProperties;

  /**
   * Configures Redisson client for standalone or cluster mode with optional password and SSL.
   */
  @Bean(destroyMethod = "shutdown") // <-- Add destroy method to properly close RedissonClient
  public RedissonClient redissonClient() {
    Config config = new Config();

    final boolean isSslEnabled = redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled();
    final String protocol = isSslEnabled ? "rediss://" : "redis://";
    final String password = StringUtils.isNotBlank(redisProperties.getPassword())
        ? redisProperties.getPassword()
        : null;

    RedisProperties.Cluster cluster = redisProperties.getCluster();
    if (cluster != null && cluster.getNodes() != null && !cluster.getNodes().isEmpty()) {
      // Cluster mode
      String[] nodeAddresses = cluster.getNodes().stream()
          .map(node -> protocol + node.trim()) // <-- safer to trim
          .toArray(String[]::new);
      ClusterServersConfig clusterConfig = config.useClusterServers()
          .addNodeAddress(nodeAddresses);
      if (StringUtils.isNotBlank(password)) {
        clusterConfig.setPassword(password);
      }
      if (isSslEnabled) {
        clusterConfig.setSslVerificationMode(SslVerificationMode.STRICT);
      }

    } else {
      // Standalone mode
      if (StringUtils.isBlank(redisProperties.getHost())) {
        throw new IllegalArgumentException("Redis host must be specified for standalone mode");
      }
      String address = protocol + redisProperties.getHost().trim() + ":" + redisProperties.getPort();
      SingleServerConfig singleConfig = config.useSingleServer()
          .setAddress(address);
      if (StringUtils.isNotBlank(password)) {
        singleConfig.setPassword(password);
      }
      if (isSslEnabled) {
        singleConfig.setSslVerificationMode(SslVerificationMode.NONE);
      }
    }

    return Redisson.create(config);
  }
}
