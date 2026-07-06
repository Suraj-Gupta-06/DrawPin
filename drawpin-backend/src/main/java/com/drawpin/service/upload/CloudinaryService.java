package com.drawpin.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads an image to Cloudinary.
     *
     * @param file the multipart file to upload
     * @param folder the folder in Cloudinary to store the image
     * @return the secure URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map params = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image"
        );
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Deletes an image from Cloudinary by its public ID.
     * The public ID is typically extracted from the URL.
     *
     * @param imageUrl the full Cloudinary URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        
        try {
            // Extract public ID from the URL (very basic extraction)
            // e.g. https://res.cloudinary.com/.../image/upload/v1234/folder/public_id.jpg
            String[] parts = imageUrl.split("/");
            String fileWithExt = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            String publicId = folder + "/" + fileWithExt.split("\\.")[0];
            
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.debug("Deleted image from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary: {}", imageUrl, e);
        }
    }
}
