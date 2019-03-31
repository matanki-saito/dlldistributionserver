package com.popush.triela.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = {
        "com.popush.triela.db"
})
public class MapperTestApplication {
    /* 何も書かないこと */
    /* http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-test-autoconfigure/ */
}
