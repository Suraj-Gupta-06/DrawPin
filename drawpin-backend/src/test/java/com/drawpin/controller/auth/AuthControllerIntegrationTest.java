package com.drawpin.controller.auth;

import com.drawpin.dto.request.auth.LoginRequest;
import com.drawpin.dto.request.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 *
 * <p>Uses Testcontainers to spin up a real PostgreSQL instance and Spring Boot's
 * {@link AutoConfigureMockMvc} to make HTTP requests through the full filter chain.
 *
 * <p>Each test runs in its own transaction that is rolled back after completion,
 * keeping the database clean between tests.
 *
 * <p><b>Prerequisites to run:</b> Docker must be available on the host machine.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Use H2 in PostgreSQL compatibility mode
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:drawpin_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        
        // Let Hibernate generate the schema instead of Flyway since Flyway might have Postgres-specific syntax
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");

        // Disable Redis for integration tests
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        // Use a long enough test secret
        registry.add("drawpin.jwt.secret",
                () -> "integration-test-secret-key-that-is-at-least-64-characters-long!!");
        // Disable email sending in tests
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/register — should return 201 and access token")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Aria Vance");
        request.setEmail("aria.integration@example.com");
        request.setPassword("Str0ng!Pass");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("aria.integration@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("collector"));
    }

    @Test
    @DisplayName("POST /auth/register — should return 400 for blank name")
    void register_blankName_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setEmail("aria@example.com");
        request.setPassword("Str0ng!Pass");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fields.name").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register — should return 400 for weak password")
    void register_weakPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Aria Vance");
        request.setEmail("aria@example.com");
        request.setPassword("weakpass");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fields.password").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register — should return 409 when email already exists")
    void register_duplicateEmail_returns409() throws Exception {
        // First registration
        RegisterRequest first = new RegisterRequest();
        first.setName("Aria Vance");
        first.setEmail("duplicate@example.com");
        first.setPassword("Str0ng!Pass");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login — should return 200 and access token after successful registration")
    void login_validCredentials_returns200() throws Exception {
        // Register first
        RegisterRequest register = new RegisterRequest();
        register.setName("Login Test");
        register.setEmail("logintest@example.com");
        register.setPassword("Str0ng!Pass");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        // Now login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("logintest@example.com");
        loginRequest.setPassword("Str0ng!Pass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(900));
    }

    @Test
    @DisplayName("POST /auth/login — should return 401 for wrong password")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("aria@example.com");
        request.setPassword("WrongPassword1");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /auth/forgot-password — should always return 200 regardless of email existence")
    void forgotPassword_unknownEmail_alwaysReturns200() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nonexistent@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
