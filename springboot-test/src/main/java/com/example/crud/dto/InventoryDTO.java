package com.example.crud.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {

    @NotNull(message = "ProductId không được để trống")
    private Long productId;

    @Min(value = 0, message = "Số lượng tồn tối thiểu 0")
    private Integer quantityOnHand;

    @Min(value = 0, message = "Số lượng giữ chỗ tối thiểu 0")
    private Integer reserved;

    @Min(value = 0, message = "Mức đặt lại tối thiểu 0")
    private Integer reorderLevel;
}
