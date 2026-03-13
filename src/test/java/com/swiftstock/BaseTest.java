package com.swiftstock;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.given;

public class BaseTest {

    protected static String token;

    @BeforeAll
    public static void loginAndGetToken() {
        RestAssured.baseURI = "http://localhost:9090/swiftstock";

        Response response = given()
                .header("Content-Type", "application/json")
                .body("{\"username\":\"admin\", \"password\":\"admin\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().response();

        token = response.jsonPath().getString("data.token");
        System.out.println("✅ BaseTest自动登录成功！Token已全局可用");
    }
}