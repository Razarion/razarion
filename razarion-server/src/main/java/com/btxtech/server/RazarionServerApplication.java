package com.btxtech.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RazarionServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RazarionServerApplication.class, args);
    }

}
