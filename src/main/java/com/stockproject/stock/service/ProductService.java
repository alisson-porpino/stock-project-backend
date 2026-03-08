package com.stockproject.stock.service;

import com.stockproject.stock.dto.ProductDTO;
import com.stockproject.stock.entity.Product;
import com.stockproject.stock.repository.ProductMaterialRepository;
import com.stockproject.stock.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMaterialRepository productMaterialRepository;

    /**
     * Converts a Product entity to a ProductDTO.
     * This keeps the entity (database model) separate from what the API returns.
     */
    private ProductDTO toDTO(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.id = entity.id;
        dto.name = entity.name;
        dto.value = entity.value;
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        Product entity = new Product();
        entity.name = dto.name;
        entity.value = dto.value;
        return entity;
    }

    public List<ProductDTO> listAll() {
        return productRepository.listAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        return toDTO(product);
    }

    @Transactional
    public ProductDTO create(ProductDTO dto) {
        Product product = toEntity(dto);
        productRepository.persist(product);
        return toDTO(product);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        product.name = dto.name;
        product.value = dto.value;
        return toDTO(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        productMaterialRepository.deleteByProductId(id);
        productRepository.delete(product);
    }
}
