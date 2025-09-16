package org.atlas.infrastructure.api.server.rest.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.atlas.framework.json.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@UtilityClass
public class HttpUtil {

  /**
   * Helper method to read a header safely
   */
  public String getHeader(HttpServletRequest request, String headerName) {
    return StringUtils.trimToNull(request.getHeader(headerName));
  }

  public static void respondJson(HttpServletResponse response, Object payload,
      HttpStatus httpStatus) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setStatus(httpStatus.value());

    PrintWriter out = response.getWriter();
    out.print(JsonUtil.getInstance().toJson(payload));
    out.flush();
  }
}
