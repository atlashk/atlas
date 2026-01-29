package org.atlas.libs.persistence.jpa.auditing;

import java.util.Optional;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Also need configure on {@link JpaBaseEntity}
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class AuditingConfig {

  @Bean
  public DateTimeProvider dateTimeProvider() {
    return () -> Optional.of(DateUtil.now().toInstant());
  }
}
