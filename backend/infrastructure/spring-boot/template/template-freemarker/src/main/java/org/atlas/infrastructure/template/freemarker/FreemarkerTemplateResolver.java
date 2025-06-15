package org.atlas.infrastructure.template.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Component
@RequiredArgsConstructor
public class FreemarkerTemplateResolver {

  private final Configuration configuration;

  /**
   * @param templateName The relative path of template file in resources/templates folder.
   */
  public String resolve(@Nonnull String templateName, Map<String, Object> data)
      throws IOException, TemplateException {
    return FreeMarkerTemplateUtils.processTemplateIntoString(
        configuration.getTemplate(templateName), data);
  }
}
