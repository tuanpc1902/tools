package com.example.crud.service;

import com.example.crud.dto.UserDTO;
import com.example.crud.entity.User;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho UserService
 * Sử dụng Mockito để mock dependencies
 * Test từng method một cách độc lập
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        // Setup test data
        userDTO = new UserDTO();
        userDTO.setName("Nguyễn Văn A");
        userDTO.setEmail("nguyenvana@example.com");
        userDTO.setPhone("0123456789");
        userDTO.setStatus("ACTIVE");

        user = new User();
        user.setId(1L);
        user.setName("Nguyễn Văn A");
        user.setEmail("nguyenvana@example.com");
        user.setPhone("0123456789");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test CREATE user - thành công")
    void testCreateUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.createUser(any(User.class))).thenReturn(user);

        // When
        UserDTO result = userService.createUser(userDTO);

        // Then
        assertNotNull(result);
        assertEquals(userDTO.getName(), result.getName());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).existsByEmail(userDTO.getEmail());
        verify(userRepository, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("Test CREATE user - email đã tồn tại")
    void testCreateUser_EmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService.createUser(userDTO);
        });

        assertEquals("Email đã tồn tại: " + userDTO.getEmail(), exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(userDTO.getEmail());
        verify(userRepository, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("Test READ user by ID - thành công")
    void testGetUserById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.getUserById(userId)).thenReturn(Optional.of(user));

        // When
        UserDTO result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Test READ user by ID - không tìm thấy")
    void testGetUserById_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.getUserById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        assertEquals("Không tìm thấy user với ID: " + userId, exception.getMessage());
        verify(userRepository, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Test READ ALL users - thành công")
    void testGetAllUsers_Success() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Trần Thị B");
        user2.setEmail("tranthib@example.com");
        user2.setPhone("0987654321");

        List<User> users = Arrays.asList(user, user2);
        when(userRepository.getAllUsers()).thenReturn(users);

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(user.getName(), result.get(0).getName());
        assertEquals(user2.getName(), result.get(1).getName());
        verify(userRepository, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Test UPDATE user - thành công")
    void testUpdateUser_Success() {
        // Given
        Long userId = 1L;
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Nguyễn Văn A Updated");
        updateDTO.setEmail("nguyenvana@example.com");
        updateDTO.setPhone("0111111111");
        updateDTO.setStatus("ACTIVE");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findByEmail(updateDTO.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.updateUser(any(User.class))).thenReturn(user);

        // When
        UserDTO result = userService.updateUser(userId, updateDTO);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).findByEmail(updateDTO.getEmail());
        verify(userRepository, times(1)).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Test UPDATE user - không tìm thấy")
    void testUpdateUser_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateUser(userId, userDTO);
        });

        assertEquals("Không tìm thấy user với ID: " + userId, exception.getMessage());
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Test UPDATE user - email trùng với user khác")
    void testUpdateUser_EmailExistsForOtherUser() {
        // Given
        Long userId = 1L;
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        userDTO.setEmail("other@example.com");
        userDTO.setStatus("ACTIVE");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(otherUser));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService.updateUser(userId, userDTO);
        });

        assertEquals("Email đã được sử dụng bởi user khác: " + userDTO.getEmail(), exception.getMessage());
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).findByEmail(userDTO.getEmail());
        verify(userRepository, never()).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Test DELETE user - thành công")
    void testDeleteUser_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteUser(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("Test DELETE user - không tìm thấy")
    void testDeleteUser_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertEquals("Không tìm thấy user với ID: " + userId, exception.getMessage());
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteUser(userId);
    }

    @Test
    @DisplayName("Test SEARCH users by name - thành công")
    void testSearchUsersByName_Success() {
        // Given
        String searchName = "Nguyễn";
        List<User> users = Arrays.asList(user);
        when(userRepository.findByNameContaining(searchName)).thenReturn(users);

        // When
        List<UserDTO> result = userService.searchUsersByName(searchName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains(searchName));
        verify(userRepository, times(1)).findByNameContaining(searchName);
    }

    @Test
    @DisplayName("Test DYNAMIC SEARCH - tìm kiếm với nhiều điều kiện")
    void testSearchUsersDynamic_Success() {
        // Given
        List<User> users = Arrays.asList(user);
        when(userRepository.findUsersDynamic("Nguyễn", null, null)).thenReturn(users);

        // When
        List<UserDTO> result = userService.searchUsersDynamic("Nguyễn", null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findUsersDynamic("Nguyễn", null, null);
    }
}
