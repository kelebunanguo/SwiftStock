package com.swiftstock;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class InventoryTest extends BaseTest {

    @Test
    public void testInventoryIn() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"productId\":1, \"quantity\":10, \"reason\":\"自动化测试入库\"}")
        .when()
                .post("/inventory/in")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all();
    }
}