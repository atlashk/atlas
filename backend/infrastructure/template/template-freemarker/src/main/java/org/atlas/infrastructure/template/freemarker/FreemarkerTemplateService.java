package org.atlas.infrastructure.template.freemarker;

import jakarta.annotation.Nonnull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.template.TemplateService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FreemarkerTemplateService implements TemplateService {

  private final FreemarkerTemplateResolver freemarkerTemplateResolver;

  @Override
  public String resolveTemplate(@Nonnull String templateName, Map<String, Object> data)
      throws Exception {
    String path = templateName + ".ftl";
    return freemarkerTemplateResolver.resolve(path, data);
  }
}
