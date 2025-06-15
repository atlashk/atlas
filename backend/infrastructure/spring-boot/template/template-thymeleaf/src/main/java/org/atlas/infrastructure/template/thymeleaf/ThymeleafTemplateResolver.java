package org.atlas.infrastructure.template.thymeleaf;

import jakarta.annotation.Nonnull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class ThymeleafTemplateResolver {

  private final TemplateEngine templateEngine;

  public String resolve(@Nonnull String templateName, Map<String, Object> data)
      throws Exception {
    // Create a context with the provided data
    Context context = new Context();
    // Add each entry in the data map to the context
    if (data != null) {
      data.forEach(context::setVariable);
    }
    // Render the template with the provided data
    return templateEngine.process(templateName, context);
  }
}
