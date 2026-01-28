package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public abstract class BaseApiClient {

    protected final ApiConfig apiConfig;
    

    protected BaseApiClient(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        RestAssured.baseURI = apiConfig.baseUrl();
    }

    protected RequestSpecification spec() {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Accept", "application/json, text/plain, */*")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().ifValidationFails(); // quan trọng
    }
}
