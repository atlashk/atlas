package org.atlas.libs.template.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Component
@RequiredArgsConstructor
public class FreemarkerTemplateResolver implements InitializingBean {

  private final Configuration configuration;

  @Override
  public void afterPropertiesSet() {
    // Support java.time API
    configuration.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_31));
  }

  /**
   * @param templateName The relative path of template file in resources/templates folder.
   */
  public String resolve(@Nonnull String templateName, Map<String, Object> data)
      throws IOException, TemplateException {
    return FreeMarkerTemplateUtils.processTemplateIntoString(
        configuration.getTemplate(templateName), data);
  }
}
