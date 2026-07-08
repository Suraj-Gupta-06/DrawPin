package com.drawpin.service.media;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Agnostic interface for file storage operations.
 * 
 * <p>This abstraction ensures that the business logic remains independent
 * of any specific storage provider (e.g., Cloudinary, AWS S3).
 */
public interface FileStorageService {

    /**
     * Uploads a file to the storage provider.
     *
     * @param file   The file to upload
     * @param folder The target folder path in the storage provider
     * @return A map containing provider-specific metadata (must include at least "public_id", "secure_url", "format", "bytes")
     */
    Map<String, Object> upload(MultipartFile file, String folder);

    /**
     * Deletes a file from the storage provider.
     *
     * @param publicId The provider's unique identifier for the file
     */
    void delete(String publicId);

    /**
     * Generates a transformed/optimized URL if supported by the provider.
     *
     * @param publicId The provider's unique identifier
     * @param width    Desired width
     * @param height   Desired height
     * @param cropMode Crop mode (e.g., "fill", "scale")
     * @return The generated URL, or the original secure URL if transformations are unsupported
     */
    String generateOptimizedUrl(String publicId, int width, int height, String cropMode);
}
