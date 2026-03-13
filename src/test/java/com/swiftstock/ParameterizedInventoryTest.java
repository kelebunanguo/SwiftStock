package com.swiftstock;

import io.restassured.http.ContentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ParameterizedInventoryTest extends BaseTest {

    @ParameterizedTest
    @CsvSource({
        "1, 5, 测试入库5件",     // productId=1, quantity=5
        "2, 10, 测试入库10件",
        "3, 20, 测试入库20件"
    })
    public void testInventoryInWithDifferentData(Long productId, Integer quantity, String reason) {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(String.format("{\"productId\":%d, \"quantity\":%d, \"reason\":\"%s\"}", 
                                  productId, quantity, reason))
        .when()
                .post("/inventory/in")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all();
    }

    @ParameterizedTest
    @CsvSource({
        "1, 5, 测试出库5件",     // productId=1, quantity=5
        "2, 10, 测试出库10件",
        "3, 20, 测试出库20件"
    })
    public void testInventoryOutWithDifferentData(Long productId, Integer quantity, String reason) {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(String.format("{\"productId\":%d, \"quantity\":%d, \"reason\":\"%s\"}",
                                  productId, quantity, reason))
        .when()
                .post("/inventory/out")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all();
    }
}