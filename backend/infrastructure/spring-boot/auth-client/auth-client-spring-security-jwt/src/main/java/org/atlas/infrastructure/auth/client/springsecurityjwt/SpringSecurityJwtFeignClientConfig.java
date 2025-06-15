package org.atlas.infrastructure.auth.client.springsecurityjwt;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.atlas.infrastructure.auth.client.springsecurityjwt")
public class SpringSecurityJwtFeignClientConfig {

}
