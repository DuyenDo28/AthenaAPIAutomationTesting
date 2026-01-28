package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import io.restassured.response.Response;

public class PositionApiClient extends BaseApiClient {

    private final TokenProvider tokenProvider;

    public PositionApiClient(ApiConfig apiConfig, TokenProvider tokenProvider) {
        super(apiConfig);
        this.tokenProvider = tokenProvider;
    }

    public Response getPositions() {
        return spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .get("/api/Positions");
    }
}
