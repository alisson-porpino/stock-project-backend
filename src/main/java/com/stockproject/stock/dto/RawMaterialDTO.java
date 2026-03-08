package com.stockproject.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class RawMaterialDTO {

    public Long id;

    @NotBlank(message = "Raw material name is required")
    public String name;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    public BigDecimal stockQuantity;
}
