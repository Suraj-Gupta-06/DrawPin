package com.drawpin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * DrawPin 2.0 — Spring Boot Application Entry Point.
 *
 * <p>This class bootstraps the entire Spring context. The following cross-cutting
 * behaviours are enabled here at the class level:
 * <ul>
 *   <li>{@code @EnableJpaAuditing} — populates {@code @CreatedDate} and {@code @LastModifiedDate}
 *       fields on all auditable entities automatically.</li>
 *   <li>{@code @EnableAsync} — allows {@code @Async} annotated service methods (e.g., email
 *       sending) to run on a separate thread pool without blocking the HTTP request thread.</li>
 * </ul>
 *
 * <p><b>Connected modules:</b>
 * <ul>
 *   <li>All Spring Beans in the {@code com.drawpin} package are auto-discovered.</li>
 *   <li>Flyway runs database migrations before the JPA context initialises.</li>
 *   <li>Spring Security filter chain activates on every inbound HTTP request.</li>
 * </ul>
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class DrawPinApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrawPinApplication.class, args);
    }
}
