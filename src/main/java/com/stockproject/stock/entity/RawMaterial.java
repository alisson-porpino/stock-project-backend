package com.stockproject.stock.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Entity
@Table(name = "raw_material")
public class RawMaterial extends PanacheEntity {

    @NotBlank(message = "Raw material name is required")
    @Column(nullable = false)
    public String name;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false, precision = 10, scale = 2)
    public BigDecimal stockQuantity = BigDecimal.ZERO;
}
