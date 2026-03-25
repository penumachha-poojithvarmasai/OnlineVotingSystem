package com.example.voting_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String voterName;
    private String voterId;
    private String timestamp;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String faceImageBase64;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String faceDescriptor;

    private String candidateName;
    private String status;

    @Column(length = 64)
    private String previousHash;

    @Column(length = 64)
    private String currentHash;

    // Default constructor for JPA
    public AuditLog() {}

    public AuditLog(String voterName, String voterId, String faceImageBase64, String faceDescriptor, String candidateName, String status, String previousHash, String currentHash) {
        this.voterName = voterName;
        this.voterId = voterId;
        this.faceImageBase64 = faceImageBase64;
        this.faceDescriptor = faceDescriptor;
        this.candidateName = candidateName;
        this.status = status;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.previousHash = previousHash;
        this.currentHash = currentHash;
    }

    public Long getId() { return id; }
    public String getVoterName() { return voterName; }
    public String getVoterId() { return voterId; }
    public String getTimestamp() { return timestamp; }
    public String getFaceImageBase64() { return faceImageBase64; }
    public String getFaceDescriptor() { return faceDescriptor; }
    public String getCandidateName() { return candidateName; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) { this.status = status; }

    public String getPreviousHash() { return previousHash; }
    public String getCurrentHash() { return currentHash; }
}
