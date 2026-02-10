package com.example.crud.service;

import com.example.crud.entity.AuditLog;
import com.example.crud.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(AuditLog log) {
        auditLogRepository.createLog(log);
    }
}
