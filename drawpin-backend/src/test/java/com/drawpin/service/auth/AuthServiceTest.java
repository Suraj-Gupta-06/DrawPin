package com.drawpin.service.auth;

import com.drawpin.domain.entity.User;
import com.drawpin.domain.entity.UserSettings;
import com.drawpin.domain.enums.UserRole;
import com.drawpin.domain.enums.UserStatus;
import com.drawpin.dto.request.auth.ChangePasswordRequest;
import com.drawpin.dto.request.auth.LoginRequest;
import com.drawpin.dto.request.auth.RegisterRequest;
import com.drawpin.dto.response.AuthResponse;
import com.drawpin.dto.response.AuthResult;
import com.drawpin.exception.ConflictException;
import com.drawpin.exception.UnauthorizedException;
import com.drawpin.exception.ValidationException;
import com.drawpin.mapper.UserMapper;
import com.drawpin.repository.CreatorRepository;
import com.drawpin.repository.UserRepository;
import com.drawpin.repository.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>Uses Mockito to isolate the service from its dependencies.
 * All collaborators are mocked — no database or Spring context is started.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserSettingsRepository userSettingsRepository;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private EmailVerificationService emailVerificationService;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;

    @Mock
    private CreatorRepository creatorRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Inject @Value fields that Spring would normally populate
        ReflectionTestUtils.setField(authService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMinutes", 15);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRATION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should create a new user when email is not taken")
        void register_success() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setName("Aria Vance");
            request.setEmail("aria@example.com");
            request.setPassword("Str0ng!Pass");

            User savedUser = buildUser();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByHandle(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(new UserSettings());
            when(refreshTokenService.issueToken(any(), any(), any(), anyBoolean())).thenReturn("raw-token");
            when(jwtService.buildAuthResponse(any(), any())).thenReturn(buildAuthResponse());

            // Act
            AuthResult result = authService.register(request, "Chrome/Windows", "127.0.0.1");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAuthResponse().getAccessToken()).isEqualTo("test-access-token");
            assertThat(result.getRawRefreshToken()).isEqualTo("raw-token");
            verify(userRepository).save(any(User.class));
            verify(userSettingsRepository).save(any(UserSettings.class));
            verify(emailVerificationService).sendVerificationEmail(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw ConflictException when email already exists")
        void register_emailAlreadyExists_throwsConflict() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setName("Aria Vance");
            request.setEmail("existing@example.com");
            request.setPassword("Str0ng!Pass");

            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request, "Chrome", "127.0.0.1"))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should default to COLLECTOR role when no role specified")
        void register_noRole_defaultsToCollector() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setName("Test User");
            request.setEmail("test@example.com");
            request.setPassword("Str0ng!Pass");
            request.setRole(null);

            User savedUser = buildUser();
            savedUser.setRole(UserRole.COLLECTOR);

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByHandle(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                assertThat(u.getRole()).isEqualTo(UserRole.COLLECTOR);
                return savedUser;
            });
            when(userSettingsRepository.save(any())).thenReturn(new UserSettings());
            when(refreshTokenService.issueToken(any(), any(), any(), anyBoolean())).thenReturn("token");
            when(jwtService.buildAuthResponse(any(), any())).thenReturn(buildAuthResponse());

            // Act
            authService.register(request, "Chrome", "127.0.0.1");

            // Assert: verified in the save() stub above
            verify(userRepository).save(any(User.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should return AuthResponse on valid credentials")
        void login_success() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setEmail("aria@example.com");
            request.setPassword("Str0ng!Pass");

            User user = buildUser();

            when(userRepository.findByEmailAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(refreshTokenService.issueToken(any(), any(), any(), anyBoolean())).thenReturn("raw-token");
            when(jwtService.buildAuthResponse(any(), any())).thenReturn(buildAuthResponse());

            // Act
            AuthResult result = authService.login(request, "Chrome", "127.0.0.1");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAuthResponse().getAccessToken()).isEqualTo("test-access-token");
            assertThat(result.getRawRefreshToken()).isEqualTo("raw-token");
            verify(userRepository).resetLoginFailures(user.getId());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException on wrong password")
        void login_wrongPassword_throwsUnauthorized() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setEmail("aria@example.com");
            request.setPassword("WrongPass");

            User user = buildUser();

            when(userRepository.findByEmailAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request, "Chrome", "127.0.0.1"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("incorrect");

            verify(userRepository).incrementFailedLoginCount(user.getId());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException for unknown email")
        void login_unknownEmail_throwsUnauthorized() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setEmail("unknown@example.com");
            request.setPassword("Pass");

            when(userRepository.findByEmailAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request, "Chrome", "127.0.0.1"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException for suspended account")
        void login_suspendedAccount_throwsUnauthorized() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setEmail("aria@example.com");
            request.setPassword("Str0ng!Pass");

            User user = buildUser();
            user.setStatus(UserStatus.SUSPENDED);

            when(userRepository.findByEmailAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request, "Chrome", "127.0.0.1"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("suspended");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE PASSWORD TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password and revoke all sessions on success")
        void changePassword_success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("OldPass!1");
            request.setNewPassword("NewPass!2");

            User user = buildUser();
            user.setPasswordHash("$2a$old-hashed");

            when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("OldPass!1", "$2a$old-hashed")).thenReturn(true);
            when(passwordEncoder.matches("NewPass!2", "$2a$old-hashed")).thenReturn(false);
            when(passwordEncoder.encode("NewPass!2")).thenReturn("$2a$new-hashed");
            when(userRepository.save(any())).thenReturn(user);

            // Act
            authService.changePassword(userId, request);

            // Assert
            verify(refreshTokenService).revokeAllTokensForUser(userId);
            verify(emailService).sendPasswordChangedEmail(user.getEmail(), user.getName());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when current password is wrong")
        void changePassword_wrongCurrentPassword_throwsUnauthorized() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("WrongOld");
            request.setNewPassword("NewPass!2");

            User user = buildUser();

            when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WrongOld", user.getPasswordHash())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(userId, request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("incorrect");
        }

        @Test
        @DisplayName("Should throw ValidationException when new password equals current")
        void changePassword_sameAsCurrentPassword_throwsValidation() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("SamePass!1");
            request.setNewPassword("SamePass!1");

            User user = buildUser();

            when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(userId, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("different");
        }
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
                .role(UserRole.COLLECTOR)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
    }

    private AuthResponse buildAuthResponse() {
        return AuthResponse.builder()
                .accessToken("test-access-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();
    }
}
