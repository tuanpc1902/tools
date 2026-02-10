package com.example.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private Long productId;
    private Integer quantityOnHand;
    private Integer reserved;
    private Integer reorderLevel;
    private LocalDateTime updatedAt;
}
