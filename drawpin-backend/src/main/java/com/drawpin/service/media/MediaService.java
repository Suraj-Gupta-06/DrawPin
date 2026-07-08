package com.drawpin.service.media;

import com.drawpin.domain.entity.Media;
import com.drawpin.domain.entity.User;
import com.drawpin.domain.enums.MediaStatus;
import com.drawpin.domain.enums.MediaType;
import com.drawpin.domain.enums.StorageProvider;
import com.drawpin.dto.response.media.MediaResponse;
import com.drawpin.dto.response.media.MediaUploadResponse;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.exception.UnauthorizedException;
import com.drawpin.exception.ValidationException;
import com.drawpin.exception.InternalServerException;
import com.drawpin.mapper.MediaMapper;
import com.drawpin.repository.MediaRepository;
import com.drawpin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;
    private final MediaMapper mediaMapper;
    private final UserRepository userRepository;

    // Constraints
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_IMAGE_DIMENSION = 4096;

    /**
     * Uploads a new media file.
     * Prevents duplicate uploads by checking the SHA-256 checksum against existing active media for the owner.
     */
    public MediaUploadResponse uploadMedia(UUID ownerId, MultipartFile file, MediaType mediaType) {
        validateFile(file);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Owner not found"));

        String checksum = calculateChecksum(file);

        // Duplicate Detection
        Optional<Media> existingMedia = mediaRepository.findByOwnerIdAndChecksumAndStatus(ownerId, checksum, MediaStatus.READY);
        if (existingMedia.isPresent()) {
            log.info("Duplicate media detected for owner {} with checksum {}. Returning existing record.", ownerId, checksum);
            return mediaMapper.toUploadResponse(existingMedia.get());
        }

        // Determine folder based on media type
        String folder = getFolderForMediaType(mediaType);

        // Execute upload via StorageService abstraction
        Map<String, Object> uploadResult = fileStorageService.upload(file, folder);

        // Extract image dimensions if it's an image
        int width = 0;
        int height = 0;
        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            try (InputStream is = file.getInputStream()) {
                BufferedImage image = ImageIO.read(is);
                if (image != null) {
                    width = image.getWidth();
                    height = image.getHeight();
                }
            } catch (Exception e) {
                log.warn("Could not extract image dimensions for {}: {}", file.getOriginalFilename(), e.getMessage());
            }
        }

        // Optionally, Cloudinary returns width/height in uploadResult if it's an image.
        if (width == 0 && uploadResult.containsKey("width")) {
            width = (Integer) uploadResult.get("width");
        }
        if (height == 0 && uploadResult.containsKey("height")) {
            height = (Integer) uploadResult.get("height");
        }

        double aspectRatio = (height > 0) ? (double) width / height : 0.0;
        String orientation = null;
        if (width > 0 && height > 0) {
            if (width > height) orientation = "landscape";
            else if (width < height) orientation = "portrait";
            else orientation = "square";
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());

        Media media = Media.builder()
                .owner(owner)
                .mediaType(mediaType)
                .storageProvider(StorageProvider.CLOUDINARY)
                .publicId((String) uploadResult.get("public_id"))
                .originalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown")
                .mimeType(file.getContentType())
                .fileExtension(fileExtension)
                .fileSize(file.getSize())
                .width(width > 0 ? width : null)
                .height(height > 0 ? height : null)
                .aspectRatio(aspectRatio > 0 ? aspectRatio : null)
                .orientation(orientation)
                .secureUrl((String) uploadResult.get("secure_url"))
                .folder(folder)
                .status(MediaStatus.READY)
                .checksum(checksum)
                .build();

        media = mediaRepository.save(media);
        log.info("AUDIT: Media uploaded and saved with ID {}", media.getId());

        return mediaMapper.toUploadResponse(media);
    }

    /**
     * Soft deletes media. Does NOT delete from storage provider immediately.
     */
    public void deleteMedia(UUID mediaId, UUID requestorId) {
        Media media = getMediaEntity(mediaId);
        
        verifyOwnership(media, requestorId);

        media.setStatus(MediaStatus.DELETED);
        media.setDeletedAt(java.time.Instant.now());
        
        mediaRepository.save(media);
        log.info("AUDIT: Media {} soft-deleted by user {}", mediaId, requestorId);
    }

    @Transactional(readOnly = true)
    public MediaResponse getMediaById(UUID mediaId) {
        Media media = mediaRepository.findByIdAndStatusNot(mediaId, MediaStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MEDIA_NOT_FOUND", "Media not found or deleted"));
        return mediaMapper.toResponse(media);
    }

    // --- Helpers ---

    private Media getMediaEntity(UUID id) {
        return mediaRepository.findByIdAndStatusNot(id, MediaStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MEDIA_NOT_FOUND", "Media not found"));
    }

    private void verifyOwnership(Media media, UUID requestorId) {
        if (!media.getOwner().getId().equals(requestorId)) {
            // Note: Admin check would normally go here, handled upstream by controller roles ideally
            throw new UnauthorizedException("ACCESS_DENIED", "You do not have permission to modify this media");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("EMPTY_FILE", "File cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("FILE_TOO_LARGE", "File size exceeds the 10MB limit");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
            throw new ValidationException("INVALID_TYPE", "Only images and videos are supported");
        }
    }

    private String getFolderForMediaType(MediaType mediaType) {
        if (mediaType == null) return "misc";
        if (mediaType == MediaType.AVATAR) return "avatars";
        if (mediaType == MediaType.PIN_IMAGE) return "pins";
        if (mediaType == MediaType.PORTFOLIO_IMAGE) return "portfolio";
        if (mediaType == MediaType.MESSAGE_ATTACHMENT) return "messages";
        if (mediaType == MediaType.REVIEW_IMAGE) return "reviews";
        return "misc";
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String calculateChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new InternalServerException("CHECKSUM_ERROR", "Failed to calculate file checksum");
        }
    }
}
