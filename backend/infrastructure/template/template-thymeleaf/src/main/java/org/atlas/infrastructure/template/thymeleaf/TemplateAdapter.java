package org.atlas.infrastructure.template.thymeleaf;

import jakarta.annotation.Nonnull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.template.TemplatePort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateAdapter implements TemplatePort {

  private final ThymeleafTemplateResolver thymeleafTemplateResolver;

  @Override
  public String resolveTemplate(@Nonnull String templateName, Map<String, Object> data)
      throws Exception {
    String path = templateName + ".html";
    return thymeleafTemplateResolver.resolve(path, data);
  }
}
