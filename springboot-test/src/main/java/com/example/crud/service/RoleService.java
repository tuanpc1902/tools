package com.example.crud.service;

import com.example.crud.dto.RoleDTO;
import com.example.crud.entity.AuditLog;
import com.example.crud.entity.Role;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.RoleRepository;
import com.example.crud.repository.UserRepository;
import com.example.crud.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditLogService auditLogService;

    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       AuditLogService auditLogService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.auditLogService = auditLogService;
    }

    public RoleDTO createRole(RoleDTO dto) {
        if (roleRepository.findByCode(dto.getCode()).isPresent()) {
            throw new ConflictException("Role code đã tồn tại: " + dto.getCode());
        }
        Role created = roleRepository.createRole(toEntity(dto));
        auditLogService.record(new AuditLog(null, null, "CREATE", "ROLE", created.getId(), null, null, null, null));
        return toDTO(created);
    }

    public RoleDTO updateRole(Long id, RoleDTO dto) {
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy role với ID: " + id));

        Role byCode = roleRepository.findByCode(dto.getCode()).orElse(null);
        if (byCode != null && !byCode.getId().equals(id)) {
            throw new ConflictException("Role code đã được dùng: " + dto.getCode());
        }

        existing.setCode(dto.getCode());
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        Role updated = roleRepository.updateRole(existing);
        auditLogService.record(new AuditLog(null, null, "UPDATE", "ROLE", updated.getId(), null, null, null, null));
        return toDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(this::toDTO).toList();
    }

    public void deleteRole(Long id) {
        if (roleRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Không tìm thấy role với ID: " + id);
        }
        roleRepository.deleteById(id);
        auditLogService.record(new AuditLog(null, null, "DELETE", "ROLE", id, null, null, null, null));
    }

    public void assignRoleToUser(Long userId, String roleCode) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Không tìm thấy user với ID: " + userId);
        }
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy role với code: " + roleCode));
        userRoleRepository.addRoleToUser(userId, role.getId());
        auditLogService.record(new AuditLog(null, userId, "ASSIGN_ROLE", "ROLE", role.getId(), null, null, null, null));
    }

    private Role toEntity(RoleDTO dto) {
        Role role = new Role();
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        return role;
    }

    private RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}
