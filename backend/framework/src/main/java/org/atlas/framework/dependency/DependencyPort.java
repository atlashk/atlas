package org.atlas.framework.dependency;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

public interface DependencyPort {

  <T> Optional<T> getInstanceByName(String name, Class<T> type);

  <T> Optional<T> getInstanceByType(Class<T> type);

  <T> Map<String, T> getAllInstancesByType(Class<T> type);

  Map<String, Object> getAllInstancesByAnnotation(Class<? extends Annotation> annotationType);

  <T extends Annotation, S> Optional<Object> getInstanceByAnnotationAttribute(
      Class<T> annotationType, Class<S> attributeType, String attributeName, S attributeValue);
}
