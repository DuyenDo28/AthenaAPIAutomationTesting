package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import io.restassured.response.Response;

import java.time.LocalDate;

public class HistoricalPositionApiClient extends BaseApiClient {

    private final TokenProvider tokenProvider;

    public HistoricalPositionApiClient(ApiConfig apiConfig, TokenProvider tokenProvider) {
        super(apiConfig);
        this.tokenProvider = tokenProvider;
    }

    /**
     * STEP 1: start async -> return operationId
     */
    public String startAsync(LocalDate asOfDate) {

        Response res = spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .queryParam("asOfDate", asOfDate.toString())
                .post("/api/HistoricalPositions/Async");

        int status = res.statusCode();
        if (status != 200 && status != 202) {
            throw new RuntimeException(
                    "Start PositionHist async failed. status=" + status
                            + " body=" + res.asString()
            );
        }

        try {
            String opId = res.jsonPath().getString("operationId");
            if (opId != null && !opId.isBlank()) return opId;
        } catch (Exception ignored) {
        }

        try {
            String opId = res.jsonPath().getString("id");
            if (opId != null && !opId.isBlank()) return opId;
        } catch (Exception ignored) {
        }

        throw new RuntimeException("Cannot extract operationId. body=" + res.asString());
    }

    /**
     * STEP 2: poll async result
     */
    public Response poll(String operationId) throws InterruptedException {

        for (int i = 0; i < apiConfig.maxAttempts(); i++) {

            Response r = spec()
                    .header("Authorization", "Bearer " + tokenProvider.get())
                    .queryParam("OperationId", operationId)
                    .get("/api/HistoricalPositions/AsyncResults");

            System.out.println("Polling attempt " + i + " status=" + r.statusCode());

            if (r.statusCode() == 200) return r;

            Thread.sleep(apiConfig.sleepMs());
        }

        throw new RuntimeException("Timeout polling PositionHist API, opId=" + operationId);
    }
}
