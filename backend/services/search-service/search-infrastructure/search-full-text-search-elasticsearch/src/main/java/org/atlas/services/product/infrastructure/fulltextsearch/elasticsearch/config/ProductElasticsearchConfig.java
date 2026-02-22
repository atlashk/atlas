package org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.repository")
public class ProductElasticsearchConfig {

}
