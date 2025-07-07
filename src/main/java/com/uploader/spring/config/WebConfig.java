package com.uploader.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.uploader.spring.filter.HttpReqResLoggingFilter;
import com.uploader.spring.interceptor.ApiKeyInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ApiKeyInterceptor apiKeyInterceptor;

    @Autowired
    private HttpReqResLoggingFilter httpReqResLoggingFilter;

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(apiKeyInterceptor).addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/serve/**");
    }

    @Bean
    FilterRegistrationBean<HttpReqResLoggingFilter> loggingFilterRegistration() {
        FilterRegistrationBean<HttpReqResLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(httpReqResLoggingFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
