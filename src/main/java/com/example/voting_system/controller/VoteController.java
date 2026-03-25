package com.example.voting_system.controller;

import com.example.voting_system.model.AuditLog;
import com.example.voting_system.model.Candidate;
import com.example.voting_system.model.Voter;
import com.example.voting_system.repository.AuditLogRepository;
import com.example.voting_system.repository.CandidateRepository;
import com.example.voting_system.repository.VoterRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class VoteController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private VoterRepository voterRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Database off by default 
    private boolean votingActive = false;

    @PostConstruct
    public void init() {
        if (candidateRepository.count() == 0) {
            candidateRepository.save(new Candidate("Dhoni"));
            candidateRepository.save(new Candidate("Jadeja"));
            candidateRepository.save(new Candidate("Nota"));
            candidateRepository.save(new Candidate("Kohli"));
        }
    }

    @GetMapping("/vote")
    public String votePage(Model model, HttpSession session) {
        Voter sessionVoter = (Voter) session.getAttribute("voter");
        if (sessionVoter == null) {
            return "redirect:/voter/login";
        }

        // Fetch latest state
        Voter loggedInVoter = voterRepository.findByVoterId(sessionVoter.getVoterId()).orElse(null);
        if (loggedInVoter == null) return "redirect:/voter/login";

        model.addAttribute("voter", loggedInVoter);
        model.addAttribute("candidates", candidateRepository.findAll());
        model.addAttribute("votingActive", votingActive);
        model.addAttribute("message", "");
        return "index";
    }

    @PostMapping("/vote")
    public String vote(@RequestParam(required = false) String candidate,
                       @RequestParam(required = false) String faceImageBase64,
                       @RequestParam(required = false) String faceDescriptor,
                       Model model, HttpSession session) {
        
        Voter sessionVoter = (Voter) session.getAttribute("voter");
        if (sessionVoter == null) {
            return "redirect:/voter/login";
        }
        
        Voter loggedInVoter = voterRepository.findByVoterId(sessionVoter.getVoterId()).orElse(null);
        if (loggedInVoter == null) return "redirect:/voter/login";

        List<Candidate> allCandidates = candidateRepository.findAll();

        if (!votingActive) {
            model.addAttribute("message", "Voting is currently closed!");
            model.addAttribute("candidates", allCandidates);
            model.addAttribute("votingActive", votingActive);
            return "index";
        }

        if (faceImageBase64 == null || faceImageBase64.isEmpty()) {
            model.addAttribute("message", "Biometric Face Capture is REQUIRED to cast your vote!");
            model.addAttribute("candidates", allCandidates);
            model.addAttribute("votingActive", votingActive);
            return "index";
        }

        if (candidate == null || candidate.isEmpty()) {
            model.addAttribute("message", "Please select a candidate!");
            model.addAttribute("candidates", allCandidates);
            model.addAttribute("votingActive", votingActive);
            return "index";
        }

        if (loggedInVoter.isHasVoted()) {
            model.addAttribute("message", "You have already voted!");
            model.addAttribute("candidates", allCandidates);
            model.addAttribute("votingActive", votingActive);
            return "index";
        }

        Candidate c = candidateRepository.findByName(candidate).orElse(null);
        if (c != null) {
            
            // --- CROSS-ID DUPLICATION CHECK ---
            boolean isDuplicate = false;
            AuditLog matchingLog = null;
            if (faceDescriptor != null && !faceDescriptor.isEmpty() && !faceDescriptor.equals("null")) {
                List<AuditLog> allLogs = auditLogRepository.findAll();
                for (AuditLog log : allLogs) {
                    if (log.getFaceDescriptor() != null && !log.getFaceDescriptor().isEmpty() && !log.getFaceDescriptor().equals("null")) {
                        // Skip if it is the same voter ID
                        if (log.getVoterId().equals(loggedInVoter.getVoterId())) continue;
                        
                        double distance = calculateFaceDistance(faceDescriptor, log.getFaceDescriptor());
                        if (distance < 0.55 && distance >= 0) { // Valid distance and below threshold
                            isDuplicate = true;
                            matchingLog = log;
                            break;
                        }
                    }
                }
            }

            loggedInVoter.setHasVoted(true);
            voterRepository.save(loggedInVoter);

            AuditLog lastLog = auditLogRepository.findFirstByOrderByIdDesc();
            String previousHash = lastLog != null ? lastLog.getCurrentHash() : "0000000000000000000000000000000000000000000000000000000000000000";
            String rawData = previousHash + loggedInVoter.getVoterId() + candidate + System.currentTimeMillis();
            String currentHash = calculateHash(rawData);

            if (isDuplicate && matchingLog != null) {
                // Fraud! Rollback the old vote
                if (matchingLog.getCandidateName() != null) {
                    Candidate oldCandidate = candidateRepository.findByName(matchingLog.getCandidateName()).orElse(null);
                    if (oldCandidate != null) {
                        oldCandidate.removeVote();
                        candidateRepository.save(oldCandidate);
                    }
                }
                
                // Mark old AuditLog as TERMINATED
                matchingLog.setStatus("TERMINATED_DUPLICATE_FACE");
                auditLogRepository.save(matchingLog);

                // Save new AuditLog as TERMINATED
                AuditLog auditLog = new AuditLog(loggedInVoter.getFirstName() + " " + loggedInVoter.getLastName(), 
                                                 loggedInVoter.getVoterId(), faceImageBase64, faceDescriptor, 
                                                 c.getName(), "TERMINATED_DUPLICATE_FACE", previousHash, currentHash);
                auditLogRepository.save(auditLog);

                model.addAttribute("message", "CRITICAL WARNING: Duplicate Face Detected. Both this vote and your previous vote have been TERMINATED and flagged for admin review.");
            } else {
                // Valid Vote
                c.addVote();
                candidateRepository.save(c);

                AuditLog auditLog = new AuditLog(loggedInVoter.getFirstName() + " " + loggedInVoter.getLastName(), 
                                                 loggedInVoter.getVoterId(), faceImageBase64, faceDescriptor, 
                                                 c.getName(), "VALID", previousHash, currentHash);
                auditLogRepository.save(auditLog);

                model.addAttribute("message", "success: Vote securely recorded in Blockchain Ledger.");
            }

        } else {
            model.addAttribute("message", "Candidate not found!");
        }

        model.addAttribute("candidates", candidateRepository.findAll());
        model.addAttribute("votingActive", votingActive);
        return "index";
    }

    @GetMapping("/result")
    public String resultPage(Model model) {
        model.addAttribute("candidates", candidateRepository.findAll());
        return "result";
    }

    public List<Candidate> getCandidates() {
        return candidateRepository.findAll();
    }
    
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }

    public boolean isVotingActive() {
        return votingActive;
    }

    public void toggleVotingState() {
        votingActive = !votingActive;
    }

    public void addCandidate(String name) {
        if (name != null && !name.trim().isEmpty() && !candidateRepository.findByName(name.trim()).isPresent()) {
            candidateRepository.save(new Candidate(name.trim()));
        }
    }

    public void deleteCandidate(Long id) {
        if (id != null) {
            candidateRepository.deleteById(id);
        }
    }

    @jakarta.transaction.Transactional
    public void resetElection() {
        this.votingActive = false; // Turn off voting for safety
        
        // 1. Delete all audit logs (erases blockchain history)
        auditLogRepository.deleteAll();
        
        // 2. Set all candidate votes to 0
        List<Candidate> candidates = candidateRepository.findAll();
        for (Candidate c : candidates) {
            c.setVotes(0);
        }
        candidateRepository.saveAll(candidates);
        
        // 3. Reset all voter statuses to allow voting again
        List<Voter> voters = voterRepository.findAll();
        for (Voter v : voters) {
            v.setHasVoted(false);
        }
        voterRepository.saveAll(voters);
    }

    private String calculateHash(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Encryption Error: " + e.getMessage());
        }
    }

    private double calculateFaceDistance(String descriptor1Json, String descriptor2Json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            double[] arr1 = mapper.readValue(descriptor1Json, double[].class);
            double[] arr2 = mapper.readValue(descriptor2Json, double[].class);
            
            if (arr1.length != arr2.length) return 1.0;
            
            double sum = 0;
            for (int i = 0; i < arr1.length; i++) {
                double diff = arr1[i] - arr2[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        } catch (Exception e) {
            e.printStackTrace();
            return 1.0; // Assume not match if error
        }
    }
}