package org.atlas.services.catalog.infrastructure.search.elasticsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.atlas.services.catalog.infrastructure.search.elasticsearch.repository")
public class CatalogElasticsearchConfig {

}
