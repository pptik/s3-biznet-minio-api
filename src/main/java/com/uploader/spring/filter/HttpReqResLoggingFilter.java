package com.uploader.spring.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpReqResLoggingFilter extends OncePerRequestFilter {

    private static final List<String> SENSITIVE_HEADERS = List.of(
            "authorization", "cookie", "x-api-key", "set-cookie");

    private boolean isSensitiveHeader(String headerName) {
        return SENSITIVE_HEADERS.contains(headerName);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, long duration) {
        StringBuilder msg = new StringBuilder();
        msg.append("\n--- HTTP Request ---\n");
        msg.append("Method: ").append(request.getMethod()).append("\n");
        msg.append("URI: ").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            msg.append("?").append(request.getQueryString());
        }
        msg.append("\n");
        msg.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        msg.append("Duration: ").append(duration).append("ms\n");

        msg.append("Headers:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            msg.append("  ").append(headerName).append(": ");
            if (isSensitiveHeader(headerName)) {
                msg.append("[REDACTED]\n");
            } else {
                msg.append(headerValue).append("\n");
            }
        }

        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            try {
                String payload = new String(content, request.getCharacterEncoding());
                msg.append("Payload: ").append(payload).append("\n");
            } catch (UnsupportedEncodingException e) {
                log.warn("Failed to parse request payload for logging: {}", e.getMessage());
            }
        }
        msg.append("--------------------\n");
        log.info(msg.toString());
    }

}
