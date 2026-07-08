package com.drawpin.service.media;

import com.drawpin.domain.entity.Media;
import com.drawpin.domain.entity.User;
import com.drawpin.domain.enums.MediaStatus;
import com.drawpin.domain.enums.MediaType;
import com.drawpin.dto.response.media.MediaUploadResponse;
import com.drawpin.exception.ValidationException;
import com.drawpin.mapper.MediaMapper;
import com.drawpin.repository.MediaRepository;
import com.drawpin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MediaMapper mediaMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MediaService mediaService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void uploadMedia_ValidFile_UploadsSuccessfully() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        UUID ownerId = owner.getId();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(mediaRepository.findByOwnerIdAndChecksumAndStatus(eq(ownerId), anyString(), eq(MediaStatus.READY)))
                .thenReturn(Optional.empty());

        Map<String, Object> uploadResult = Map.of(
                "public_id", "pub_id_123",
                "secure_url", "https://example.com/pub_id_123.jpg"
        );
        when(fileStorageService.upload(any(), eq("avatars"))).thenReturn(uploadResult);
        
        Media savedMedia = Media.builder().id(UUID.randomUUID()).build();
        when(mediaRepository.save(any(Media.class))).thenReturn(savedMedia);
        
        MediaUploadResponse expectedResponse = MediaUploadResponse.builder().id(savedMedia.getId()).build();
        when(mediaMapper.toUploadResponse(any())).thenReturn(expectedResponse);

        MediaUploadResponse response = mediaService.uploadMedia(ownerId, file, MediaType.AVATAR);

        assertNotNull(response);
        verify(fileStorageService).upload(file, "avatars");
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void uploadMedia_DuplicateChecksum_ReturnsExistingMedia() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        UUID ownerId = owner.getId();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        
        Media existingMedia = Media.builder().id(UUID.randomUUID()).build();
        when(mediaRepository.findByOwnerIdAndChecksumAndStatus(eq(ownerId), anyString(), eq(MediaStatus.READY)))
                .thenReturn(Optional.of(existingMedia));
                
        MediaUploadResponse expectedResponse = MediaUploadResponse.builder().id(existingMedia.getId()).build();
        when(mediaMapper.toUploadResponse(existingMedia)).thenReturn(expectedResponse);

        MediaUploadResponse response = mediaService.uploadMedia(ownerId, file, MediaType.AVATAR);

        assertEquals(expectedResponse.getId(), response.getId());
        verify(fileStorageService, never()).upload(any(), anyString());
        verify(mediaRepository, never()).save(any());
    }

    @Test
    void uploadMedia_InvalidMimeType_ThrowsValidationException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());

        ValidationException ex = assertThrows(ValidationException.class, 
                () -> mediaService.uploadMedia(owner.getId(), file, MediaType.AVATAR));
        
        assertEquals("Only images and videos are supported", ex.getMessage());
    }
}
