package io.onemonitor.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * OneMonitor Monitoring Service Application.
 * 
 * This is the main entry point for the Monitoring microservice.
 */
@SpringBootApplication
@EnableJpaAuditing
public class MonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoringApplication.class, args);
    }
}
