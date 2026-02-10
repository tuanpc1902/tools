package com.example.crud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private Long id;

    @NotBlank(message = "Mã permission không được để trống")
    @Size(max = 80, message = "Mã permission tối đa 80 ký tự")
    private String code;

    @NotBlank(message = "Tên permission không được để trống")
    @Size(max = 120, message = "Tên permission tối đa 120 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;
}
