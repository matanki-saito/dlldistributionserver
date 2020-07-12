package com.popush.triela.common.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<RequestRejectedExceptionLoggingFilter> requestRejectedExceptionHandlerFilter() {
        var bean = new FilterRegistrationBean<>(new RequestRejectedExceptionLoggingFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return bean;
    }
}
