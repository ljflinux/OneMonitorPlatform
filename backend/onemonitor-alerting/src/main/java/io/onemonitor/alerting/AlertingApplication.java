package io.onemonitor.alerting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * OneMonitor Alerting Microservice.
 * 
 * Provides alerting capabilities including:
 * - Alert Rule management (CRUD, enable/disable)
 * - Alert Instance lifecycle (fire, acknowledge, resolve)
 * - Alert evaluation and notification
 * - Alert statistics and metrics
 */
@SpringBootApplication
@EntityScan(basePackages = {
    "io.onemonitor.alerting.domain.model",
    "io.onemonitor.common.domain.base",
    "io.onemonitor.cmdb.domain.model",
    "io.onemonitor.monitoring.domain.model"
})
@EnableJpaRepositories(basePackages = {
    "io.onemonitor.alerting.domain.repository",
    "io.onemonitor.common.domain.repository"
})
public class AlertingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertingApplication.class, args);
    }
}
