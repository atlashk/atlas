package org.atlas.infrastructure.internalapi.user.rest.feign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.atlas.infrastructure.internalapi.user.rest.feign")
public class UserFeignClientConfig {

}
