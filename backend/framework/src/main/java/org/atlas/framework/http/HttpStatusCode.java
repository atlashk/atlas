package org.atlas.framework.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing HTTP status codes with their corresponding numeric values and descriptions.
 * Organized by status code categories for better maintainability.
 */
@Getter
@RequiredArgsConstructor
public enum HttpStatusCode {

  // 1xx Informational responses
  CONTINUE(100, "Continue"),
  SWITCHING_PROTOCOLS(101, "Switching Protocols"),
  PROCESSING(102, "Processing"),

  // 2xx Success
  OK(200, "OK"),
  CREATED(201, "Created"),
  ACCEPTED(202, "Accepted"),
  NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
  NO_CONTENT(204, "No Content"),
  RESET_CONTENT(205, "Reset Content"),
  PARTIAL_CONTENT(206, "Partial Content"),
  MULTI_STATUS(207, "Multi-Status"),
  ALREADY_REPORTED(208, "Already Reported"),
  IM_USED(226, "IM Used"),

  // 3xx Redirection
  MULTIPLE_CHOICES(300, "Multiple Choices"),
  MOVED_PERMANENTLY(301, "Moved Permanently"),
  FOUND(302, "Found"),
  SEE_OTHER(303, "See Other"),
  NOT_MODIFIED(304, "Not Modified"),
  USE_PROXY(305, "Use Proxy"),
  TEMPORARY_REDIRECT(307, "Temporary Redirect"),
  PERMANENT_REDIRECT(308, "Permanent Redirect"),

  // 4xx Client errors
  BAD_REQUEST(400, "Bad Request"),
  UNAUTHORIZED(401, "Unauthorized"),
  PAYMENT_REQUIRED(402, "Payment Required"),
  FORBIDDEN(403, "Forbidden"),
  NOT_FOUND(404, "Not Found"),
  METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
  NOT_ACCEPTABLE(406, "Not Acceptable"),
  PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
  REQUEST_TIMEOUT(408, "Request Timeout"),
  CONFLICT(409, "Conflict"),
  GONE(410, "Gone"),
  LENGTH_REQUIRED(411, "Length Required"),
  PRECONDITION_FAILED(412, "Precondition Failed"),
  PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
  URI_TOO_LONG(414, "URI Too Long"),
  UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
  RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
  EXPECTATION_FAILED(417, "Expectation Failed"),
  IM_A_TEAPOT(418, "I'm a teapot"),
  MISDIRECTED_REQUEST(421, "Misdirected Request"),
  UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
  LOCKED(423, "Locked"),
  FAILED_DEPENDENCY(424, "Failed Dependency"),
  TOO_EARLY(425, "Too Early"),
  UPGRADE_REQUIRED(426, "Upgrade Required"),
  PRECONDITION_REQUIRED(428, "Precondition Required"),
  TOO_MANY_REQUESTS(429, "Too Many Requests"),
  REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
  UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

  // 5xx Server errors
  INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
  NOT_IMPLEMENTED(501, "Not Implemented"),
  BAD_GATEWAY(502, "Bad Gateway"),
  SERVICE_UNAVAILABLE(503, "Service Unavailable"),
  GATEWAY_TIMEOUT(504, "Gateway Timeout"),
  HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
  VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
  INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
  LOOP_DETECTED(508, "Loop Detected"),
  NOT_EXTENDED(510, "Not Extended"),
  NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

  private final int code;
  private final String reasonPhrase;

  /**
   * Checks if the status code represents a successful response (2xx).
   *
   * @return true if the status code is in the 2xx range
   */
  public boolean isSuccessful() {
    return code >= 200 && code < 300;
  }

  /**
   * Checks if the status code represents a client error (4xx).
   *
   * @return true if the status code is in the 4xx range
   */
  public boolean isClientError() {
    return code >= 400 && code < 500;
  }

  /**
   * Checks if the status code represents a server error (5xx).
   *
   * @return true if the status code is in the 5xx range
   */
  public boolean isServerError() {
    return code >= 500 && code < 600;
  }

  /**
   * Checks if the status code represents an error (4xx or 5xx).
   *
   * @return true if the status code is in the 4xx or 5xx range
   */
  public boolean isError() {
    return isClientError() || isServerError();
  }

  /**
   * Checks if the status code represents a redirection (3xx).
   *
   * @return true if the status code is in the 3xx range
   */
  public boolean isRedirection() {
    return code >= 300 && code < 400;
  }

  /**
   * Checks if the status code represents an informational response (1xx).
   *
   * @return true if the status code is in the 1xx range
   */
  public boolean isInformational() {
    return code >= 100 && code < 200;
  }

  /**
   * Finds an HttpStatusCode by its numeric code.
   *
   * @param code the numeric HTTP status code
   * @return the corresponding HttpStatusCode, or null if not found
   */
  public static HttpStatusCode valueOf(int code) {
    for (HttpStatusCode status : values()) {
      if (status.code == code) {
        return status;
      }
    }
    return null;
  }

  /**
   * Returns a string representation of the status code in the format "code reasonPhrase".
   *
   * @return formatted string representation
   */
  @Override
  public String toString() {
    return code + " " + reasonPhrase;
  }
}

