package org.atlas.services.payment.infrastructure.gateway.simulator;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.payment.simulator")
@Getter
@Setter
public class SimulatorProps {

  private String webhookUrl;
}
