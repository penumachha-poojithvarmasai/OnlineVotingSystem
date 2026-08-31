package com.example.voting_system.repository;

import com.example.voting_system.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    AuditLog findFirstByOrderByIdDesc();
}
