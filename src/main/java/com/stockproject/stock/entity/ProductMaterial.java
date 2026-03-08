package com.stockproject.stock.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Entity
@Table(name = "product_material", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "raw_material_id"})
})
public class ProductMaterial extends PanacheEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_material_id", nullable = false)
    public RawMaterial rawMaterial;

    @NotNull(message = "Quantity needed is required")
    @Positive(message = "Quantity needed must be positive")
    @Column(name = "quantity_needed", nullable = false, precision = 10, scale = 2)
    public BigDecimal quantityNeeded;
}
