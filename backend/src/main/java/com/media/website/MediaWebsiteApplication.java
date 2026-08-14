package com.media.website;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.media.website.mapper")
public class MediaWebsiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaWebsiteApplication.class, args);
    }
}
