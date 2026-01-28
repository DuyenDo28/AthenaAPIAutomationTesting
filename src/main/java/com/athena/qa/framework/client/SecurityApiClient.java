package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import io.restassured.response.Response;

public class SecurityApiClient extends BaseApiClient {

    private final TokenProvider tokenProvider;

    public SecurityApiClient(ApiConfig apiConfig, TokenProvider tokenProvider) {
        super(apiConfig);
        this.tokenProvider = tokenProvider;
    }


    /** Lấy tất cả securities */
    public Response getAllSecurities(int maxNumItems) {
        String token = tokenProvider.get(); // token đã clean quotes

        return spec()
                .header("Authorization", "Bearer " + token)
                .queryParam("maxNumItems", maxNumItems)
                .get("/api/Securities");
    }
}
