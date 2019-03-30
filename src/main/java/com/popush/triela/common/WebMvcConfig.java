package com.popush.triela.common;

import com.popush.triela.common.github.GitHubReposResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final GitHubReposResolver gitHubReposResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(gitHubReposResolver);
    }

    /**
     * Cache-Controlを設定する
     *
     * @param registry registry
     * @see <ExeDao href="https://stackoverflow.com/questions/24164014/how-to-enable-http-response-caching-in-spring-boot">参考</ExeDao>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // static
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600 * 24); // 1日
    }

    /**
     * Etagを設定するためのフィルタ api/*のみで有効
     *
     * @return フィルタbean
     */
    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> shallowEtagBean() {
        final FilterRegistrationBean<ShallowEtagHeaderFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(new ShallowEtagHeaderFilter());
        frb.addUrlPatterns("/api/*");
        frb.setOrder(2);
        return frb;
    }
}
