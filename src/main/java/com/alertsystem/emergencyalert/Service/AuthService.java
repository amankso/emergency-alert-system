package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.DTO.UserDTO;
import com.alertsystem.emergencyalert.Entity.OtpEntity;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import com.alertsystem.emergencyalert.Repository.OtpRepository;
import com.alertsystem.emergencyalert.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.random.RandomGenerator;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final TwilioService twilioService;

    //signup code , username and mobile number received
    public UserEntity signup(UserDTO userDTO){
        if(userRepository.existsByMobileNumber(userDTO.getMobileNumber())){
            throw new RuntimeException("User already exists");
        }
        UserEntity userEntity = new UserEntity();
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setMobileNumber(userDTO.getMobileNumber());
        otpEntity.setOtp(RandomUUID.Integer()); //4 digit code

        userEntity.setMobileNumber(userDTO.getMobileNumber());
        userEntity.setUsername(userDTO.getUsername());
        userEntity.se

    }
}
