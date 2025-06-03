package org.atlas.infrastructure.scheduler.quartz.core;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzDataSourceConfig {

  @Bean
  @QuartzDataSource
  public DataSource quartzDataSource(@Qualifier("quartzDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  @ConfigurationProperties("spring.datasource.quartz")
  public DataSourceProperties quartzDataSourceProperties() {
    return new DataSourceProperties();
  }
}
