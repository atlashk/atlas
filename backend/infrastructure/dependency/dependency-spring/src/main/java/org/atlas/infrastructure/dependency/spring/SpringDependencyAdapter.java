package org.atlas.infrastructure.dependency.spring;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.dependency.DependencyPort;
import org.atlas.framework.util.MapUtil;
import org.atlas.framework.util.ReflectionUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringDependencyAdapter implements DependencyPort {

  private final ApplicationContext applicationContext;

  @Override
  public <T> Optional<T> getInstanceByName(String name, Class<T> type) {
    return Optional.of(applicationContext.getBean(name, type));
  }

  @Override
  public <T> Optional<T> getInstanceByType(Class<T> type) {
    return Optional.of(applicationContext.getBean(type));
  }

  @Override
  public <T> Map<String, T> getAllInstancesByType(Class<T> type) {
    return applicationContext.getBeansOfType(type);
  }

  @Override
  public Map<String, Object> getAllInstancesByAnnotation(
      Class<? extends Annotation> annotationType) {
    return applicationContext.getBeansWithAnnotation(annotationType);
  }

  @Override
  public <T extends Annotation, S> Optional<Object> getInstanceByAnnotationAttribute(
      Class<T> annotationType, Class<S> attributeType, String attributeName, S attributeValue) {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);
    if (MapUtil.isEmpty(beans)) {
      return Optional.empty();
    }

    return beans.values()
        .stream()
        .filter(o -> {
          // Get the target class if it's a proxy
          Class<?> targetClass = AopUtils.isAopProxy(o) ? AopUtils.getTargetClass(o) : o.getClass();

          // Use AnnotationUtils to find annotation on target class
          T annotation = AnnotationUtils.findAnnotation(targetClass, annotationType);
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
