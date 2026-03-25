package com.example.voting_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "voters")
public class Voter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String voterId;

    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private boolean hasVoted;

    // Default constructor for JPA
    public Voter() {}

    public Voter(String voterId, String firstName, String lastName, String dob, String email) {
        this.voterId = voterId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
        this.hasVoted = false;
    }

    public Long getId() { return id; }
    public String getVoterId() { return voterId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDob() { return dob; }
    public String getEmail() { return email; }
    public boolean isHasVoted() { return hasVoted; }

    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
}