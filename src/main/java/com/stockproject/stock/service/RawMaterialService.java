package com.stockproject.stock.service;

import com.stockproject.stock.dto.RawMaterialDTO;
import com.stockproject.stock.entity.RawMaterial;
import com.stockproject.stock.repository.RawMaterialRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RawMaterialService {

    @Inject
    RawMaterialRepository rawMaterialRepository;

    private RawMaterialDTO toDTO(RawMaterial entity) {
        RawMaterialDTO dto = new RawMaterialDTO();
        dto.id = entity.id;
        dto.name = entity.name;
        dto.stockQuantity = entity.stockQuantity;
        return dto;
    }

    private RawMaterial toEntity(RawMaterialDTO dto) {
        RawMaterial entity = new RawMaterial();
        entity.name = dto.name;
        entity.stockQuantity = dto.stockQuantity;
        return entity;
    }

    public List<RawMaterialDTO> listAll() {
        return rawMaterialRepository.listAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RawMaterialDTO findById(Long id) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(id);
        if (rawMaterial == null) {
            throw new NotFoundException("Raw material not found with id: " + id);
        }
        return toDTO(rawMaterial);
    }

    @Transactional
    public RawMaterialDTO create(RawMaterialDTO dto) {
        RawMaterial rawMaterial = toEntity(dto);
        rawMaterialRepository.persist(rawMaterial);
        return toDTO(rawMaterial);
    }

    @Transactional
    public RawMaterialDTO update(Long id, RawMaterialDTO dto) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(id);
        if (rawMaterial == null) {
            throw new NotFoundException("Raw material not found with id: " + id);
        }
        rawMaterial.name = dto.name;
        rawMaterial.stockQuantity = dto.stockQuantity;
        return toDTO(rawMaterial);
    }

    @Transactional
    public void delete(Long id) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(id);
        if (rawMaterial == null) {
            throw new NotFoundException("Raw material not found with id: " + id);
        }
        rawMaterialRepository.delete(rawMaterial);
    }
}
