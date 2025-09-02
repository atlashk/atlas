package org.atlas.infrastructure.application.context;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.util.MapUtil;
import org.atlas.framework.util.ReflectionUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationContextService {

  private final ApplicationContext applicationContext;

  public <T extends Annotation, S> Optional<Object> getBeanByAnnotationAttribute(
      Class<T> annotationType, Class<S> attributeType, String attributeName, S attributeValue) {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);
    if (MapUtil.isEmpty(beans)) {
      return Optional.empty();
    }

    return beans.values()
        .stream()
        .filter(o -> {
          T annotation = o
              .getClass()
              .getAnnotation(annotationType);
          if (annotation == null) {
            return false;
          }

          S value = ReflectionUtil.getAnnotationAttributeValue(annotation, attributeName,
              attributeType);
          return attributeValue.equals(value);
        })
        .findAny();
  }
}
