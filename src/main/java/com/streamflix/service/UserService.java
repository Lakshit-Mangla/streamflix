package com.streamflix.service;

import com.streamflix.dto.request.UpdateProfileRequest;
import com.streamflix.dto.response.UserProfileResponse;
import com.streamflix.entity.User;
import com.streamflix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(User user) {
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPreferredGenres() != null) {
            user.setPreferredGenres(request.getPreferredGenres());
        }
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .preferredGenres(user.getPreferredGenres())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
