package com.jobportal.service;

import com.jobportal.dto.ProfileUpdateRequest;
import com.jobportal.dto.UserDto;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto updateProfile(Long userId, ProfileUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setName(req.getName().trim());
        user.setPhone(req.getPhone());
        user.setEducation(req.getEducation());
        user.setSkills(req.getSkills());

        User updated = userRepository.save(user);
        return UserDto.fromEntity(updated);
    }
}
