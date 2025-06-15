package org.atlas.framework.template;

import java.util.Map;

public interface TemplatePort {

  String resolveTemplate(String templateName, Map<String, Object> data) throws Exception;

  default String resolveEmailSubject(String templateName, Map<String, Object> data)
      throws Exception {
    return resolveTemplate("email/subject/" + templateName, data);
  }

  default String resolveEmailBody(String templateName, Map<String, Object> data)
      throws Exception {
    return resolveTemplate("email/body/" + templateName, data);
  }
}
