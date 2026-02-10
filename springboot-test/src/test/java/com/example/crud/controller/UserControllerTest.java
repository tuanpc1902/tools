package com.example.crud.controller;

import com.example.crud.dto.UserDTO;
import com.example.crud.exception.NotFoundException;
import com.example.crud.service.AddressService;
import com.example.crud.service.RoleService;
import com.example.crud.service.UserListService;
import com.example.crud.service.UserProfileService;
import com.example.crud.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho UserController
 * Sử dụng @WebMvcTest để test chỉ Controller layer
 * Mock Service layer để test riêng Controller logic
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @MockBean: thay bean thật bằng mock trong Spring context của WebMvcTest.
    @MockBean
    private UserService userService;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private AddressService addressService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private UserListService userListService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Nguyễn Văn A");
        userDTO.setEmail("nguyenvana@example.com");
        userDTO.setPhone("0123456789");
        userDTO.setStatus("ACTIVE");
        userDTO.setCreatedAt(LocalDateTime.now());
        userDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test POST /api/users - CREATE thành công")
    void testCreateUser_Success() throws Exception {
        // Given
        UserDTO newUserDTO = new UserDTO();
        newUserDTO.setName("Trần Thị B");
        newUserDTO.setEmail("tranthib@example.com");
        newUserDTO.setPhone("0987654321");
        newUserDTO.setStatus("ACTIVE");

        UserDTO createdUser = new UserDTO();
        createdUser.setId(2L);
        createdUser.setName(newUserDTO.getName());
        createdUser.setEmail(newUserDTO.getEmail());
        createdUser.setPhone(newUserDTO.getPhone());
        createdUser.setStatus("ACTIVE");

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Trần Thị B"))
                .andExpect(jsonPath("$.email").value("tranthib@example.com"));

        verify(userService, times(1)).createUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("Test POST /api/users - Validation error")
    void testCreateUser_ValidationError() throws Exception {
        // Given - UserDTO không hợp lệ (thiếu email)
        UserDTO invalidUserDTO = new UserDTO();
        invalidUserDTO.setName("Test");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("Test GET /api/users/{id} - thành công")
    void testGetUserById_Success() throws Exception {
        // Given
        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userDTO.getName()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Test GET /api/users/{id} - không tìm thấy")
    void testGetUserById_NotFound() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new NotFoundException("Not found"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Test GET /api/users - lấy tất cả users")
    void testGetAllUsers_Success() throws Exception {
        // Given
        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setName("Trần Thị B");
        user2.setEmail("tranthib@example.com");
        user2.setStatus("ACTIVE");

        List<UserDTO> users = Arrays.asList(userDTO, user2);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Test PUT /api/users/{id} - UPDATE thành công")
    void testUpdateUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Nguyễn Văn A Updated");
        updateDTO.setEmail("nguyenvana@example.com");
        updateDTO.setPhone("0111111111");
        updateDTO.setStatus("ACTIVE");

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(userId);
        updatedUser.setName(updateDTO.getName());
        updatedUser.setEmail(updateDTO.getEmail());
        updatedUser.setPhone(updateDTO.getPhone());
        updatedUser.setStatus("ACTIVE");

        when(userService.updateUser(anyLong(), any(UserDTO.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nguyễn Văn A Updated"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserDTO.class));
    }

    @Test
    @DisplayName("Test PUT /api/users/{id} - không tìm thấy")
    void testUpdateUser_NotFound() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.updateUser(anyLong(), any(UserDTO.class)))
            .thenThrow(new NotFoundException("Not found"));

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(userId), any(UserDTO.class));
    }

    @Test
    @DisplayName("Test DELETE /api/users/{id} - thành công")
    void testDeleteUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("Test DELETE /api/users/{id} - không tìm thấy")
    void testDeleteUser_NotFound() throws Exception {
        // Given
        Long userId = 999L;
        doThrow(new RuntimeException("Not found")).when(userService).deleteUser(userId);
        doThrow(new NotFoundException("Not found")).when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("Test GET /api/users/search?name={name} - tìm kiếm thành công")
    void testSearchUsers_Success() throws Exception {
        // Given
        String searchName = "Nguyễn";
        List<UserDTO> users = Arrays.asList(userDTO);
        when(userService.searchUsersByName(searchName)).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .param("name", searchName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(userDTO.getName()));

        verify(userService, times(1)).searchUsersByName(searchName);
    }

    @Test
    @DisplayName("Test GET /api/users/page - exclude test data")
    void testGetUsersPage_ExcludeTestData() throws Exception {
        // Test case: excludeTestData = true -> trả về danh sách không có account_test.
        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setName("PO User");
        user1.setEmail("po@example.com");
        user1.setLevelCode("PO");
        user1.setIsTest(false);

        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setName("CO User");
        user2.setEmail("co@example.com");
        user2.setLevelCode("CO");
        user2.setIsTest(false);

        when(userListService.getUsersForPage(any(), eq(true))).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users/page")
                        .param("excludeTestData", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                // Assert: không có user test trong response.
                .andExpect(jsonPath("$[?(@.isTest==true)]").isEmpty());
    }

    @Test
    @DisplayName("Test GET /api/users/page - include test data")
    void testGetUsersPage_IncludeTestData() throws Exception {
        // Test case: excludeTestData = false -> danh sách có account_test.
        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setName("PO User");
        user1.setEmail("po@example.com");
        user1.setLevelCode("PO");
        user1.setIsTest(false);

        UserDTO testUser = new UserDTO();
        testUser.setId(3L);
        testUser.setName("Account Test");
        testUser.setEmail("account_test@example.com");
        testUser.setLevelCode("Player");
        testUser.setIsTest(true);

        when(userListService.getUsersForPage(any(), eq(false))).thenReturn(List.of(user1, testUser));

        mockMvc.perform(get("/api/users/page")
                        .param("excludeTestData", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                // Assert: có user test trong response.
                .andExpect(jsonPath("$[?(@.isTest==true)]").isNotEmpty());
    }
}
