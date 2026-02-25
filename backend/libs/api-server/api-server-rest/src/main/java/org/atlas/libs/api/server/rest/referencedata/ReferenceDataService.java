package org.atlas.libs.api.server.rest.referencedata;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.enums.ReferenceData;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReferenceDataService {

  private static final String[] BASE_PACKAGES = {
      "org.atlas.libs.framework.domain.shared",
      "org.atlas.services"
  };

  private final Map<String, Class<? extends Enum<?>>> registry = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    scanAndRegisterEnums();
    log.info("Registered {} reference data types: {}", registry.size(), registry.keySet());
  }

  private void scanAndRegisterEnums() {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(ReferenceData.class));

    for (String basePackage : BASE_PACKAGES) {
      Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
      for (BeanDefinition candidate : candidates) {
        try {
          @SuppressWarnings("unchecked")
          Class<? extends Enum<?>> enumClass =
              (Class<? extends Enum<?>>) Class.forName(candidate.getBeanClassName());

          if (enumClass.isEnum()) {
            String key = resolveKey(enumClass);
            registry.put(key, enumClass);
            log.debug("Registered reference data: {} -> {}", key, enumClass.getName());
          }
        } catch (ClassNotFoundException e) {
          log.warn("Failed to load class: {}", candidate.getBeanClassName(), e);
        }
      }
    }
  }

  private String resolveKey(Class<? extends Enum<?>> enumClass) {
    ReferenceData annotation = enumClass.getAnnotation(ReferenceData.class);
    if (annotation != null && !annotation.value().isEmpty()) {
      return annotation.value().toUpperCase();
    }
    // Convert class name to UPPER_SNAKE_CASE
    // Example: OrderStatus -> ORDER_STATUS
    return toUpperSnakeCase(enumClass.getSimpleName());
  }

  private String toUpperSnakeCase(String camelCase) {
    return camelCase
        .replaceAll("([a-z])([A-Z])", "$1_$2")
        .toUpperCase();
  }

  public Map<String, String> getData(String type) {
    Class<? extends Enum<?>> enumClass = registry.get(type.toUpperCase());
    if (enumClass == null) {
      return null;
    }
    return Arrays.stream(enumClass.getEnumConstants())
        .collect(Collectors.toMap(
            Enum::name,
            this::resolveEnumValue,
            (a, b) -> a,
            LinkedHashMap::new
        ));
  }

  private String resolveEnumValue(Enum<?> enumValue) {
    // Try to get value from getValue() method
    try {
      var method = enumValue.getClass().getMethod("getValue");
      Object result = method.invoke(enumValue);
      if (result != null) {
        String value = result.toString();
        if (!value.isEmpty()) {
          return value;
        }
      }
    } catch (NoSuchMethodException e) {
      // getValue() method not found, use fallback
    } catch (Exception e) {
      log.warn("Failed to invoke getValue() on enum {}", enumValue.name(), e);
    }

    // Fallback to formatEnumName
    return formatEnumName(enumValue);
  }

  private String formatEnumName(Enum<?> enumValue) {
    String name = enumValue.name();
    return Arrays.stream(name.split("_"))
        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
        .collect(Collectors.joining(" "));
  }
}



