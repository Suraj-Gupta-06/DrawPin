package com.drawpin.domain.entity;

import com.drawpin.domain.enums.MediaStatus;
import com.drawpin.domain.enums.MediaType;
import com.drawpin.domain.enums.StorageProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity representing a Media asset in the DrawPin platform.
 *
 * <p>The Media Foundation is storage-provider independent. It serves as the
 * central repository for all images, videos, and documents across the platform.
 *
 * <p><b>Table:</b> {@code media}<br>
 * <b>Managed by Flyway:</b> {@code V6__create_media.sql}
 *
 * <p><b>Important Architectural Notes:</b>
 * <ul>
 *   <li>Uses {@code @Getter}/{@code @Setter} instead of {@code @Data}.</li>
 *   <li>Includes checksum for duplicate detection.</li>
 *   <li>Includes soft-delete tracking via {@code deletedAt} and {@code status}.</li>
 * </ul>
 */
@Entity
@Table(name = "media")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 50)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, length = 50)
    private StorageProvider storageProvider;

    @Column(name = "public_id", nullable = false, length = 255)
    private String publicId;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_extension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "aspect_ratio")
    private Double aspectRatio;

    @Column(name = "orientation", length = 50)
    private String orientation;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "secure_url", nullable = false, columnDefinition = "TEXT")
    private String secureUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "folder", nullable = false, length = 100)
    private String folder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MediaStatus status;

    @Column(name = "checksum", nullable = false, length = 255)
    private String checksum;

    // --- Auditing & Soft Delete ---

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // --- Identity-based equals & hashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Media media)) return false;
        return id != null && Objects.equals(id, media.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
