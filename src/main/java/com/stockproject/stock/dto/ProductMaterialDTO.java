package com.stockproject.stock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ProductMaterialDTO {

    public Long id;

    @NotNull(message = "Raw material ID is required")
    public Long rawMaterialId;

    public String rawMaterialName;

    @NotNull(message = "Quantity needed is required")
    @Positive(message = "Quantity needed must be positive")
    public BigDecimal quantityNeeded;
}
