package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.DTO.UserDTO;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import com.alertsystem.emergencyalert.Repository.UserRepository;
import com.alertsystem.emergencyalert.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserProfile(String sessionToken) {
        UserEntity user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired session token"));

        return UserDTO.builder()
                .username(user.getUsername())
                .mobileNumber(user.getMobileNumber())
                .isVerified(user.isVerified())
                .sessionToken(user.getSessionToken())
                .build();
    }

    @Transactional(readOnly = true)
    public List<String> getPredefinedMessages(String sessionToken) {
        UserEntity user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired session token"));
        return user.getPredefinedMessages();
    }

    @Transactional
    public void updatePredefinedMessages(String sessionToken, List<String> messages) {
        if (messages.size() > 3) {
            throw new IllegalArgumentException("You can only save up to 3 predefined messages.");
        }

        UserEntity user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired session token"));

        user.setPredefinedMessages(messages);
        userRepository.save(user);
    }
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }


    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserEntity getByMobile(String mobile) {
        return userRepository.findByMobileNumber(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    public UserEntity getBySessionToken(String token) {
        return userRepository.findBySessionToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired session token"));
    }

}
