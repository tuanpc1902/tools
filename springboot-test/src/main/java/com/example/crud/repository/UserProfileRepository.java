package com.example.crud.repository;

import com.example.crud.entity.UserProfile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserProfileRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserProfile upsertProfile(UserProfile profile) {
        String sql = "INSERT INTO user_profiles (user_id, date_of_birth, gender, national_id, job_title, company, bio) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE date_of_birth = VALUES(date_of_birth), gender = VALUES(gender), " +
                "national_id = VALUES(national_id), job_title = VALUES(job_title), company = VALUES(company), " +
                "bio = VALUES(bio), updated_at = CURRENT_TIMESTAMP";
        jdbcTemplate.update(sql,
                profile.getUserId(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getNationalId(),
                profile.getJobTitle(),
                profile.getCompany(),
                profile.getBio()
        );
        return getByUserId(profile.getUserId()).orElseThrow();
    }

    public Optional<UserProfile> getByUserId(Long userId) {
        String sql = "SELECT user_id, date_of_birth, gender, national_id, job_title, company, bio, created_at, updated_at " +
                "FROM user_profiles WHERE user_id = ?";
        try {
            UserProfile profile = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(UserProfile.class), userId);
            return Optional.ofNullable(profile);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
