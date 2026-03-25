package com.example.voting_system.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String key) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(key, otp);
        return otp;
    }

    public boolean validateOtp(String key, String otp) {
        if (otpStorage.containsKey(key) && otpStorage.get(key).equals(otp)) {
            otpStorage.remove(key); // OTP is one-time use
            return true;
        }
        return false;
    }
}
