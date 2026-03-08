package com.stockproject.stock.service;

import com.stockproject.stock.dto.ProductMaterialDTO;
import com.stockproject.stock.entity.Product;
import com.stockproject.stock.entity.ProductMaterial;
import com.stockproject.stock.entity.RawMaterial;
import com.stockproject.stock.repository.ProductMaterialRepository;
import com.stockproject.stock.repository.ProductRepository;
import com.stockproject.stock.repository.RawMaterialRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductMaterialService {

    @Inject
    ProductMaterialRepository productMaterialRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    RawMaterialRepository rawMaterialRepository;

    private ProductMaterialDTO toDTO(ProductMaterial entity) {
        ProductMaterialDTO dto = new ProductMaterialDTO();
        dto.id = entity.id;
        dto.rawMaterialId = entity.rawMaterial.id;
        dto.rawMaterialName = entity.rawMaterial.name;
        dto.quantityNeeded = entity.quantityNeeded;
        return dto;
    }

    public List<ProductMaterialDTO> listByProduct(Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        return productMaterialRepository.findByProductId(productId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductMaterialDTO addMaterial(Long productId, ProductMaterialDTO dto) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        RawMaterial rawMaterial = rawMaterialRepository.findById(dto.rawMaterialId);
        if (rawMaterial == null) {
            throw new NotFoundException("Raw material not found with id: " + dto.rawMaterialId);
        }

        ProductMaterial existing = productMaterialRepository
                .findByProductAndMaterial(productId, dto.rawMaterialId);
        if (existing != null) {
            throw new BadRequestException(
                "Raw material '" + rawMaterial.name + "' is already associated with this product"
            );
        }

        ProductMaterial entity = new ProductMaterial();
        entity.product = product;
        entity.rawMaterial = rawMaterial;
        entity.quantityNeeded = dto.quantityNeeded;
        productMaterialRepository.persist(entity);

        return toDTO(entity);
    }

    @Transactional
    public ProductMaterialDTO updateQuantity(Long productId, Long materialId, ProductMaterialDTO dto) {
        ProductMaterial entity = productMaterialRepository.findByProductAndMaterial(productId, materialId);
        if (entity == null) {
            throw new NotFoundException(
                "Association not found for product " + productId + " and material " + materialId
            );
        }
        entity.quantityNeeded = dto.quantityNeeded;
        return toDTO(entity);
    }

    @Transactional
    public void removeMaterial(Long productId, Long materialId) {
        ProductMaterial entity = productMaterialRepository.findByProductAndMaterial(productId, materialId);
        if (entity == null) {
            throw new NotFoundException(
                "Association not found for product " + productId + " and material " + materialId
            );
        }
        productMaterialRepository.delete(entity);
    }
}
