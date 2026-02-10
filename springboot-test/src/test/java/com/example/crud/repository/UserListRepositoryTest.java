package com.example.crud.repository;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.UserListRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// @JdbcTest: load JDBC slice (JdbcTemplate + DataSource) cho test repository.
@JdbcTest
// @Import: register repository cần test trong context.
@Import(UserListRepository.class)
// @ActiveProfiles: dùng application-test.properties cho DB giả.
@ActiveProfiles("test")
// @AutoConfigureTestDatabase: giữ cấu hình DB test (H2) thay vì auto replace.
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DisplayName("UserListRepository JDBC Tests")
class UserListRepositoryTest {

    @Autowired
    private UserListRepository userListRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Exclude test data -> không trả về account_test")
    void fetchUsersExcludeTest_ExcludeTrue() {
        // Test case: excludeTestData = true -> lọc bỏ is_test = 1.
        String tag = "RepoTest-" + System.currentTimeMillis();
        insertUser(tag + "-Real", tag + "-real@example.com", false, "PO");
        insertUser(tag + "-Test", tag + "-test@example.com", true, "Player");
        UserListRequest request = new UserListRequest();
        request.setName(tag);
        request.setPage(0);
        request.setSize(10);

        List<UserDTO> result = userListRepository.fetchUsersExcludeTest(request, true);

        // Assert: không có user test trong kết quả.
        assertTrue(result.stream().noneMatch(user -> Boolean.TRUE.equals(user.getIsTest())));
        // Assert: user thật được trả về.
        assertTrue(result.stream().anyMatch(user -> (tag + "-real@example.com").equals(user.getEmail())));
    }

    @Test
    @DisplayName("Include test data -> có account_test")
    void fetchUsersExcludeTest_ExcludeFalse() {
        // Test case: excludeTestData = false -> bao gồm account_test.
        String tag = "RepoTest-" + System.currentTimeMillis();
        insertUser(tag + "-Real", tag + "-real@example.com", false, "PO");
        insertUser(tag + "-Test", tag + "-test@example.com", true, "Player");
        UserListRequest request = new UserListRequest();
        request.setName(tag);
        request.setPage(0);
        request.setSize(10);

        List<UserDTO> result = userListRepository.fetchUsersExcludeTest(request, false);

        // Assert: có user test trong kết quả.
        assertTrue(result.stream().anyMatch(user -> Boolean.TRUE.equals(user.getIsTest())));
        // Assert: account_test được trả về theo email đã insert.
        assertTrue(result.stream().anyMatch(user -> (tag + "-test@example.com").equals(user.getEmail())));
    }

    @Test
    @DisplayName("Legacy SP -> luôn bao gồm account_test")
    void fetchUsersLegacy_IncludesTestAccount() {
        // Test case: SP cũ không có param excludeTestData -> luôn trả về account_test.
        String tag = "RepoTest-" + System.currentTimeMillis();
        insertUser(tag + "-Real", tag + "-real@example.com", false, "PO");
        insertUser(tag + "-Test", tag + "-test@example.com", true, "Player");
        UserListRequest request = new UserListRequest();
        request.setName(tag);
        request.setPage(0);
        request.setSize(10);

        List<UserDTO> result = userListRepository.fetchUsersLegacy(request);

        // Assert: SP cũ luôn trả về cả user test.
        assertTrue(result.stream().anyMatch(user -> Boolean.TRUE.equals(user.getIsTest())));
    }

    @Test
    @DisplayName("Negative page/size -> clamp về 0/1")
    void fetchUsersExcludeTest_NegativePagination() {
        // Test case: dữ liệu input xấu (page/size âm) vẫn chạy an toàn.
        String tag = "RepoTest-" + System.currentTimeMillis();
        insertUser(tag + "-Real", tag + "-real@example.com", false, "PO");
        insertUser(tag + "-Test", tag + "-test@example.com", true, "Player");
        UserListRequest request = new UserListRequest();
        request.setName(tag);
        request.setPage(-1);
        request.setSize(-5);

        List<UserDTO> result = userListRepository.fetchUsersExcludeTest(request, true);

        // Assert: exclude true -> không có test user.
        assertTrue(result.stream().noneMatch(user -> Boolean.TRUE.equals(user.getIsTest())));
        // Assert: user thật được trả về.
        assertTrue(result.stream().anyMatch(user -> (tag + "-real@example.com").equals(user.getEmail())));
    }

    private void insertUser(String name, String email, boolean isTest, String levelCode) {
        jdbcTemplate.update(
                "INSERT INTO users (name, email, phone, status, level_code, is_test, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
                name,
                email,
                "0900000000",
                "ACTIVE",
                levelCode,
                isTest ? 1 : 0
        );
    }
}
