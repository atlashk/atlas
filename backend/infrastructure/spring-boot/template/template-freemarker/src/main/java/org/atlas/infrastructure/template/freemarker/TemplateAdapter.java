package org.atlas.infrastructure.template.freemarker;

import jakarta.annotation.Nonnull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.template.TemplatePort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateAdapter implements TemplatePort {

  private final FreemarkerTemplateResolver freemarkerTemplateResolver;

  @Override
  public String resolveTemplate(@Nonnull String templateName, Map<String, Object> data)
      throws Exception {
    String path = templateName + ".ftl";
    return freemarkerTemplateResolver.resolve(path, data);
  }
}
