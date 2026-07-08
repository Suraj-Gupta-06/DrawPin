package com.drawpin.repository;

import com.drawpin.domain.entity.Media;
import com.drawpin.domain.enums.MediaStatus;
import com.drawpin.domain.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    Optional<Media> findByIdAndStatusNot(UUID id, MediaStatus status);

    Page<Media> findByOwnerIdAndStatusNot(UUID ownerId, MediaStatus status, Pageable pageable);

    Page<Media> findByOwnerIdAndMediaTypeAndStatusNot(UUID ownerId, MediaType mediaType, MediaStatus status, Pageable pageable);

    /**
     * Used for duplicate detection before upload.
     */
    Optional<Media> findByOwnerIdAndChecksumAndStatus(UUID ownerId, String checksum, MediaStatus status);
}
