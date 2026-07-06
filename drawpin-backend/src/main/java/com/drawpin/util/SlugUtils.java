package com.drawpin.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-safe slug and handle strings.
 *
 * <p>Used during user registration to auto-generate a unique {@code handle}
 * from the user's display name. If the generated handle already exists, a
 * numeric suffix is appended ({@code aria.vance2}, {@code aria.vance3}, etc.).
 *
 * <p><b>Example:</b>
 * <pre>
 *   SlugUtils.toHandle("Aria Vance")  →  "aria.vance"
 *   SlugUtils.toHandle("José María")  →  "jose.maria"
 *   SlugUtils.toHandle("  Bold!!!")   →  "bold"
 * </pre>
 *
 * <p>This is a stateless utility class and should not be instantiated.
 * All methods are static.
 */
public final class SlugUtils {

    /** Pattern that matches any character that is not a lowercase letter, digit, or dot. */
    private static final Pattern NON_SLUG_CHAR = Pattern.compile("[^a-z0-9.]");

    /** Pattern that matches multiple consecutive dots. */
    private static final Pattern MULTIPLE_DOTS = Pattern.compile("\\.{2,}");

    /** Pattern that matches leading or trailing dots. */
    private static final Pattern LEADING_TRAILING_DOTS = Pattern.compile("^\\.|\\.$");

    private SlugUtils() {
        // Utility class — not instantiable
    }

    /**
     * Converts a display name into a URL-safe handle.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Normalize Unicode characters (NFD decomposition → strip diacritics)</li>
     *   <li>Convert to lowercase</li>
     *   <li>Replace spaces with dots</li>
     *   <li>Remove any character that is not [a-z0-9.]</li>
     *   <li>Collapse multiple consecutive dots into one</li>
     *   <li>Strip leading/trailing dots</li>
     *   <li>Truncate to 40 characters</li>
     * </ol>
     *
     * @param displayName the raw name string (e.g., "Aria Vance")
     * @return a lowercase dot-separated handle safe for use in URLs
     */
    public static String toHandle(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "user";
        }

        // Strip diacritics (e.g., é → e)
        String normalized = Normalizer.normalize(displayName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        String slug = normalized
                .toLowerCase(Locale.ROOT)
                .trim()
                .replace(' ', '.')
                .replace('-', '.');

        slug = NON_SLUG_CHAR.matcher(slug).replaceAll("");
        slug = MULTIPLE_DOTS.matcher(slug).replaceAll(".");
        slug = LEADING_TRAILING_DOTS.matcher(slug).replaceAll("");

        // Truncate to max handle length (40 chars per DB constraint)
        if (slug.length() > 40) {
            slug = slug.substring(0, 40);
            // Remove trailing dot introduced by truncation
            slug = LEADING_TRAILING_DOTS.matcher(slug).replaceAll("");
        }

        return slug.isEmpty() ? "user" : slug;
    }

    /**
     * Appends a numeric suffix to a handle to make it unique.
     * Called when the generated handle is already taken.
     *
     * @param baseHandle the handle without a suffix (e.g., "aria.vance")
     * @param suffix     the numeric suffix to append (e.g., 2 → "aria.vance2")
     * @return the handle with the suffix, truncated to 40 characters
     */
    public static String withSuffix(String baseHandle, int suffix) {
        String suffixStr = String.valueOf(suffix);
        int maxBase = 40 - suffixStr.length();
        String truncated = baseHandle.length() > maxBase
                ? baseHandle.substring(0, maxBase)
                : baseHandle;
        return truncated + suffixStr;
    }
}
