package com.uploader.spring.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uploader.spring.models.dto.ResponseMessageWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiKeyInterceptor implements HandlerInterceptor {
    private final String API_KEY_HEADER_NAME = "X-API-KEY";

    @Value("${app.api-key}")
    private String validApiKey;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String apiKey = request.getHeader(API_KEY_HEADER_NAME);

        if (apiKey == null || !apiKey.equals(validApiKey)) {
            log.warn("Unauthorized access attempt: Missing or invalid API Key from IP {}", request.getRemoteAddr());

            ResponseMessageWrapper responseMessageWrapper = ResponseMessageWrapper.builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .status(false)
                    .message("Unauthorized: Invalid or missing API Key")
                    .build();

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(this.objectMapper.writeValueAsString(responseMessageWrapper));
            response.getWriter().flush();

            return false;
        }

        return true;
    }
}
