package com.drawpin.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating cryptographically random tokens.
 *
 * <p>Used to produce the raw token strings that are:
 * <ul>
 *   <li>Sent to users in password reset and email verification emails</li>
 *   <li>Set as HttpOnly cookie values for refresh tokens</li>
 * </ul>
 *
 * <p>Tokens are generated using {@link SecureRandom} (CSPRNG) and Base64 URL-encoded
 * to be safe for use in URLs and cookies without additional encoding.
 *
 * <p>The raw token is NEVER stored in the database. Only the SHA-256 hash
 * (produced by {@link HashUtils}) is persisted.
 *
 * <p>This is a stateless utility class. All methods are static.
 */
public final class TokenUtils {

    /** Default token length in bytes. 32 bytes → 43 Base64URL characters. */
    private static final int DEFAULT_BYTE_LENGTH = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenUtils() {
        // Utility class — not instantiable
    }

    /**
     * Generates a cryptographically random URL-safe token using the default length (32 bytes).
     *
     * @return a 43-character Base64URL-encoded token string
     */
    public static String generate() {
        return generate(DEFAULT_BYTE_LENGTH);
    }

    /**
     * Generates a cryptographically random URL-safe token of the specified byte length.
     *
     * @param byteLength the number of random bytes to generate (must be &gt; 0)
     * @return a Base64URL-encoded token string
     * @throws IllegalArgumentException if {@code byteLength} is not positive
     */
    public static String generate(int byteLength) {
        if (byteLength <= 0) {
            throw new IllegalArgumentException("byteLength must be positive, got: " + byteLength);
        }
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
