package com.ttt.cinevibe.util;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RepairFlyway implements ApplicationRunner {

    private final DataSource dataSource;

    public RepairFlyway(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            System.out.println("Starting Flyway repair process...");

            // Create a Flyway instance and point it to the database
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .cleanDisabled(false) // Explicitly enable cleaning
                .load();

            // Repair the flyway_schema_history table
            flyway.repair();
            System.out.println("Flyway repair completed successfully.");
            
            // Clean database by default - will drop all tables and recreate
            // Only skip if explicitly told not to clean with --no-clean argument
            boolean shouldClean = !args.containsOption("no-clean");
            if (shouldClean) {
                System.out.println("Cleaning the database (all data will be lost)...");
                flyway.clean();
                System.out.println("Database cleaned successfully.");
            }
            
            // Attempt migration
            flyway.migrate();
            System.out.println("Flyway migration completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during Flyway repair: " + e.getMessage());
            e.printStackTrace();
        }
    }
}