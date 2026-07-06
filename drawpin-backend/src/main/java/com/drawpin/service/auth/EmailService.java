package com.drawpin.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service responsible for sending all transactional emails.
 *
 * <p>All public methods are annotated with {@code @Async} to dispatch emails on
 * a background thread (via the {@link com.drawpin.config.AsyncConfig} executor),
 * preventing email delivery from blocking the HTTP request thread.
 *
 * <p>Each email method composes HTML content and delegates to {@link JavaMailSender}.
 * In production, the SMTP credentials are provided via environment variables.
 *
 * <p><b>Emails sent by this service:</b>
 * <ul>
 *   <li>Email verification link (sent after registration)</li>
 *   <li>Password reset link (sent after forgot-password request)</li>
 *   <li>Password changed confirmation (sent after change/reset)</li>
 * </ul>
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.service.auth.EmailVerificationService} — requests verification email</li>
 *   <li>{@link com.drawpin.service.auth.PasswordResetService} — requests reset email</li>
 *   <li>{@link com.drawpin.service.auth.AuthService} — requests confirmation email</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@drawpin.com}")
    private String fromAddress;

    @Value("${drawpin.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Sends an email verification link to a newly registered user.
     *
     * <p>The link points to the frontend's email verification page with the raw token
     * as a query parameter. The frontend extracts the token and calls
     * {@code POST /api/v1/auth/verify-email}.
     *
     * @param toEmail   recipient email address
     * @param userName  recipient's display name (for personalisation)
     * @param rawToken  the raw verification token (embedded in the link URL)
     */
    @Async
    public void sendVerificationEmail(String toEmail, String userName, String rawToken) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + rawToken;
        String subject = "Verify your DrawPin email address";
        String body = buildVerificationEmailHtml(userName, verifyUrl);
        sendHtmlEmail(toEmail, subject, body);
        log.info("Verification email sent to {}", toEmail);
    }

    /**
     * Sends a password reset link to the user's email address.
     *
     * <p>The link points to the frontend's reset-password page. The raw token
     * is embedded as a query parameter for the frontend to extract and submit.
     *
     * @param toEmail  recipient email address
     * @param userName recipient's display name
     * @param rawToken the raw reset token
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String rawToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + rawToken;
        String subject = "Reset your DrawPin password";
        String body = buildPasswordResetEmailHtml(userName, resetUrl);
        sendHtmlEmail(toEmail, subject, body);
        log.info("Password reset email sent to {}", toEmail);
    }

    /**
     * Sends a security notification informing the user their password was changed.
     * This is sent both for self-service changes and admin-triggered resets.
     *
     * @param toEmail  recipient email address
     * @param userName recipient's display name
     */
    @Async
    public void sendPasswordChangedEmail(String toEmail, String userName) {
        String subject = "Your DrawPin password was changed";
        String body = buildPasswordChangedEmailHtml(userName);
        sendHtmlEmail(toEmail, subject, body);
        log.info("Password changed notification sent to {}", toEmail);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sends an HTML email via the configured JavaMailSender.
     * Failures are logged as errors but do NOT propagate to the caller — email
     * delivery is best-effort and should not block the primary flow.
     *
     * @param to      recipient address
     * @param subject email subject line
     * @param htmlBody the full HTML email body
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "DrawPin");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    /** Builds the HTML body for the email verification email. */
    private String buildVerificationEmailHtml(String userName, String verifyUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Inter, Arial, sans-serif; background: #0f0f17; color: #e2e8f0; padding: 40px;">
                  <div style="max-width: 560px; margin: 0 auto; background: #1a1a2e; border-radius: 16px; padding: 40px;">
                    <h1 style="color: #a855f7; margin-bottom: 8px;">DrawPin</h1>
                    <h2 style="font-size: 24px; color: #ffffff;">Welcome, %s!</h2>
                    <p>Please verify your email address to activate your account.</p>
                    <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #a855f7, #6366f1);
                       color: white; text-decoration: none; padding: 14px 28px; border-radius: 8px;
                       font-weight: 600; margin: 24px 0;">Verify Email Address</a>
                    <p style="color: #94a3b8; font-size: 14px;">This link expires in 24 hours.</p>
                    <hr style="border-color: #334155; margin: 24px 0;">
                    <p style="color: #64748b; font-size: 12px;">If you did not create a DrawPin account, please ignore this email.</p>
                  </div>
                </body>
                </html>
                """.formatted(userName, verifyUrl);
    }

    /** Builds the HTML body for the password reset email. */
    private String buildPasswordResetEmailHtml(String userName, String resetUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Inter, Arial, sans-serif; background: #0f0f17; color: #e2e8f0; padding: 40px;">
                  <div style="max-width: 560px; margin: 0 auto; background: #1a1a2e; border-radius: 16px; padding: 40px;">
                    <h1 style="color: #a855f7; margin-bottom: 8px;">DrawPin</h1>
                    <h2 style="font-size: 24px; color: #ffffff;">Password Reset Request</h2>
                    <p>Hi %s, we received a request to reset your DrawPin password.</p>
                    <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #a855f7, #6366f1);
                       color: white; text-decoration: none; padding: 14px 28px; border-radius: 8px;
                       font-weight: 600; margin: 24px 0;">Reset My Password</a>
                    <p style="color: #94a3b8; font-size: 14px;">This link expires in 1 hour.</p>
                    <hr style="border-color: #334155; margin: 24px 0;">
                    <p style="color: #64748b; font-size: 12px;">If you did not request a password reset, your account may be at risk. Please contact support immediately.</p>
                  </div>
                </body>
                </html>
                """.formatted(userName, resetUrl);
    }

    /** Builds the HTML body for the password-changed security notification. */
    private String buildPasswordChangedEmailHtml(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Inter, Arial, sans-serif; background: #0f0f17; color: #e2e8f0; padding: 40px;">
                  <div style="max-width: 560px; margin: 0 auto; background: #1a1a2e; border-radius: 16px; padding: 40px;">
                    <h1 style="color: #a855f7; margin-bottom: 8px;">DrawPin</h1>
                    <h2 style="font-size: 24px; color: #ffffff;">Password Changed</h2>
                    <p>Hi %s, your DrawPin password was successfully changed.</p>
                    <p>If you did not make this change, please <a href="%s/contact" style="color: #a855f7;">contact us immediately</a>.</p>
                    <hr style="border-color: #334155; margin: 24px 0;">
                    <p style="color: #64748b; font-size: 12px;">This is an automated security notification from DrawPin.</p>
                  </div>
                </body>
                </html>
                """.formatted(userName, frontendUrl);
    }
}
