package com.popush.triela.common.auth;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 設定するとredirect-uriが間違った値？になってしまうのでymlでしっかり設定すること。
        http.authorizeRequests(authorize -> authorize.antMatchers("/login",
                                                                  "/authorize/**",
                                                                  "/login/**",
                                                                  "/api/**",
                                                                  "/actuator/prometheus",
                                                                  "/actuator/info",
                                                                  "/actuator/health",
                                                                  "/error")
                                                     .permitAll()
                                                     .anyRequest()
                                                     .authenticated())
            .oauth2Login(withDefaults());
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
           .antMatchers("/api/**");
    }
}
