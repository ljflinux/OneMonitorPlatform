package io.onemonitor.cmdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * OneMonitor CMDB Service Application.
 * 
 * This is the main entry point for the CMDB microservice.
 */
@SpringBootApplication
@EnableJpaAuditing
public class CmdbApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmdbApplication.class, args);
    }
}
