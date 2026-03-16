package com.project.logiclayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LogicLayerApplication: Entry point của module logic-layer.
 *
 * Giải thích các annotation:
 * @SpringBootApplication(scanBasePackages):
 *   Scan cả package của data-layer (entities, mappers) lẫn logic-layer (services, controllers).
 *   Nếu không khai báo scanBasePackages, Spring chỉ scan package com.project.logiclayer
 *   và không thấy các @Component trong com.project.datalayer.
 *
 * @EnableJpaRepositories: Chỉ định nơi chứa Repository interfaces để Spring Data JPA
 *   tạo implementation tự động.
 *
 * @EntityScan: Chỉ định nơi chứa @Entity classes để Hibernate biết cần tạo/validate bảng nào.
 */
@SpringBootApplication(scanBasePackages = {
    "com.project.datalayer",
    "com.project.logiclayer"
})
@EnableJpaRepositories(basePackages = "com.project.datalayer.repository")
@EntityScan(basePackages = "com.project.datalayer.entity")
@EnableScheduling   // BẮT BUỘC để @Scheduled trong Scheduler classes hoạt động
public class LogicLayerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogicLayerApplication.class, args);
    }
}
