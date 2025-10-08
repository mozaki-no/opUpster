package com.opupster.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public class ApiException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  private final HttpStatusCode status;
  private final String method;
  private final String url;
  private final HttpHeaders headers;
  private final String responseBody;

  public ApiException(HttpStatusCode status, String method, String url,
                      HttpHeaders headers, String responseBody) {
    super("[%s] %s -> %s\n%s".formatted(method, url, status, abbreviate(responseBody)));
    this.status = status;
    this.method = method;
    this.url = url;
    this.headers = headers;
    this.responseBody = responseBody;
  }

  public HttpStatusCode getStatus() { return status; }
  public String getMethod() { return method; }
  public String getUrl() { return url; }
  public HttpHeaders getHeaders() { return headers; }
  public String getResponseBody() { return responseBody; }

  private static String abbreviate(String s) {
    if (s == null) return "";
    return s.length() > 2000 ? s.substring(0, 2000) + " ...(truncated)" : s;
  }
}
