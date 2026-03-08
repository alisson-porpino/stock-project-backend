package com.stockproject.stock.repository;

import com.stockproject.stock.entity.ProductMaterial;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductMaterialRepository implements PanacheRepository<ProductMaterial> {

    public List<ProductMaterial> findByProductId(Long productId) {
        return list("product.id", productId);
    }

    public ProductMaterial findByProductAndMaterial(Long productId, Long rawMaterialId) {
        return find("product.id = ?1 and rawMaterial.id = ?2", productId, rawMaterialId).firstResult();
    }

    public void deleteByProductId(Long productId) {
        delete("product.id", productId);
    }
}
