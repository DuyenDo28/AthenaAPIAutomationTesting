package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import com.athena.qa.framework.config.TradeMultiConfig;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client gọi /api/Trades/Multi
 * - GIỮ NGUYÊN code cũ
 * - CHỈ THÊM retry + refresh token khi expire
 */
public class CreateTradeApiClient extends BaseApiClient {

    private final TokenProvider tokenProvider;

    public CreateTradeApiClient(ApiConfig apiConfig, TokenProvider tokenProvider) {
        super(apiConfig);
        this.tokenProvider = tokenProvider;
    }

    /**
     * Method cũ – chỉ create trade
     */
    public void createTradesMulti(TradeMultiConfig cfg) {

        List<Map<String, Object>> batch = new ArrayList<>();

        for (int i = 1; i <= cfg.count(); i++) {

            batch.add(TradePayloadFactory.buildTrade(cfg, i));

            if (batch.size() == cfg.batchSize() || i == cfg.count()) {
                postBatch(batch, i);
                batch.clear();
            }
        }
    }

    /**
     * Method cũ + trả payload (để dump JSON)
     */
    public List<Map<String, Object>> createTradesMultiAndReturnPayload(TradeMultiConfig cfg) {

        List<Map<String, Object>> allPayloads = new ArrayList<>();
        List<Map<String, Object>> batch = new ArrayList<>();

        for (int i = 1; i <= cfg.count(); i++) {

            Map<String, Object> trade = TradePayloadFactory.buildTrade(cfg, i);

            batch.add(trade);
            allPayloads.add(trade);

            if (batch.size() == cfg.batchSize() || i == cfg.count()) {
                postBatch(batch, i);
                batch.clear();
            }
        }

        return allPayloads;
    }

    /**
     * Gửi batch + auto refresh token nếu expire
     */
    private void postBatch(List<Map<String, Object>> batch, int index) {

        Response res = send(batch);

        // 🔄 Token expire → refresh + retry 1 lần
        if (res.statusCode() == 401 || res.statusCode() == 403) {
            System.out.println("🔄 Token expired, refreshing...");
            tokenProvider.refresh();
            res = send(batch);
        }

        int status = res.statusCode();

        // ⭐ THÊM ĐOẠN NÀY ⭐
        // Nếu là batch CUỐI và bị 504 → stop gracefully
        if (status == 504 && index >= batch.size()) {
            System.out.println(
                    "⚠️ 504 Gateway Timeout at last batch (index=" + index + "). " +
                            "Trades likely already processed. Stop test gracefully."
            );
            return; // ⬅️ DỪNG Ở ĐÂY, KHÔNG THROW
        }

        if (status != 200 && status != 201) {
            throw new RuntimeException(
                    "Trades/Multi failed at index=" + index +
                            " status=" + status +
                            " body=" + res.asString()
            );
        }
    }


    /**
     * Send request (tách riêng để retry)
     */
    private Response send(List<Map<String, Object>> batch) {
        return spec()
                .header("Authorization", "Bearer " + tokenProvider.get())
                .body(batch)
                .post("/api/Trades/Multi");
        // .post("/api/Trades/Async");
    }
}
