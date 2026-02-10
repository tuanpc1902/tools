package com.example.crud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho trang danh sách user.
 * Bao gồm 15 tham số tương ứng stored procedure cũ (SP 15 params).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListRequest {

    private String name;
    private String email;
    private String phone;
    private String status;
    private String levelCode;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
    private String createdFrom;
    private String createdTo;
    private String updatedFrom;
    private String updatedTo;
    private Long minId;
    private Long maxId;
}
