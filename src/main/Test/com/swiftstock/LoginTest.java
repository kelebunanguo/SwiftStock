package com.swiftstock;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginTest {

    private static String baseUrl = "http://localhost:9090/swiftstock";
    public static String token;   // 静态变量


    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = baseUrl;
    }

    @Test
    public void testLoginAndGetToken(){
        Response response =given()
            .header("Content-Type", "application/json")
            .body("{\"username\":\"admin\", \"password\":\"admin\"}")
    .when()
            .post("/auth/login")
    .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.token", notNullValue())
            .extract().response();
        token = response.jsonPath().getString("data.token");
        System.out.println("✅ 登录成功！Token已获取（长度：" + token.length() + "）");
        System.out.println("Token: " + token);
    }
}
