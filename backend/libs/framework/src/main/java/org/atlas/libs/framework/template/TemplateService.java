package org.atlas.libs.framework.template;

import java.util.Map;

public interface TemplateService {

  String resolveTemplate(String templateName, Map<String, Object> data) throws Exception;

  default String resolveEmailSubject(String templateName, Map<String, Object> data)
      throws Exception {
    return resolveTemplate("email/subject/" + templateName, data);
  }

  default String resolveEmailBody(String templateName, Map<String, Object> data)
      throws Exception {
    return resolveTemplate("email/body/" + templateName, data);
  }

  default String resolveInAppMessage(String templateName, Map<String, Object> data)
      throws Exception {
    return resolveTemplate("inapp/" + templateName, data);
  }
}
