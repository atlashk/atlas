package org.atlas.infrastructure.internalapi.product.rest.feign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.atlas.infrastructure.internalapi.product.rest.feign")
public class ProductFeignClientConfig {

}
