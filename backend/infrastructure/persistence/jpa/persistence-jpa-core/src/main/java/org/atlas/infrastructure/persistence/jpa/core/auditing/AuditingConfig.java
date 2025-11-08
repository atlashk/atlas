package org.atlas.infrastructure.persistence.jpa.core.auditing;

import java.util.Optional;
import org.atlas.framework.util.DateUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Also need configure on
 * {@link org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity}
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class AuditingConfig {

  @Bean
  public DateTimeProvider dateTimeProvider() {
    return () -> Optional.of(DateUtil.now().toInstant());
  }
}
