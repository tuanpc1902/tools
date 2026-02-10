package com.example.crud.service;

import com.example.crud.dto.PermissionDTO;
import com.example.crud.entity.AuditLog;
import com.example.crud.entity.Permission;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final AuditLogService auditLogService;

    public PermissionService(PermissionRepository permissionRepository, AuditLogService auditLogService) {
        this.permissionRepository = permissionRepository;
        this.auditLogService = auditLogService;
    }

    public PermissionDTO createPermission(PermissionDTO dto) {
        if (permissionRepository.findByCode(dto.getCode()).isPresent()) {
            throw new ConflictException("Permission code đã tồn tại: " + dto.getCode());
        }
        Permission created = permissionRepository.createPermission(toEntity(dto));
        auditLogService.record(new AuditLog(null, null, "CREATE", "PERMISSION", created.getId(), null, null, null, null));
        return toDTO(created);
    }

    public PermissionDTO updatePermission(Long id, PermissionDTO dto) {
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy permission với ID: " + id));

        Permission byCode = permissionRepository.findByCode(dto.getCode()).orElse(null);
        if (byCode != null && !byCode.getId().equals(id)) {
            throw new ConflictException("Permission code đã được dùng: " + dto.getCode());
        }

        existing.setCode(dto.getCode());
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        Permission updated = permissionRepository.updatePermission(existing);
        auditLogService.record(new AuditLog(null, null, "UPDATE", "PERMISSION", updated.getId(), null, null, null, null));
        return toDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream().map(this::toDTO).toList();
    }

    public void deletePermission(Long id) {
        if (permissionRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Không tìm thấy permission với ID: " + id);
        }
        permissionRepository.deleteById(id);
        auditLogService.record(new AuditLog(null, null, "DELETE", "PERMISSION", id, null, null, null, null));
    }

    private Permission toEntity(PermissionDTO dto) {
        Permission permission = new Permission();
        permission.setCode(dto.getCode());
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        return permission;
    }

    private PermissionDTO toDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}
