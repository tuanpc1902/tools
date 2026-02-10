package com.example.crud.service;

import com.example.crud.dto.RoleDTO;
import com.example.crud.entity.Role;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.RoleRepository;
import com.example.crud.repository.UserRepository;
import com.example.crud.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RoleService roleService;

    private RoleDTO roleDTO;
    private Role role;

    @BeforeEach
    void setUp() {
        roleDTO = new RoleDTO(1L, "ADMIN", "Administrator", "Full access");
        role = new Role(1L, "ADMIN", "Administrator", "Full access", null, null);
    }

    @Test
    @DisplayName("Create role - success")
    void createRoleSuccess() {
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.createRole(any(Role.class))).thenReturn(role);

        RoleDTO result = roleService.createRole(roleDTO);

        assertNotNull(result);
        assertEquals("ADMIN", result.getCode());
        verify(roleRepository, times(1)).createRole(any(Role.class));
        verify(auditLogService, times(1)).record(any());
    }

    @Test
    @DisplayName("Create role - conflict")
    void createRoleConflict() {
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.of(role));

        assertThrows(ConflictException.class, () -> roleService.createRole(roleDTO));
        verify(roleRepository, never()).createRole(any(Role.class));
    }

    @Test
    @DisplayName("Assign role - user not found")
    void assignRoleUserNotFound() {
        when(userRepository.existsById(10L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> roleService.assignRoleToUser(10L, "ADMIN"));
        verify(userRoleRepository, never()).addRoleToUser(anyLong(), anyLong());
    }
}
