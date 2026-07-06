package com.drawpin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Mail configuration for transactional email delivery.
 *
 * <p>Spring Boot's auto-configuration handles the {@link JavaMailSender} bean when
 * {@code spring.mail.*} properties are set. This class exists to allow future
 * customization (e.g., switching to a third-party provider like SendGrid, Mailgun, or
 * Postmark) without touching the auto-configured defaults.
 *
 * <p>In production, set {@code MAIL_HOST}, {@code MAIL_PORT}, {@code MAIL_USERNAME},
 * and {@code MAIL_PASSWORD} environment variables. The auto-configuration picks these
 * up from {@code application.yml}.
 *
 * <p>This class currently acts as a marker configuration. The actual
 * {@link JavaMailSender} is provided by Spring Boot auto-configuration. If you need
 * to override the sender (e.g., for SendGrid SMTP bridge), define a custom
 * {@link JavaMailSenderImpl} bean here.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.service.auth.EmailService} — injects {@link JavaMailSender}</li>
 * </ul>
 */
@Configuration
public class MailConfig {
    // Spring Boot's MailSenderAutoConfiguration handles the JavaMailSender bean.
    // Override here if you need a custom SMTP provider or API-based email service.
}
