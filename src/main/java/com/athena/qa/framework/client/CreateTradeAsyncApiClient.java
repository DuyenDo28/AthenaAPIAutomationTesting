package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import com.athena.qa.framework.config.TradeMultiConfig;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client gọi /api/Trades/Async
 * - Làm Y CHANG Trades/Multi
 * - Nhưng dùng async endpoint
 * - Dùng cho bulk lớn (3000+)
 */
public class CreateTradeAsyncApiClient extends BaseApiClient {

    private final TokenProvider tokenProvider;

    public CreateTradeAsyncApiClient(ApiConfig apiConfig, TokenProvider tokenProvider) {
        super(apiConfig);
        this.tokenProvider = tokenProvider;
    }

    /**
     * Submit async trades (KHÔNG block)
     */
    public void createTradesAsync(TradeMultiConfig cfg) {

        List<Map<String, Object>> payload = new ArrayList<>();

        for (int i = 1; i <= cfg.count(); i++) {
            payload.add(TradePayloadFactory.buildTrade(cfg, i));
        }

        postAsync(payload);
    }

    /**
     * Submit async trades + trả payload (để dump JSON)
     */
    public List<Map<String, Object>> createTradesAsyncAndReturnPayload(TradeMultiConfig cfg) {

        List<Map<String, Object>> payload = new ArrayList<>();

        for (int i = 1; i <= cfg.count(); i++) {
            payload.add(TradePayloadFactory.buildTrade(cfg, i));
        }

        postAsync(payload);
        return payload;
    }

    /**
     * Gửi request async
     */
    private void postAsync(List<Map<String, Object>> payload) {

        Response res = spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .body(payload)
                .post("/api/Trades/Async");

        int status = res.statusCode();

        // Async thường trả 200 hoặc 202
        if (status != 200 && status != 202) {
            throw new RuntimeException(
                    "Trades/Async failed " +
                            "status=" + status +
                            " body=" + res.asString()
            );
        }

        System.out.println("✅ Trades/Async submitted successfully. status=" + status);
    }
}
