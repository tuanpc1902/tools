package com.example.crud.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "UserId không được để trống")
    private Long userId;

    @Pattern(regexp = "[A-Z]{3}", message = "Tiền tệ phải là mã 3 ký tự (VD: VND)")
    private String currency;

    @Size(min = 1, message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    private List<@Valid OrderItemRequest> items;
}
