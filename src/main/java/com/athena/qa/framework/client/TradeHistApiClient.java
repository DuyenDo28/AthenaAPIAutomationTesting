package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import io.restassured.response.Response;

import java.time.LocalDate;

public class TradeHistApiClient extends BaseApiClient {

    private final TokenProvider tokenProvider;

    public TradeHistApiClient(ApiConfig apiConfig, TokenProvider tokenProvider) {
        super(apiConfig);
        this.tokenProvider = tokenProvider;
    }

    /** Step 1: start async */
    public String startAsync(LocalDate start, LocalDate end) {

        Response res = spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .queryParam("startDate", start.toString())
                .queryParam("endDate", end.toString())
                .post("/api/HistoricalTrades/Async");

        int status = res.statusCode();
        if (status != 200 && status != 202) {
            throw new RuntimeException(
                    "Start TradeHist async failed. status=" + status
                            + " body=" + res.asString());
        }

        //  try JSON field: operationId
        try {
            String opId = res.jsonPath().getString("operationId");
            if (opId != null && !opId.isBlank()) return opId;
        } catch (Exception ignored) {}

        // try JSON field: id
        try {
            String opId = res.jsonPath().getString("id");
            if (opId != null && !opId.isBlank()) return opId;
        } catch (Exception ignored) {}

        //  fallback: plain text
        String raw = res.asString();
        if (raw != null && !raw.isBlank()) {
            return raw.trim();
        }

        throw new RuntimeException(
                "Cannot extract operationId from TradeHist start response. body="
                        + res.asString());
    }


    /** Step 2: poll async */
    public Response poll(String operationId) throws InterruptedException {
        for (int i = 0; i < apiConfig.maxAttempts(); i++) {
            Response r = spec()
                    .header("Authorization", "Bearer " + tokenProvider.get())
                    .queryParam("OperationId", operationId) //
                    .get("/api/HistoricalTrades/AsyncResults");

            System.out.println(
                    "Polling attempt " + i +
                            " status=" + r.statusCode()
            );

            if (r.statusCode() == 200) return r;

            Thread.sleep(apiConfig.sleepMs());
        }
        throw new RuntimeException("Timeout polling TradeHist API, opId=" + operationId);
    }


}
