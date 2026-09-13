package com.skillsaathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SkillSaathiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillSaathiApplication.class, args);
    }
}
