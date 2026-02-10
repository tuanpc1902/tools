package com.example.crud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;

    @NotBlank(message = "Loại địa chỉ không được để trống")
    @Size(max = 20, message = "Loại địa chỉ tối đa 20 ký tự")
    private String type;

    @NotBlank(message = "Địa chỉ dòng 1 không được để trống")
    @Size(max = 120, message = "Địa chỉ dòng 1 tối đa 120 ký tự")
    private String line1;

    @Size(max = 120, message = "Địa chỉ dòng 2 tối đa 120 ký tự")
    private String line2;

    @NotBlank(message = "Thành phố không được để trống")
    @Size(max = 80, message = "Thành phố tối đa 80 ký tự")
    private String city;

    @Size(max = 80, message = "Tỉnh/Thành tối đa 80 ký tự")
    private String state;

    @Size(max = 20, message = "Mã bưu chính tối đa 20 ký tự")
    private String postalCode;

    @NotBlank(message = "Quốc gia không được để trống")
    @Size(max = 80, message = "Quốc gia tối đa 80 ký tự")
    private String country;

    @NotNull(message = "isDefault không được để trống")
    private Boolean isDefault;
}
