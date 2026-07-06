package com.drawpin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async task executor configuration.
 *
 * <p>Defines a dedicated thread pool for {@code @Async} methods, primarily used for
 * sending transactional emails without blocking the HTTP request thread.
 *
 * <p>Without a custom executor, Spring's {@code @EnableAsync} uses a single-threaded
 * executor that can create bottlenecks under load. This configuration provides a
 * bounded thread pool with a sensible queue for email dispatch and future async tasks.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.service.auth.EmailService} — {@code @Async} email dispatch</li>
 *   <li>{@link DrawPinApplication} — {@code @EnableAsync} activates this</li>
 * </ul>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Creates the primary async task executor.
     *
     * <p>Thread pool sizing rationale for email-heavy workloads:
     * <ul>
     *   <li>Core threads: 4 — always available for immediate email dispatch</li>
     *   <li>Max threads: 16 — burst capacity during registration spikes</li>
     *   <li>Queue capacity: 100 — buffer for transient spikes without rejection</li>
     * </ul>
     *
     * @return a configured {@link ThreadPoolTaskExecutor}
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("drawpin-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
