package org.atlas.infrastructure.scheduler.spring.core;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.atlas.framework.config.ApplicationConfigPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Once a scheduled task acquires the lock, it will automatically be released after 10 minutes, unless it is released manually earlier.
 * This prevents deadlocks in cases like:
 * - The application crashes
 * - The task throws an exception and doesn't reach the unlock point
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@RequiredArgsConstructor
public class SchedulerLockConfig {

    private final ApplicationConfigPort applicationConfigPort;

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
        String environment = applicationConfigPort.getActiveProfile();
        String prefix = applicationConfigPort.getApplicationName();
        return new RedisLockProvider(redisConnectionFactory, environment, prefix);
    }
}
