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
import com.drawpin.service.upload.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
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
        return userMapper.toUserResponse(user);
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
        return userMapper.toUserResponse(user);
    }

    /**
     * Retrieves a user's profile by their UUID.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return userMapper.toUserResponse(getUser(userId));
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
        
        String newAvatarUrl = cloudinaryService.uploadImage(file, "drawpin/avatars");
        user.setAvatarUrl(newAvatarUrl);
        user = userRepository.save(user);
        
        if (oldAvatarUrl != null) {
            cloudinaryService.deleteImage(oldAvatarUrl);
        }
        
        log.info("AUDIT: Avatar updated for user {}", userId);
        return userMapper.toUserResponse(user);
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
            cloudinaryService.deleteImage(oldAvatarUrl);
            log.info("AUDIT: Avatar deleted for user {}", userId);
        }
        
        return userMapper.toUserResponse(user);
    }

    private User getUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found."));
    }
}
