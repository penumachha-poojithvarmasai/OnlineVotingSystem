package com.example.voting_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private int votes;

    // Default constructor for JPA
    public Candidate() {}

    public Candidate(String name) {
        this.name = name;
        this.votes = 0;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getVotes() { return votes; }
    public void setVotes(int votes) { this.votes = votes; }

    public void addVote() {
        this.votes++;
    }

    public void removeVote() {
        if (this.votes > 0) {
            this.votes--;
        }
    }
}