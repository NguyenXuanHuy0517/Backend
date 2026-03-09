package com.project.logiclayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.project.datalayer", "com.project.logiclayer"})
@EnableJpaRepositories(basePackages = "com.project.datalayer.repository")
@EntityScan(basePackages = "com.project.datalayer.entity")
public class LogicLayerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogicLayerApplication.class, args);
    }

}
