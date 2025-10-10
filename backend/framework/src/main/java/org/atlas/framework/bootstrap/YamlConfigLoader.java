package org.atlas.framework.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.util.MapUtil;
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
  private static final String PROFILE_YAML_PATTERN = "classpath*:/application-%s.yaml";
  private static final String PROFILE_YML_PATTERN = "classpath*:/application-%s.yml";

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

    // Load base configuration files first
    loadBaseConfigFiles(resolver, propertySources);

    // Load profile-specific configuration files
    loadProfileConfigFiles(resolver, propertySources, environment);
  }

  private void loadBaseConfigFiles(PathMatchingResourcePatternResolver resolver,
      MutablePropertySources propertySources) throws IOException {
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
            log.debug("Loaded config: {}", sourceName);
          });
    }
  }

  private void loadProfileConfigFiles(PathMatchingResourcePatternResolver resolver,
      MutablePropertySources propertySources, ConfigurableEnvironment environment)
      throws IOException {
    String[] activeProfiles = environment.getActiveProfiles();

    // If no active profiles, check for default profiles
    if (activeProfiles.length == 0) {
      activeProfiles = environment.getDefaultProfiles();
    }

    // Load profile-specific files for each active profile
    for (String profile : activeProfiles) {
      loadProfileSpecificFiles(resolver, propertySources, profile);
    }
  }

  private void loadProfileSpecificFiles(PathMatchingResourcePatternResolver resolver,
      MutablePropertySources propertySources, String profile) throws IOException {
    // Load profile-specific .yaml files
    Resource[] profileYamlResources = resolver.getResources(
        String.format(PROFILE_YAML_PATTERN, profile));
    Resource[] profileYmlResources = resolver.getResources(
        String.format(PROFILE_YML_PATTERN, profile));

    // Combine both arrays
    Resource[] allProfileResources = new Resource[profileYamlResources.length
        + profileYmlResources.length];
    System.arraycopy(profileYamlResources, 0, allProfileResources, 0, profileYamlResources.length);
    System.arraycopy(profileYmlResources, 0, allProfileResources, profileYamlResources.length,
        profileYmlResources.length);

    for (Resource resource : allProfileResources) {
      String sourceName = resource.getURI().toString();
      parseYaml(resource, sourceName)
          .ifPresent(propertySource -> {
            // Add profile-specific properties with higher precedence (addFirst instead of addLast)
            propertySources.addFirst(propertySource);
            log.debug("Loaded profile config for '{}': {}", profile, sourceName);
          });
    }
  }

  private Optional<PropertySource<?>> parseYaml(Resource resource, String sourceName)
      throws IOException {
    try (InputStream inputStream = resource.getInputStream()) {
      Yaml yaml = new Yaml();
      Map<String, Object> yamlMap = yaml.load(inputStream);
      if (MapUtil.isEmpty(yamlMap)) {
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

