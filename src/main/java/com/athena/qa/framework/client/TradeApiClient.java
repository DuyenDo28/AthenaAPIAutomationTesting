package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import io.restassured.response.Response;

public class TradeApiClient extends BaseApiClient {

    private final TokenProvider tokenProvider;

    public TradeApiClient(ApiConfig apiConfig, TokenProvider tokenProvider) {
        super(apiConfig);
        this.tokenProvider = tokenProvider;
    }

    public Response getAllTrades() {
        return spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .get("/api/Trades");
    }

    public Response createTrade(Object body) {
        return spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .body(body)
                .post("/api/Trades");
    }

    public Response updateTrade(Object body) {
        return spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .body(body)
                .put("/api/Trades");
    }

    //tradehist asyn
    public Response startHistoricalAsync(String startDate, String endDate) {
        return spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .post("/api/HistoricalTrades/AsyncFull"
                        + "?startDate=" + startDate
                        + "&endDate=" + endDate);
    }

    public Response getHistoricalAsyncResult(String operationId) {
        return spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .get("/api/HistoricalTrades/AsyncResults?OperationId=" + operationId);
    }


}
