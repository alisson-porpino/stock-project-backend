package com.stockproject.stock.service;

import com.stockproject.stock.dto.ProductionSuggestionDTO;
import com.stockproject.stock.dto.ProductionSuggestionDTO.SuggestionItem;
import com.stockproject.stock.entity.Product;
import com.stockproject.stock.entity.ProductMaterial;
import com.stockproject.stock.repository.ProductMaterialRepository;
import com.stockproject.stock.repository.ProductRepository;
import com.stockproject.stock.repository.RawMaterialRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating which products can be manufactured
 * with the available raw materials in stock.
 *
 * ALGORITHM: Greedy
 * ─────────────────────────
 * The problem statement that i used to create
 * this project explicitly says: "the prioritization must be by the
 * highest value products". This is a textbook Greedy approach:
 *
 * 1. Sort all products by value DESCENDING
 * 2. For each product, calculate how many units can be produced
 *    given the CURRENT (virtual) stock
 * 3. "Consume" the raw materials from the virtual stock
 * 4. Move to the next product with the remaining stock
 *
 * I could go for an optimal solution like Linear Programming. However,
 * the problem asks for prioritization by value, not global optimization.
 * A greedy approach satisfies the requirement directly. If this were an
 * optimization problem (maximize total revenue), we would need Integer
 * Linear Programming (ILP), which is NP-hard in the general case.
 *
 * TIME COMPLEXITY: O(P * M) where P = number of products, M = max materials per product
 * SPACE COMPLEXITY: O(R) where R = number of distinct raw materials (for the virtual stock map)
 */
@ApplicationScoped
public class ProductionSuggestionService {

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMaterialRepository productMaterialRepository;

    @Inject
    RawMaterialRepository rawMaterialRepository;

    public ProductionSuggestionDTO calculateSuggestion() {

        // ──────────────────────────────────────────────
        // STEP 1: Build a virtual copy of the stock
        // ──────────────────────────────────────────────
        // We use a HashMap to simulate stock consumption without
        // modifying the actual database. Key = rawMaterialId, Value = available quantity.
        Map<Long, BigDecimal> virtualStock = new HashMap<>();
        rawMaterialRepository.listAll().forEach(rm ->
            virtualStock.put(rm.id, rm.stockQuantity)
        );

        // ──────────────────────────────────────────────
        // STEP 2: Get all products and sort by value DESC
        // ──────────────────────────────────────────────
        // The greedy strategy: prioritize the most valuable products first,
        // so they get first pick of the available raw materials.
        List<Product> products = productRepository.listAll()
                .stream()
                .sorted(Comparator.comparing((Product p) -> p.value).reversed())
                .collect(Collectors.toList());

        // ──────────────────────────────────────────────
        // STEP 3: For each product, calculate max producible quantity
        // ──────────────────────────────────────────────
        List<SuggestionItem> suggestions = new ArrayList<>();

        for (Product product : products) {
            List<ProductMaterial> materials = productMaterialRepository.findByProductId(product.id);

            // Skip products with no materials associated
            if (materials.isEmpty()) {
                continue;
            }

            // Calculate: for each raw material needed, how many units of the product
            // can we make? The answer is the MINIMUM across all materials.
            // Example: need 5 Resin (stock: 1000) and 2 Dye (stock: 500)
            //          → min(1000/5, 500/2) = min(200, 250) = 200
            int maxProducible = Integer.MAX_VALUE;

            for (ProductMaterial pm : materials) {
                BigDecimal available = virtualStock.getOrDefault(pm.rawMaterial.id, BigDecimal.ZERO);
                BigDecimal needed = pm.quantityNeeded;

                // Avoid division by zero
                if (needed.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                // Floor division: we can only produce whole units
                int possibleUnits = available.divide(needed, 0, RoundingMode.FLOOR).intValue();
                maxProducible = Math.min(maxProducible, possibleUnits);
            }

            // If maxProducible is still MAX_VALUE, something went wrong; treat as 0
            if (maxProducible == Integer.MAX_VALUE) {
                maxProducible = 0;
            }

            // ──────────────────────────────────────────────
            // STEP 4: "Consume" virtual stock
            // ──────────────────────────────────────────────
            // Only if we can actually produce something
            if (maxProducible > 0) {
                for (ProductMaterial pm : materials) {
                    BigDecimal consumed = pm.quantityNeeded
                            .multiply(BigDecimal.valueOf(maxProducible));
                    BigDecimal remaining = virtualStock.get(pm.rawMaterial.id).subtract(consumed);
                    virtualStock.put(pm.rawMaterial.id, remaining);
                }

                BigDecimal subtotal = product.value
                        .multiply(BigDecimal.valueOf(maxProducible));

                suggestions.add(new SuggestionItem(
                    product.id,
                    product.name,
                    product.value,
                    maxProducible,
                    subtotal
                ));
            }
        }

        // ──────────────────────────────────────────────
        // STEP 5: Calculate total revenue
        // ──────────────────────────────────────────────
        BigDecimal totalValue = suggestions.stream()
                .map(s -> s.subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ProductionSuggestionDTO result = new ProductionSuggestionDTO();
        result.suggestions = suggestions;
        result.totalValue = totalValue;

        return result;
    }
}
