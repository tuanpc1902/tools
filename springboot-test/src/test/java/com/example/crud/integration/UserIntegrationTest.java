package com.example.crud.integration;

import com.example.crud.dto.UserDTO;
import com.example.crud.entity.User;
import com.example.crud.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full Integration Test - Test toàn bộ flow từ Controller -> Service -> Repository
 * Sử dụng @SpringBootTest để load full application context
 * Sử dụng real database (H2 in-memory)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Full Integration Tests - End to End")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

        @Autowired
        private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Xóa dữ liệu test trước mỗi test
                jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("Integration Test: CREATE -> READ -> UPDATE -> DELETE")
    void testFullCrudFlow() throws Exception {
        // 1. CREATE
        UserDTO newUser = new UserDTO();
        newUser.setName("Nguyễn Văn A");
        newUser.setEmail("nguyenvana@example.com");
        newUser.setPhone("0123456789");
        newUser.setStatus("ACTIVE");

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.email").value("nguyenvana@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDTO createdUser = objectMapper.readValue(response, UserDTO.class);
        Long userId = createdUser.getId();

        // 2. READ
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Nguyễn Văn A"));

        // 3. UPDATE
        UserDTO updateUser = new UserDTO();
        updateUser.setName("Nguyễn Văn A Updated");
        updateUser.setEmail("nguyenvana@example.com");
        updateUser.setPhone("0111111111");
        updateUser.setStatus("ACTIVE");

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nguyễn Văn A Updated"));

        // 4. DELETE
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Verify đã xóa
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test: CREATE multiple users -> READ ALL")
    void testCreateMultipleUsersAndReadAll() throws Exception {
        // CREATE user 1
        UserDTO user1 = new UserDTO();
        user1.setName("Nguyễn Văn A");
        user1.setEmail("nguyenvana@example.com");
        user1.setPhone("0123456789");
        user1.setStatus("ACTIVE");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        // CREATE user 2
        UserDTO user2 = new UserDTO();
        user2.setName("Trần Thị B");
        user2.setEmail("tranthib@example.com");
        user2.setPhone("0987654321");
        user2.setStatus("ACTIVE");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        // READ ALL
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Integration Test: SEARCH users by name")
    void testSearchUsersByName() throws Exception {
        // CREATE users
        UserDTO user1 = new UserDTO();
        user1.setName("Nguyễn Văn A");
        user1.setEmail("nguyenvana@example.com");
        user1.setStatus("ACTIVE");

        UserDTO user2 = new UserDTO();
        user2.setName("Trần Thị B");
        user2.setEmail("tranthib@example.com");
        user2.setStatus("ACTIVE");

        UserDTO user3 = new UserDTO();
        user3.setName("Nguyễn Thị C");
        user3.setEmail("nguyenthic@example.com");
        user3.setStatus("ACTIVE");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andExpect(status().isCreated());

        // SEARCH
        mockMvc.perform(get("/api/users/search")
                        .param("name", "Nguyễn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
