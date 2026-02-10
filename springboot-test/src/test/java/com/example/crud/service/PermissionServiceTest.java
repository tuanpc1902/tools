package com.example.crud.service;

import com.example.crud.dto.PermissionDTO;
import com.example.crud.entity.Permission;
import com.example.crud.exception.ConflictException;
import com.example.crud.repository.PermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    @DisplayName("Create permission - conflict")
    void createPermissionConflict() {
        Permission existing = new Permission(1L, "USER_READ", "Read users", "", null, null);
        when(permissionRepository.findByCode("USER_READ")).thenReturn(Optional.of(existing));

        PermissionDTO dto = new PermissionDTO(null, "USER_READ", "Read users", "");
        assertThrows(ConflictException.class, () -> permissionService.createPermission(dto));
        verify(permissionRepository, never()).createPermission(any(Permission.class));
    }
}
