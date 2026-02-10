package com.example.crud.controller;

import com.example.crud.dto.InventoryDTO;
import com.example.crud.dto.ProductDTO;
import com.example.crud.service.InventoryService;
import com.example.crud.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/products - success")
    void createProductSuccess() throws Exception {
        ProductDTO request = new ProductDTO(null, "SKU-001", "Prod", "Desc", new BigDecimal("1000"), "VND", "ACTIVE");
        ProductDTO response = new ProductDTO(1L, "SKU-001", "Prod", "Desc", new BigDecimal("1000"), "VND", "ACTIVE");
        when(productService.createProduct(any(ProductDTO.class), any(InventoryDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("quantity", "10"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/products - validation error")
    void createProductValidationError() throws Exception {
        ProductDTO request = new ProductDTO();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
