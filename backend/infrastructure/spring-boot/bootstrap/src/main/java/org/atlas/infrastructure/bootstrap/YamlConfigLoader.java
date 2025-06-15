package org.atlas.infrastructure.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

/**
 * Load YAML files from libraries into the current Spring Boot application
 */
@Configuration
@Slf4j
public class YamlConfigLoader implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final String YAML_PATTERN = "classpath*:/application.yaml";
  private static final String YML_PATTERN = "classpath*:/application.yml";

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    try {
      loadYamlResources(applicationContext);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load YAML resources", e);
    }
  }

  private void loadYamlResources(ConfigurableApplicationContext applicationContext)
      throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    ConfigurableEnvironment environment = applicationContext.getEnvironment();
    MutablePropertySources propertySources = environment.getPropertySources();

    // Load both .yaml and .yml files
    Resource[] yamlResources = resolver.getResources(YAML_PATTERN);
    Resource[] ymlResources = resolver.getResources(YML_PATTERN);

    // Combine both arrays
    Resource[] allResources = new Resource[yamlResources.length + ymlResources.length];
    System.arraycopy(yamlResources, 0, allResources, 0, yamlResources.length);
    System.arraycopy(ymlResources, 0, allResources, yamlResources.length, ymlResources.length);

    for (Resource resource : allResources) {
      String sourceName = resource.getURI().toString();
      parseYaml(resource, sourceName)
          .ifPresent(propertySource -> {
            propertySources.addLast(propertySource);
            log.info("Loaded config: {}", sourceName);
          });
    }
  }

  private Optional<PropertySource<?>> parseYaml(Resource resource, String sourceName)
      throws IOException {
    try (InputStream inputStream = resource.getInputStream()) {
      Yaml yaml = new Yaml();
      Map<String, Object> yamlMap = yaml.load(inputStream);
      if (MapUtils.isEmpty(yamlMap)) {
        return Optional.empty();
      }

      // Convert nested maps to flat structure with dot notation
      Map<String, Object> flatProperties = new LinkedHashMap<>();
      flattenMap("", yamlMap, flatProperties);

      // Convert to Properties
      Properties properties = new Properties();
      flatProperties.forEach(
          (key, value) -> properties.put(key, value != null ? value.toString() : ""));

      return Optional.of(new PropertiesPropertySource(sourceName, properties));
    }
  }

  private void flattenMap(String prefix, Map<String, Object> source, Map<String, Object> target) {
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        flattenMap(key, map, target);
      } else {
        target.put(key, value);
      }
    }
  }
}

