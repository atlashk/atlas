package org.atlas.services.user.infrastructure.persistence.jpa.config;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepositoryImpl;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Need to configure base packages of JPA entities and repositories in case of multi-modules
 * project.
 */
@Configuration
@EntityScan(basePackages = "org.atlas.services.user.infrastructure.persistence.jpa.entity")
@EnableJpaRepositories(
    basePackages = "org.atlas.services.user.infrastructure.persistence.jpa.repository",
    repositoryBaseClass = JpaBaseRepositoryImpl.class
)
public class UserJpaConfig {

}
