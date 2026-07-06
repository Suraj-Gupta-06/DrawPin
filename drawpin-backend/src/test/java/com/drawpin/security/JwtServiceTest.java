package com.drawpin.security;

import com.drawpin.config.JwtConfig;
import com.drawpin.domain.entity.User;
import com.drawpin.domain.enums.UserRole;
import com.drawpin.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtTokenProvider}.
 *
 * <p>Tests JWT generation and validation without mocking the provider itself
 * — uses a real instance with a test secret to verify actual cryptographic behaviour.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Tests")
class JwtServiceTest {

    private static final String TEST_SECRET =
            "test-secret-key-that-is-at-least-64-characters-long-for-hs512-algorithm-requirement!!";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setSecret(TEST_SECRET);
        config.setAccessTokenExpirySeconds(900L);

        jwtTokenProvider = new JwtTokenProvider(config);
        jwtTokenProvider.init(); // Trigger @PostConstruct
    }

    @Test
    @DisplayName("Should generate a valid JWT token for a user")
    void generateToken_validUser_returnsToken() {
        // Arrange
        User user = buildUser();

        // Act
        String token = jwtTokenProvider.generateAccessToken(user);

        // Assert
        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("Should extract the correct user ID from a generated token")
    void extractUserId_fromGeneratedToken_returnsCorrectId() {
        // Arrange
        User user = buildUser();
        String token = jwtTokenProvider.generateAccessToken(user);

        // Act
        UUID extractedId = jwtTokenProvider.extractUserId(token);

        // Assert
        assertThat(extractedId).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should extract the correct email from a generated token")
    void extractEmail_fromGeneratedToken_returnsCorrectEmail() {
        // Arrange
        User user = buildUser();
        String token = jwtTokenProvider.generateAccessToken(user);

        // Act
        String extractedEmail = jwtTokenProvider.extractEmail(token);

        // Assert
        assertThat(extractedEmail).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Should validate a freshly generated token as VALID")
    void validate_freshToken_returnsValid() {
        // Arrange
        User user = buildUser();
        String token = jwtTokenProvider.generateAccessToken(user);

        // Act
        JwtTokenProvider.JwtValidationResult result = jwtTokenProvider.validate(token);

        // Assert
        assertThat(result).isEqualTo(JwtTokenProvider.JwtValidationResult.VALID);
    }

    @Test
    @DisplayName("Should return MALFORMED for an invalid token string")
    void validate_malformedToken_returnsMalformed() {
        // Act
        JwtTokenProvider.JwtValidationResult result = jwtTokenProvider.validate("not.a.valid.jwt.token");

        // Assert
        assertThat(result).isEqualTo(JwtTokenProvider.JwtValidationResult.MALFORMED);
    }

    @Test
    @DisplayName("Should return INVALID_SIGNATURE for token signed with a different key")
    void validate_wrongSignature_returnsInvalidSignature() {
        // Create a token with a different secret
        JwtConfig otherConfig = new JwtConfig();
        otherConfig.setSecret("a-completely-different-secret-key-that-is-also-64-chars-long!!!!!!!");
        otherConfig.setAccessTokenExpirySeconds(900L);

        JwtTokenProvider otherProvider = new JwtTokenProvider(otherConfig);
        otherProvider.init();

        User user = buildUser();
        String tokenFromOtherKey = otherProvider.generateAccessToken(user);

        // Act — validate with the original provider (different key)
        JwtTokenProvider.JwtValidationResult result = jwtTokenProvider.validate(tokenFromOtherKey);

        // Assert
        assertThat(result).isEqualTo(JwtTokenProvider.JwtValidationResult.INVALID_SIGNATURE);
    }

    @Test
    @DisplayName("Should return the configured expiry in seconds")
    void getAccessTokenExpirySeconds_returnsConfiguredValue() {
        assertThat(jwtTokenProvider.getAccessTokenExpirySeconds()).isEqualTo(900L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("aria@example.com")
                .passwordHash("$2a$12$hashed")
                .name("Aria Vance")
                .handle("aria.vance")
                .role(UserRole.CREATOR)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
