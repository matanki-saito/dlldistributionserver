package com.popush.triela.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/*
AWSでERR unknown command `CONFIG`がでてしまう対策
https://qiita.com/tq_jappy/items/80b3a006a1f32d055bde
 */
@EnableRedisHttpSession
@Configuration
public class SessionConfig {

    @Bean
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
