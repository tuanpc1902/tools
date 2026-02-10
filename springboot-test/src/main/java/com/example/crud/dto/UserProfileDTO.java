package com.example.crud.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    @Past(message = "Ngày sinh phải ở quá khứ")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Giới tính phải là MALE, FEMALE, hoặc OTHER")
    private String gender;

    @Size(max = 30, message = "CMND/CCCD tối đa 30 ký tự")
    private String nationalId;

    @Size(max = 100, message = "Chức danh tối đa 100 ký tự")
    private String jobTitle;

    @Size(max = 120, message = "Công ty tối đa 120 ký tự")
    private String company;

    @Size(max = 500, message = "Giới thiệu tối đa 500 ký tự")
    private String bio;
}
