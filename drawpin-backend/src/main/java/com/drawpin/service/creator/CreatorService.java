package com.drawpin.service.creator;

import com.drawpin.domain.entity.Creator;
import com.drawpin.domain.entity.User;
import com.drawpin.domain.enums.CreatorVerificationStatus;
import com.drawpin.domain.enums.UserRole;
import com.drawpin.dto.request.creator.BecomeCreatorRequest;
import com.drawpin.dto.request.creator.UpdateCreatorProfileRequest;
import com.drawpin.dto.response.creator.CreatorProfileResponse;
import com.drawpin.exception.ConflictException;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.mapper.CreatorMapper;
import com.drawpin.repository.CreatorRepository;
import com.drawpin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreatorService {

    private final CreatorRepository creatorRepository;
    private final CreatorMapper creatorMapper;
    private final UserRepository userRepository;

    /**
     * Upgrades a user to a Creator and initializes their profile.
     */
    public CreatorProfileResponse becomeCreator(UUID userId, BecomeCreatorRequest request) {
        if (creatorRepository.existsByUserId(userId)) {
            throw new ConflictException("ALREADY_CREATOR", "You already have a creator profile.");
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found."));

        Creator creator = creatorMapper.toEntity(request);
        creator.setUser(user);
        creator.setVerificationStatus(CreatorVerificationStatus.NONE);
        creator.setAvailable(true);

        creator = creatorRepository.save(creator);

        // Upgrade user role to CREATOR if they are currently a COLLECTOR
        if (user.getRole() == UserRole.COLLECTOR) {
            user.setRole(UserRole.CREATOR);
            userRepository.save(user);
        }

        log.info("AUDIT: User {} became a creator with profile ID {}", userId, creator.getId());
        return creatorMapper.toResponse(creator);
    }

    /**
     * Updates an existing creator profile.
     */
    public CreatorProfileResponse updateProfile(UUID userId, UpdateCreatorProfileRequest request) {
        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CREATOR_NOT_FOUND", "Creator profile not found."));

        creatorMapper.updateEntityFromRequest(request, creator);
        creator = creatorRepository.save(creator);

        log.info("AUDIT: Creator profile updated for user {}", userId);
        return creatorMapper.toResponse(creator);
    }

    /**
     * Retrieves a creator profile by their unique profile ID.
     */
    @Transactional(readOnly = true)
    public CreatorProfileResponse getProfile(UUID creatorId) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("CREATOR_NOT_FOUND", "Creator profile not found."));
        return creatorMapper.toResponse(creator);
    }

    /**
     * Retrieves a creator profile by their user handle.
     */
    @Transactional(readOnly = true)
    public CreatorProfileResponse getProfileByHandle(String handle) {
        Creator creator = creatorRepository.findByUserHandle(handle)
                .orElseThrow(() -> new ResourceNotFoundException("CREATOR_NOT_FOUND", "Creator profile not found for handle: " + handle));
        return creatorMapper.toResponse(creator);
    }

    /**
     * Requests verification for a creator profile.
     */
    public void requestVerification(UUID userId) {
        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CREATOR_NOT_FOUND", "Creator profile not found."));

        if (creator.getVerificationStatus() != CreatorVerificationStatus.NONE &&
            creator.getVerificationStatus() != CreatorVerificationStatus.REJECTED) {
            throw new ConflictException("VERIFICATION_ALREADY_REQUESTED", "Verification is already pending or approved.");
        }

        creator.setVerificationStatus(CreatorVerificationStatus.PENDING);
        creatorRepository.save(creator);
        
        log.info("AUDIT: Verification requested for creator {}", creator.getId());
    }
}
