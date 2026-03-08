package com.stockproject.stock.service;

import com.stockproject.stock.dto.ProductionSuggestionDTO;
import com.stockproject.stock.dto.ProductionSuggestionDTO.SuggestionItem;
import com.stockproject.stock.dto.RawMaterialDTO;
import com.stockproject.stock.dto.ProductDTO;
import com.stockproject.stock.dto.ProductMaterialDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductionSuggestionServiceTest {

    @Inject
    ProductionSuggestionService suggestionService;

    @Inject
    ProductService productService;

    @Inject
    RawMaterialService rawMaterialService;

    @Inject
    ProductMaterialService productMaterialService;

    @Inject
    EntityManager em;

    /**
     * Uses native SQL to clean all tables in the correct order.
     * This avoids Hibernate cascade conflicts that happen when
     * deleting entities through JPA while cascade is configured.
     * 
     * I will optimze this later by using @BeforeEach and @AfterEach
     * to ensure a clean state for each test, but for now i will call it manually
     * in each test to have more control over when the database is cleaned.
     */
    @Transactional
    void cleanDatabase() {
        em.createNativeQuery("DELETE FROM product_material").executeUpdate();
        em.createNativeQuery("DELETE FROM product").executeUpdate();
        em.createNativeQuery("DELETE FROM raw_material").executeUpdate();
    }

    @Test
    @Order(1)
    void testBasicSuggestion_prioritizesHigherValueProduct() {
        cleanDatabase();

        // Setup raw materials
        RawMaterialDTO resin = new RawMaterialDTO();
        resin.name = "Resin";
        resin.stockQuantity = new BigDecimal("100");
        resin = rawMaterialService.create(resin);

        RawMaterialDTO dye = new RawMaterialDTO();
        dye.name = "Dye";
        dye.stockQuantity = new BigDecimal("50");
        dye = rawMaterialService.create(dye);

        // Product A: R$30, needs 5 Resin + 2 Dye
        ProductDTO productA = new ProductDTO();
        productA.name = "Product A (Expensive)";
        productA.value = new BigDecimal("30.00");
        productA = productService.create(productA);

        ProductMaterialDTO pmA1 = new ProductMaterialDTO();
        pmA1.rawMaterialId = resin.id;
        pmA1.quantityNeeded = new BigDecimal("5");
        productMaterialService.addMaterial(productA.id, pmA1);

        ProductMaterialDTO pmA2 = new ProductMaterialDTO();
        pmA2.rawMaterialId = dye.id;
        pmA2.quantityNeeded = new BigDecimal("2");
        productMaterialService.addMaterial(productA.id, pmA2);

        // Product B: R$10, needs 3 Resin
        ProductDTO productB = new ProductDTO();
        productB.name = "Product B (Cheap)";
        productB.value = new BigDecimal("10.00");
        productB = productService.create(productB);

        ProductMaterialDTO pmB1 = new ProductMaterialDTO();
        pmB1.rawMaterialId = resin.id;
        pmB1.quantityNeeded = new BigDecimal("3");
        productMaterialService.addMaterial(productB.id, pmB1);

        // Execute
        ProductionSuggestionDTO result = suggestionService.calculateSuggestion();

        // Verify
        assertNotNull(result);
        assertFalse(result.suggestions.isEmpty());

        // Product A first (higher value): min(100/5, 50/2) = 20 units
        SuggestionItem first = result.suggestions.get(0);
        assertEquals("Product A (Expensive)", first.productName);
        assertEquals(20, first.quantity);
        assertEquals(0, new BigDecimal("600.00").compareTo(first.subtotal));
        assertEquals(0, new BigDecimal("600.00").compareTo(result.totalValue));

        cleanDatabase();
    }

    @Test
    @Order(2)
    void testEmptyStock_returnsNoSuggestions() {
        cleanDatabase();

        RawMaterialDTO resin = new RawMaterialDTO();
        resin.name = "Resin";
        resin.stockQuantity = BigDecimal.ZERO;
        resin = rawMaterialService.create(resin);

        ProductDTO product = new ProductDTO();
        product.name = "Product X";
        product.value = new BigDecimal("50.00");
        product = productService.create(product);

        ProductMaterialDTO pm = new ProductMaterialDTO();
        pm.rawMaterialId = resin.id;
        pm.quantityNeeded = new BigDecimal("5");
        productMaterialService.addMaterial(product.id, pm);

        ProductionSuggestionDTO result = suggestionService.calculateSuggestion();

        assertNotNull(result);
        assertTrue(result.suggestions.isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.totalValue));

        cleanDatabase();
    }

    @Test
    @Order(3)
    void testProductWithNoMaterials_isSkipped() {
        cleanDatabase();

        ProductDTO product = new ProductDTO();
        product.name = "Orphan Product";
        product.value = new BigDecimal("100.00");
        productService.create(product);

        ProductionSuggestionDTO result = suggestionService.calculateSuggestion();

        assertNotNull(result);
        assertTrue(result.suggestions.isEmpty());

        cleanDatabase();
    }

    @Test
    @Order(4)
    void testMultipleProducts_greedyAllocation() {
        cleanDatabase();

        RawMaterialDTO resin = new RawMaterialDTO();
        resin.name = "Resin";
        resin.stockQuantity = new BigDecimal("20");
        resin = rawMaterialService.create(resin);

        // Product A: R$50, needs 3 Resin
        ProductDTO productA = new ProductDTO();
        productA.name = "Expensive";
        productA.value = new BigDecimal("50.00");
        productA = productService.create(productA);

        ProductMaterialDTO pmA = new ProductMaterialDTO();
        pmA.rawMaterialId = resin.id;
        pmA.quantityNeeded = new BigDecimal("3");
        productMaterialService.addMaterial(productA.id, pmA);

        // Product B: R$20, needs 4 Resin
        ProductDTO productB = new ProductDTO();
        productB.name = "Cheaper";
        productB.value = new BigDecimal("20.00");
        productB = productService.create(productB);

        ProductMaterialDTO pmB = new ProductMaterialDTO();
        pmB.rawMaterialId = resin.id;
        pmB.quantityNeeded = new BigDecimal("4");
        productMaterialService.addMaterial(productB.id, pmB);

        ProductionSuggestionDTO result = suggestionService.calculateSuggestion();

        // Expensive: floor(20/3) = 6 → consumes 18, leaves 2
        // Cheaper: floor(2/4) = 0
        assertNotNull(result);
        assertEquals(1, result.suggestions.size());

        SuggestionItem first = result.suggestions.get(0);
        assertEquals("Expensive", first.productName);
        assertEquals(6, first.quantity);
        assertEquals(0, new BigDecimal("300.00").compareTo(first.subtotal));
        assertEquals(0, new BigDecimal("300.00").compareTo(result.totalValue));

        cleanDatabase();
    }

    @Test
    @Order(5)
    void testEmptyDatabase_returnsEmptySuggestion() {
        cleanDatabase();

        ProductionSuggestionDTO result = suggestionService.calculateSuggestion();

        assertNotNull(result);
        assertTrue(result.suggestions.isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.totalValue));
    }
}