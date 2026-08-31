package com.example.voting_system.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // =========================================================================
    // TEMPORARY DEMO OTP MODE
    // Set IS_DEMO_MODE to false to restore production random OTP generation.
    // =========================================================================
    private static final boolean IS_DEMO_MODE = true;
    private static final String DEMO_OTP = "123456";

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String key) {
        if (IS_DEMO_MODE) {
            System.out.println("Demo OTP: " + DEMO_OTP);
            otpStorage.put(key, DEMO_OTP);
            return DEMO_OTP;
        }

        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(key, otp);
        return otp;
    }

    public boolean validateOtp(String key, String otp) {
        if (IS_DEMO_MODE && DEMO_OTP.equals(otp)) {
            otpStorage.remove(key); // OTP is one-time use
            return true;
        }
        if (otpStorage.containsKey(key) && otpStorage.get(key).equals(otp)) {
            otpStorage.remove(key); // OTP is one-time use
            return true;
        }
        return false;
    }
}
