package com.drawpin.service.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.drawpin.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Cloudinary implementation of the {@link FileStorageService}.
 * 
 * <p>This service encapsulates all interactions with the Cloudinary SDK.
 * DrawPin business logic must NEVER use Cloudinary SDK classes directly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, Object> upload(MultipartFile file, String folder) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto",
                    "use_filename", true,
                    "unique_filename", true
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return uploadResult;
            
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
            throw new InternalServerException("STORAGE_ERROR", "Failed to upload file to storage provider");
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            // Cloudinary requires resource_type for videos/raw, but we default to image here. 
            // In a fully robust implementation, we might need to pass resource_type or try both.
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted asset from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
            throw new InternalServerException("STORAGE_ERROR", "Failed to delete file from storage provider");
        }
    }

    @Override
    public String generateOptimizedUrl(String publicId, int width, int height, String cropMode) {
        try {
            return cloudinary.url()
                    .transformation(new Transformation<>()
                            .width(width)
                            .height(height)
                            .crop(cropMode)
                            .fetchFormat("auto")
                            .quality("auto"))
                    .generate(publicId);
        } catch (Exception e) {
            log.warn("Failed to generate optimized URL for {}, returning unoptimized", publicId);
            return cloudinary.url().generate(publicId);
        }
    }
}
