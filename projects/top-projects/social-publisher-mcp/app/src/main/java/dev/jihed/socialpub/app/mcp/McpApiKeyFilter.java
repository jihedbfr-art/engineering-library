package dev.jihed.socialpub.app.mcp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Guards the HTTP MCP transport (and admin endpoints) with a static bearer token from {@code
 * MCP_API_KEY}. The stdio profile runs without this filter — there the transport is a local pipe.
 * Health checks stay open so container probes work.
 */
@Component
@Profile("!stdio")
public class McpApiKeyFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(McpApiKeyFilter.class);

  private final String apiKey;

  public McpApiKeyFilter(@Value("${socialpub.mcp.api-key:}") String apiKey) {
    this.apiKey = apiKey;
    if (apiKey == null || apiKey.isBlank()) {
      log.warn("MCP_API_KEY is not set — the MCP endpoint is unauthenticated.");
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return apiKey == null
        || apiKey.isBlank()
        || path.startsWith("/actuator/health")
        || path.equals("/actuator/info");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ") || !constantEquals(header.substring(7))) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"error\":\"invalid or missing bearer token\"}");
      return;
    }
    chain.doFilter(request, response);
  }

  private boolean constantEquals(String provided) {
    return java.security.MessageDigest.isEqual(
        provided.getBytes(java.nio.charset.StandardCharsets.UTF_8),
        apiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
