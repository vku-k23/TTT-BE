package com.ttt.cinevibe.configuration;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    
    /**
     * Disable the automatic migration that Spring Boot does on startup
     * We will handle it manually in our RepairFlyway class
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Do nothing - this effectively disables auto-migration
            // The RepairFlyway utility will handle it
            System.out.println("Automatic Flyway migration has been disabled. Using custom RepairFlyway utility instead.");
        };
    }
}