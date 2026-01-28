package org.atlas.product.fulltextsearch.elasticsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.atlas.product.fulltextsearch.elasticsearch.repository")
public class ProductElasticsearchConfig {

}
