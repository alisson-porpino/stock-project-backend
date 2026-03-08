package com.stockproject.stock.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductionSuggestionResourceTest {

    private static Long resinId;
    private static Long dyeId;
    private static Long premiumId;
    private static Long standardId;

    @Inject
    EntityManager em;

    @Transactional
    void cleanDatabase() {
        em.createNativeQuery("DELETE FROM product_material").executeUpdate();
        em.createNativeQuery("DELETE FROM product").executeUpdate();
        em.createNativeQuery("DELETE FROM raw_material").executeUpdate();
    }

    @Test
    @Order(1)
    void setupTestData() {
        // Clean any residual data from other tests
        cleanDatabase();

        // Create raw materials
        resinId = given()
            .contentType("application/json")
            .body("{\"name\": \"Resin\", \"stockQuantity\": 100}")
        .when()
            .post("/api/raw-materials")
        .then()
            .statusCode(201)
        .extract()
            .jsonPath().getLong("id");

        dyeId = given()
            .contentType("application/json")
            .body("{\"name\": \"Dye\", \"stockQuantity\": 50}")
        .when()
            .post("/api/raw-materials")
        .then()
            .statusCode(201)
        .extract()
            .jsonPath().getLong("id");

        // Create products
        premiumId = given()
            .contentType("application/json")
            .body("{\"name\": \"Premium\", \"value\": 30.00}")
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
        .extract()
            .jsonPath().getLong("id");

        standardId = given()
            .contentType("application/json")
            .body("{\"name\": \"Standard\", \"value\": 10.00}")
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
        .extract()
            .jsonPath().getLong("id");

        // Associate: Premium needs 5 Resin + 2 Dye
        given()
            .contentType("application/json")
            .body("{\"rawMaterialId\": " + resinId + ", \"quantityNeeded\": 5}")
        .when()
            .post("/api/products/" + premiumId + "/materials")
        .then()
            .statusCode(201);

        given()
            .contentType("application/json")
            .body("{\"rawMaterialId\": " + dyeId + ", \"quantityNeeded\": 2}")
        .when()
            .post("/api/products/" + premiumId + "/materials")
        .then()
            .statusCode(201);

        // Associate: Standard needs 3 Resin
        given()
            .contentType("application/json")
            .body("{\"rawMaterialId\": " + resinId + ", \"quantityNeeded\": 3}")
        .when()
            .post("/api/products/" + standardId + "/materials")
        .then()
            .statusCode(201);
    }

    @Test
    @Order(2)
    void testSuggestionEndpoint_returnsCorrectCalculation() {
        // Premium (R$30): min(100/5, 50/2) = min(20, 25) = 20 units → R$600
        // After: Resin=0, Dye=10
        // Standard (R$10): min(0/3) = 0 units
        given()
        .when()
            .get("/api/production-suggestions")
        .then()
            .statusCode(200)
            .body("suggestions.size()", greaterThanOrEqualTo(1))
            .body("suggestions[0].productName", equalTo("Premium"))
            .body("suggestions[0].quantity", equalTo(20))
            .body("totalValue", equalTo(600.00f));
    }

    @Test
    @Order(3)
    void cleanupTestData() {
        cleanDatabase();
    }
}