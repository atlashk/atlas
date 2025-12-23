package org.atlas.infrastructure.persistence.jpa.adapter.order.config;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Need to configure base packages of JPA entities and repositories in case of multi-modules
 * project.
 */
@Configuration
@EntityScan(basePackages = "org.atlas.infrastructure.persistence.jpa.adapter.order.entity")
@EnableJpaRepositories(
    basePackages = "org.atlas.infrastructure.persistence.jpa.adapter.order.repository",
    repositoryBaseClass = JpaBaseRepositoryImpl.class
)
public class OrderJpaConfig {

}
