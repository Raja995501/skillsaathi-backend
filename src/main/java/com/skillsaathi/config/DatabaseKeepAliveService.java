package com.skillsaathi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DatabaseKeepAliveService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Har 10 minute mein (600000 milliseconds) query chalegi
    @Scheduled(fixedRate = 600000)
    public void pingDatabase() {
        try {
            jdbcTemplate.execute("SELECT 1");
            System.out.println("Aiven Database Keep-Alive Ping Successful!");
        } catch (Exception e) {
            System.err.println("Failed to ping database: " + e.getMessage());
        }
    }
}