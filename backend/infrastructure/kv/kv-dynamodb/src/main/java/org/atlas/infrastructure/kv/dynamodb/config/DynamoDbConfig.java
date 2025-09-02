package org.atlas.infrastructure.kv.dynamodb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Slf4j
public class DynamoDbConfig implements DisposableBean {

  private DynamoDbClient dynamoDbClient;

  @Bean
  public DynamoDbClient dynamoDbClient() {
    dynamoDbClient = DynamoDbClient.builder().build();
    log.info("Initialized DynamoDB client");
    return this.dynamoDbClient;
  }

  @Override
  public void destroy() throws Exception {
    if (this.dynamoDbClient != null) {
      this.dynamoDbClient.close();
      log.info("Closed DynamoDB client");
    }
  }
}
