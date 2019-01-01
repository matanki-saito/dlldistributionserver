package com.popush.triela.common;

import com.popush.triela.common.github.GitHubReposResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    //private final GitHubReposInterceptor gitHubReposInterceptor;
    private final GitHubReposResolver gitHubReposResolver;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(gitHubReposInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/static/**");
//    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(gitHubReposResolver);
    }
}
