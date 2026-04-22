package com.group1.mangaflowweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MangaFlowWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MangaFlowWebApplication.class, args);
    }
}
