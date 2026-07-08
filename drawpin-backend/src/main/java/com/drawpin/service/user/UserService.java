package com.drawpin.service.user;

import com.drawpin.domain.entity.User;
import com.drawpin.dto.request.user.ChangeEmailRequest;
import com.drawpin.dto.request.user.UpdateProfileRequest;
import com.drawpin.dto.response.UserResponse;
import com.drawpin.exception.ConflictException;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.exception.UnauthorizedException;
import com.drawpin.mapper.UserMapper;
import com.drawpin.repository.UserRepository;
import com.drawpin.service.auth.EmailVerificationService;
import com.drawpin.service.media.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import com.drawpin.domain.entity.Creator;
import com.drawpin.repository.CreatorRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CreatorRepository creatorRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    /**
     * Updates the user's public profile fields.
     */
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        user.setName(request.getName().trim());
        user.setBio(request.getBio() != null ? request.getBio().trim() : null);
        user.setCity(request.getCity() != null ? request.getCity().trim() : null);
        user.setWebsite(request.getWebsite() != null ? request.getWebsite().trim() : null);
        
        user = userRepository.save(user);
        log.info("AUDIT: Profile updated for user {}", userId);
        return mapToUserResponse(user);
    }

    /**
     * Retrieves a user's public profile by their unique handle.
     */
    @Transactional(readOnly = true)
    public UserResponse getPublicProfile(String handle) {
        User user = userRepository.findByHandleAndDeletedAtIsNull(handle)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Profile not found."));
        if (user.isDeleted()) {
            throw new ResourceNotFoundException("USER_NOT_FOUND", "Profile no longer exists.");
        }
        return mapToUserResponse(user);
    }

    /**
     * Retrieves a user's profile by their UUID.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return mapToUserResponse(getUser(userId));
    }

    /**
     * Initiates the change email workflow.
     * Sets the pendingEmail and sends a verification email.
     */
    public void requestEmailChange(UUID userId, ChangeEmailRequest request) {
        User user = getUser(userId);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("INVALID_PASSWORD", "Password confirmation failed.");
        }

        String newEmail = request.getNewEmail().toLowerCase().trim();
        
        if (newEmail.equals(user.getEmail())) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "This is already your current email address.");
        }
        
        if (userRepository.existsByEmail(newEmail)) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "This email address is already in use by another account.");
        }

        user.setPendingEmail(newEmail);
        userRepository.save(user);
        
        log.info("AUDIT: Email change requested for user {}. New email pending: {}", userId, newEmail);
        
        // Dispatch verification email to the NEW email address
        emailVerificationService.sendVerificationEmail(userId, newEmail, user.getName());
    }

    /**
     * Uploads and replaces the user's avatar image.
     */
    public UserResponse updateAvatar(UUID userId, MultipartFile file) throws IOException {
        User user = getUser(userId);
        String oldAvatarUrl = user.getAvatarUrl();
        
        java.util.Map<String, Object> uploadResult = fileStorageService.upload(file, "avatars");
        String newAvatarUrl = (String) uploadResult.get("secure_url");
        user.setAvatarUrl(newAvatarUrl);
        user = userRepository.save(user);
        
        if (oldAvatarUrl != null) {
            deleteAvatarFromStorage(oldAvatarUrl);
        }
        
        log.info("AUDIT: Avatar updated for user {}", userId);
        return mapToUserResponse(user);
    }

    /**
     * Deletes the user's avatar image and sets it to null.
     */
    public UserResponse deleteAvatar(UUID userId) {
        User user = getUser(userId);
        String oldAvatarUrl = user.getAvatarUrl();
        
        if (oldAvatarUrl != null) {
            user.setAvatarUrl(null);
            user = userRepository.save(user);
            deleteAvatarFromStorage(oldAvatarUrl);
            log.info("AUDIT: Avatar deleted for user {}", userId);
        }
        
        return mapToUserResponse(user);
    }

    private void deleteAvatarFromStorage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isBlank()) return;
            String[] parts = imageUrl.split("/");
            String fileWithExt = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            String publicId = folder + "/" + fileWithExt.split("\\.")[0];
            fileStorageService.delete(publicId);
        } catch (Exception e) {
            log.warn("Failed to delete avatar from storage: {}", imageUrl, e);
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found."));
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = userMapper.toUserResponse(user);
        response.setCreatorProfileId(creatorRepository.findByUserId(user.getId())
                .map(Creator::getId).orElse(null));
        return response;
    }
}
