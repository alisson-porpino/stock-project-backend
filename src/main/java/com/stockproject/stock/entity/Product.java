package com.stockproject.stock.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product extends PanacheEntity {

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    public String name;

    @NotNull(message = "Product value is required")
    @Positive(message = "Product value must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal value;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<ProductMaterial> materials = new ArrayList<>();
}
