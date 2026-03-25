package com.example.voting_system.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VoteController voteController;

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("candidates", voteController.getCandidates());
        model.addAttribute("votingActive", voteController.isVotingActive());
        model.addAttribute("auditLogs", voteController.getAuditLogs());
        return "admin-dashboard";
    }

    @PostMapping("/toggle")
    public String toggleVoting(HttpSession session) {
        if (session.getAttribute("admin") != null) {
            voteController.toggleVotingState();
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/candidate/add")
    public String addCandidate(@RequestParam String candidateName, HttpSession session) {
        if (session.getAttribute("admin") != null) {
            voteController.addCandidate(candidateName);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/candidate/delete/{id}")
    public String deleteCandidate(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin") != null) {
            voteController.deleteCandidate(id);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/reset")
    public String resetElection(HttpSession session) {
        if (session.getAttribute("admin") != null) {
            voteController.resetElection();
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}