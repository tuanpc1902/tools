package com.example.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    private Long userId;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationalId;
    private String jobTitle;
    private String company;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
