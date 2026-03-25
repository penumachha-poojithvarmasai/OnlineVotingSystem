package com.example.voting_system.controller;

import com.example.voting_system.model.Voter;
import com.example.voting_system.service.EmailService;
import com.example.voting_system.service.OtpService;
import com.example.voting_system.service.VoterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class LoginController {

    // Anti-Brute Force In-Memory Tracker
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    @Autowired
    private VoterService voterService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @GetMapping("/")
    public String home() {
        return "login";
    }

    @GetMapping("/voter/login")
    public String voterLoginPage() {
        return "voter-login";
    }

    @PostMapping("/voter/login")
    public String voterLogin(@RequestParam String voterId,
            @RequestParam String dob,
            HttpSession session,
            Model model) {
        
        String cleanVoterId = voterId.toUpperCase();
        
        // Brute Force Check
        if (failedAttempts.getOrDefault(cleanVoterId, 0) >= MAX_ATTEMPTS) {
            model.addAttribute("error", "SECURITY LOCKOUT: Your Voter ID is temporarily frozen due to excessive failed attempts. Please contact Election Commission.");
            return "voter-login";
        }

        Voter voter = voterService.getVoter(cleanVoterId);
        String normalizedDob = normalizeDOB(dob);

        if (voter != null && voter.getDob().equals(normalizedDob)) {
            // Success, clear lockouts
            failedAttempts.remove(cleanVoterId);

            if (voter.isHasVoted()) {
                model.addAttribute("error", "You have already cast your vote securely. Rest assured, your cryptographic ledger entry is sealed. Have a great day!");
                return "voter-login";
            }

            // Generate and send OTP
            String otp = otpService.generateOtp(cleanVoterId);
            emailService.sendEmail(voter.getEmail(), "Voter Login OTP", "Your OTP is: " + otp);

            // Store voterId in session temporarily
            session.setAttribute("tempVoterId", cleanVoterId);
            session.setAttribute("loginType", "voter");

            return "redirect:/otp-verify";
        }
        
        // Record failure
        int attempts = failedAttempts.getOrDefault(cleanVoterId, 0) + 1;
        failedAttempts.put(cleanVoterId, attempts);
        model.addAttribute("error", "Invalid Voter ID or Date of Birth. Attempt " + attempts + " of " + MAX_ATTEMPTS);
        return "voter-login";
    }

    private String normalizeDOB(String dob) {
        String[] parts = dob.split("-");
        if (parts.length == 3) {
            // HTML5 returns YYYY-MM-DD. Convert to DD-MM-YYYY for our database matching.
            return String.format("%02d-%02d-%04d", 
                Integer.parseInt(parts[2]), 
                Integer.parseInt(parts[1]), 
                Integer.parseInt(parts[0]));
        }
        return dob;
    }

    @GetMapping("/otp-verify")
    public String otpVerifyPage() {
        return "otp-verify";
    }

    @PostMapping("/otp-verify")
    public String verifyOtp(@RequestParam String otp, HttpSession session, Model model) {
        String loginType = (String) session.getAttribute("loginType");

        if ("voter".equals(loginType)) {
            String voterId = (String) session.getAttribute("tempVoterId");
            
            // Brute Force Check OTP Level
            if (failedAttempts.getOrDefault(voterId, 0) >= MAX_ATTEMPTS) {
                model.addAttribute("error", "SECURITY LOCKOUT: Maximum OTP guesses reached. Account frozen.");
                return "voter-login"; // Kick back to start
            }

            if (otpService.validateOtp(voterId, otp)) {
                failedAttempts.remove(voterId);
                Voter voter = voterService.getVoter(voterId);
                session.setAttribute("voter", voter);
                session.removeAttribute("tempVoterId");
                return "redirect:/vote";
            } else {
                int attempts = failedAttempts.getOrDefault(voterId, 0) + 1;
                failedAttempts.put(voterId, attempts);
                model.addAttribute("error", "Invalid OTP. Attempt " + attempts + " of " + MAX_ATTEMPTS);
                return "otp-verify";
            }
        } else if ("admin".equals(loginType)) {
            String username = (String) session.getAttribute("tempAdminUser");
            if (otpService.validateOtp(username, otp)) {
                session.setAttribute("admin", true);
                session.removeAttribute("tempAdminUser");
                return "redirect:/admin/dashboard";
            }
        }

        model.addAttribute("error", "Invalid OTP security challenge. Try again.");
        return "otp-verify";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            String otp = otpService.generateOtp(username);
            emailService.sendEmail("sunny84140104@gmail.com", "Admin Login OTP", "Your OTP is: " + otp);

            session.setAttribute("tempAdminUser", username);
            session.setAttribute("loginType", "admin");
            return "redirect:/otp-verify";
        }
        model.addAttribute("error", "Invalid admin credentials");
        return "admin-login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
