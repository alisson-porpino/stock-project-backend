package com.stockproject.stock.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for the Product REST API.
 * Uses REST Assured to make real HTTP calls to the running Quarkus application.
 *
 * These tests verify that:
 * - HTTP status codes are correct
 * - JSON response structure is correct
 * - Validation works (400 on invalid input)
 * - 404 is returned for non-existent resources
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceTest {

    private static Long createdProductId;

    @Test
    @Order(1)
    void testCreateProduct_returns201() {
        createdProductId = given()
            .contentType("application/json")
            .body("{\"name\": \"Test Product\", \"value\": 15.50}")
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("name", equalTo("Test Product"))
            .body("value", equalTo(15.50f))
            .body("id", notNullValue())
        .extract()
            .jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    void testListProducts_returnsNonEmptyList() {
        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    void testFindById_returnsProduct() {
        given()
        .when()
            .get("/api/products/" + createdProductId)
        .then()
            .statusCode(200)
            .body("id", equalTo(createdProductId.intValue()))
            .body("name", equalTo("Test Product"));
    }

    @Test
    @Order(4)
    void testUpdateProduct_returnsUpdated() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"Updated Product\", \"value\": 20.00}")
        .when()
            .put("/api/products/" + createdProductId)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Product"))
            .body("value", equalTo(20.00f));
    }

    @Test
    @Order(5)
    void testDeleteProduct_returns204() {
        given()
        .when()
            .delete("/api/products/" + createdProductId)
        .then()
            .statusCode(204);
    }

    @Test
    @Order(6)
    void testFindDeletedProduct_returns404() {
        given()
        .when()
            .get("/api/products/" + createdProductId)
        .then()
            .statusCode(404);
    }

    @Test
    @Order(7)
    void testCreateProduct_withEmptyName_returns400() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"\", \"value\": 10}")
        .when()
            .post("/api/products")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(8)
    void testCreateProduct_withNegativeValue_returns400() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"Bad Product\", \"value\": -5}")
        .when()
            .post("/api/products")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(9)
    void testFindNonExistent_returns404() {
        given()
        .when()
            .get("/api/products/99999")
        .then()
            .statusCode(404);
    }
}
