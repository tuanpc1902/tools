package com.example.crud.service;

import com.example.crud.dto.UserProfileDTO;
import com.example.crud.entity.AuditLog;
import com.example.crud.entity.UserProfile;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.UserProfileRepository;
import com.example.crud.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              UserRepository userRepository,
                              AuditLogService auditLogService) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(Long userId) {
        return userProfileRepository.getByUserId(userId)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy profile cho user: " + userId));
    }

    public UserProfileDTO upsertProfile(Long userId, UserProfileDTO dto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Không tìm thấy user với ID: " + userId);
        }
        UserProfile profile = toEntity(userId, dto);
        UserProfile saved = userProfileRepository.upsertProfile(profile);
        auditLogService.record(new AuditLog(null, userId, "UPSERT_PROFILE", "USER_PROFILE", userId, null, null, null, null));
        return toDTO(saved);
    }

    private UserProfile toEntity(Long userId, UserProfileDTO dto) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setNationalId(dto.getNationalId());
        profile.setJobTitle(dto.getJobTitle());
        profile.setCompany(dto.getCompany());
        profile.setBio(dto.getBio());
        return profile;
    }

    private UserProfileDTO toDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setGender(profile.getGender());
        dto.setNationalId(profile.getNationalId());
        dto.setJobTitle(profile.getJobTitle());
        dto.setCompany(profile.getCompany());
        dto.setBio(profile.getBio());
        return dto;
    }
}
