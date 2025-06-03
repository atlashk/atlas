package org.atlas.infrastructure.scheduler.quartz.core;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzDataSourceConfig {

  @Bean
  @QuartzDataSource
  @ConfigurationProperties("spring.datasource.quartz")
  public DataSource quartzDataSource() {
    return DataSourceBuilder.create().build();
  }
}
