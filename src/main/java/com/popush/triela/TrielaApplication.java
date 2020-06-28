package com.popush.triela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TrielaApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrielaApplication.class, args);
    }
}

