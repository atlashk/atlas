package org.atlas.infrastructure.fulltextsearch.elasticsearch.impl.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.atlas.infrastructure.fulltextsearch.elasticsearch.impl.product.repository")
public class ProductElasticsearchConfig {

}
