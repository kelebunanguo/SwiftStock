package com.swiftstock;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class AiRecommandTest extends BaseTest {

    @Test
    public void testAiRecommandWithToken() {

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/api/ai/forecast/recommend-list")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().response();

        System.out.println("✅ AI补货推荐测试通过！返回数量：" + response.jsonPath().getList("data").size());
    }
}