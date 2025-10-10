package org.atlas.infrastructure.internalapi.payment.rest.feign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.atlas.infrastructure.internalapi.payment.rest.feign.client")
public class PaymentFeignClientConfig {

}
