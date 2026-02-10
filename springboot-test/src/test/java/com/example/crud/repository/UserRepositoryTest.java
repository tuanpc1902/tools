package com.example.crud.repository;

import com.example.crud.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test cho UserRepository với JdbcTemplate
 * Sử dụng @JdbcTest để test JDBC layer với in-memory database
 */
@JdbcTest
@Import(UserRepository.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DisplayName("UserRepository Integration Tests - JdbcTemplate")
class UserRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        // Xóa dữ liệu test trước mỗi test
        jdbcTemplate.update("DELETE FROM users");
        
        user = new User();
        user.setName("Nguyễn Văn A");
        user.setEmail("nguyenvana@example.com");
        user.setPhone("0123456789");
        user.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Test CREATE user với JdbcTemplate - thành công")
    void testCreateUserWithJdbcTemplate_Success() {
        // When
        User savedUser = userRepository.createUserWithJdbcTemplate(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(user.getEmail(), savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Test FIND BY ID với JdbcTemplate - thành công")
    void testGetUserByIdWithJdbcTemplate_Success() {
        // Given
        User savedUser = userRepository.createUserWithJdbcTemplate(user);

        // When
        Optional<User> foundUser = userRepository.getUserByIdWithJdbcTemplate(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getName(), foundUser.get().getName());
    }

    @Test
    @DisplayName("Test FIND BY ID - không tìm thấy")
    void testGetUserById_NotFound() {
        // When
        Optional<User> foundUser = userRepository.getUserByIdWithJdbcTemplate(999L);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Test FIND ALL với JdbcTemplate - lấy tất cả users")
    void testGetAllUsersWithJdbcTemplate_Success() {
        // Given
        User user2 = new User();
        user2.setName("Trần Thị B");
        user2.setEmail("tranthib@example.com");
        user2.setPhone("0987654321");
        user2.setStatus("ACTIVE");

        userRepository.createUserWithJdbcTemplate(user);
        userRepository.createUserWithJdbcTemplate(user2);

        // When
        List<User> users = userRepository.getAllUsersWithJdbcTemplate();

        // Then
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Test FIND BY EMAIL - thành công")
    void testFindByEmail_Success() {
        // Given
        User savedUser = userRepository.createUserWithJdbcTemplate(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(user.getEmail(), foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Test FIND BY EMAIL - không tìm thấy")
    void testFindByEmail_NotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Test FIND BY NAME CONTAINING - tìm kiếm theo tên")
    void testFindByNameContaining_Success() {
        // Given
        User user2 = new User();
        user2.setName("Nguyễn Thị C");
        user2.setEmail("nguyenthic@example.com");
        user2.setStatus("ACTIVE");

        userRepository.createUserWithJdbcTemplate(user);
        userRepository.createUserWithJdbcTemplate(user2);

        // When
        List<User> users = userRepository.findByNameContaining("Nguyễn");

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getName().toLowerCase().contains("nguyễn")));
    }

    @Test
    @DisplayName("Test EXISTS BY EMAIL - email tồn tại")
    void testExistsByEmail_True() {
        // Given
        userRepository.createUserWithJdbcTemplate(user);

        // When
        boolean exists = userRepository.existsByEmail(user.getEmail());

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test EXISTS BY EMAIL - email không tồn tại")
    void testExistsByEmail_False() {
        // When
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Test UPDATE với JdbcTemplate - thành công")
    void testUpdateUserWithJdbcTemplate_Success() {
        // Given
        User savedUser = userRepository.createUserWithJdbcTemplate(user);
        savedUser.setName("Nguyễn Văn A Updated");
        savedUser.setPhone("0111111111");

        // When
        User updatedUser = userRepository.updateUserWithJdbcTemplate(savedUser);

        // Then
        assertEquals("Nguyễn Văn A Updated", updatedUser.getName());
        assertEquals("0111111111", updatedUser.getPhone());
    }

    @Test
    @DisplayName("Test DELETE với JdbcTemplate - xóa thành công")
    void testDeleteUserWithJdbcTemplate_Success() {
        // Given
        User savedUser = userRepository.createUserWithJdbcTemplate(user);
        Long userId = savedUser.getId();

        // When
        userRepository.deleteUserWithJdbcTemplate(userId);

        // Then
        Optional<User> deletedUser = userRepository.getUserByIdWithJdbcTemplate(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @DisplayName("Test COUNT - đếm số lượng users")
    void testCount_Success() {
        // Given
        User user2 = new User();
        user2.setName("Trần Thị B");
        user2.setEmail("tranthib@example.com");
        user2.setStatus("ACTIVE");

        userRepository.createUserWithJdbcTemplate(user);
        userRepository.createUserWithJdbcTemplate(user2);

        // When
        long count = userRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Test EXISTS BY ID - user tồn tại")
    void testExistsById_True() {
        // Given
        User savedUser = userRepository.createUserWithJdbcTemplate(user);

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test EXISTS BY ID - user không tồn tại")
    void testExistsById_False() {
        // When
        boolean exists = userRepository.existsById(999L);

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Test DYNAMIC SEARCH - tìm kiếm với nhiều điều kiện")
    void testFindUsersDynamic_Success() {
        // Given
        User user2 = new User();
        user2.setName("Trần Thị B");
        user2.setEmail("tranthib@example.com");
        user2.setPhone("0987654321");
        user2.setStatus("ACTIVE");

        userRepository.createUserWithJdbcTemplate(user);
        userRepository.createUserWithJdbcTemplate(user2);

        // When - Tìm theo name
        List<User> usersByName = userRepository.findUsersDynamic("Nguyễn", null, null);
        
        // Then
        assertEquals(1, usersByName.size());
        assertEquals("Nguyễn Văn A", usersByName.get(0).getName());

        // When - Tìm theo email
        List<User> usersByEmail = userRepository.findUsersDynamic(null, "tranthib", null);
        
        // Then
        assertEquals(1, usersByEmail.size());
        assertEquals("Trần Thị B", usersByEmail.get(0).getName());

        // When - Tìm với nhiều điều kiện
        List<User> usersMultiple = userRepository.findUsersDynamic(null, null, "0123");
        
        // Then
        assertEquals(1, usersMultiple.size());
    }

    @Test
    @DisplayName("Test PAGINATION - lấy users với phân trang")
    void testFindAllWithPagination_Success() {
        // Given - Tạo 5 users
        for (int i = 1; i <= 5; i++) {
            User u = new User();
            u.setName("User " + i);
            u.setEmail("user" + i + "@example.com");
            u.setPhone("012345678" + i);
            u.setStatus("ACTIVE");
            userRepository.createUserWithJdbcTemplate(u);
        }

        // When - Lấy page 0, size 2
        List<User> page1 = userRepository.findAllWithPagination(0, 2);
        
        // Then
        assertEquals(2, page1.size());

        // When - Lấy page 1, size 2
        List<User> page2 = userRepository.findAllWithPagination(1, 2);
        
        // Then
        assertEquals(2, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }
}
