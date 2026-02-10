package com.example.crud.repository;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.UserListRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserListRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserListRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Legacy SP (15 params) - không có tham số excludeTestData.
     */
    public List<UserDTO> fetchUsersLegacy(UserListRequest request) {
        return queryUsers(request, false);
    }

    /**
     * New SP (16 params) - tham số thứ 16: excludeTestData.
     */
    public List<UserDTO> fetchUsersExcludeTest(UserListRequest request, boolean excludeTestData) {
        return queryUsers(request, excludeTestData);
    }

    private List<UserDTO> queryUsers(UserListRequest request, boolean excludeTestData) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, email, phone, status, level_code, is_test, created_at, updated_at, deleted_at " +
                        "FROM users WHERE deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (request.getName() != null && !request.getName().isBlank()) {
            sql.append(" AND LOWER(name) LIKE LOWER(?)");
            params.add("%" + request.getName().trim() + "%");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            sql.append(" AND LOWER(email) LIKE LOWER(?)");
            params.add("%" + request.getEmail().trim() + "%");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            sql.append(" AND phone LIKE ?");
            params.add("%" + request.getPhone().trim() + "%");
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            sql.append(" AND status = ?");
            params.add(request.getStatus().trim());
        }
        if (request.getLevelCode() != null && !request.getLevelCode().isBlank()) {
            sql.append(" AND level_code = ?");
            params.add(request.getLevelCode().trim());
        }
        if (excludeTestData) {
            sql.append(" AND is_test = 0");
        }

        sql.append(" ORDER BY CASE level_code " +
                "WHEN 'PO' THEN 1 " +
                "WHEN 'CO' THEN 2 " +
                "WHEN 'Manager' THEN 3 " +
                "WHEN 'Lead' THEN 4 " +
                "WHEN 'Player' THEN 99 " +
                "ELSE 98 END, id");

        int page = request.getPage() != null ? Math.max(0, request.getPage()) : 0;
        int size = request.getSize() != null ? Math.max(1, request.getSize()) : 20;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    private UserDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserDTO dto = new UserDTO();
        dto.setId(rs.getLong("id"));
        dto.setName(rs.getString("name"));
        dto.setEmail(rs.getString("email"));
        dto.setPhone(rs.getString("phone"));
        dto.setStatus(rs.getString("status"));
        dto.setLevelCode(rs.getString("level_code"));
        dto.setIsTest(rs.getBoolean("is_test"));
        dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        dto.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        if (rs.getTimestamp("deleted_at") != null) {
            dto.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
        }
        return dto;
    }
}
