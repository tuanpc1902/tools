package com.example.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private Long id;
    private Long actorUserId;
    private String action;
    private String entityType;
    private Long entityId;
    private String beforeData;
    private String afterData;
    private String ipAddress;
    private LocalDateTime createdAt;
}
