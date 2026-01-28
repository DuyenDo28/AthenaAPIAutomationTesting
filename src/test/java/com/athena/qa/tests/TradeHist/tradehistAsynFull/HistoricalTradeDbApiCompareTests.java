package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.framework.client.TradeDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.trade.TradeCompareEngine;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoricalTradeDbApiCompareTests extends BaseApiTest {

    @Test
    public void compareHistoricalTrades() throws Exception {

        // ===== DEBUG WORKDIR
        System.out.println("WORKDIR = " + new File(".").getAbsolutePath());

        // =====================================================
        // 1️⃣ START ASYNC (AsyncFull)
        // =====================================================
        Response startRes =
                tradeApiClient.startHistoricalAsync("2024-11-01", "2024-11-02");

        startRes.then().statusCode(202);

        String operationId = startRes.jsonPath().getString("operationId");
        Thread.sleep(10_000);

        // =====================================================
        // 2️⃣ GET ASYNC RESULT
        // =====================================================
        Response res =
                tradeApiClient.getHistoricalAsyncResult(operationId);

        res.then().statusCode(200);

        @SuppressWarnings("unchecked")
        Map<String, Object> root =
                (Map<String, Object>) res.jsonPath().get("");

        String status = String.valueOf(root.get("status"));

        // ===== ASYNC NOT READY → FAIL WITH DEBUG FILE
        if (!"Successful".equalsIgnoreCase(status)) {
            writeJson("out/hist_trade_async_not_ready.json", root);
            throw new RuntimeException(
                    "Historical async not ready, status=" + status
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result =
                (Map<String, Object>) root.get("result");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiTrades =
                (List<Map<String, Object>>) result.get("elements");

        // 🔥 ghi raw API
        writeJson("out/hist_trade_api_raw.json", apiTrades);

        // =====================================================
        // 3️⃣ DB: get historical trades
        // =====================================================
        TradeDbClient dbClient =
                new TradeDbClient(dbConfig);

        List<Map<String, Object>> dbRows =
                dbClient.fetchHistoricalOrders("2024-11-01", "2024-11-02");

        // 🔥 ghi raw DB
        writeJson("out/hist_trade_db_raw.json", dbRows);

        // =====================================================
        // 🔍 DEBUG: PRINT RAW API vs DB (before compare)
        // =====================================================
        System.out.println("\n================ DEBUG API vs DB =================");

        for (Map<String, Object> apiRow : apiTrades) {

            Object apiClOrdId = apiRow.get("clOrdID");
            if (apiClOrdId == null) continue;

            Map<String, Object> dbRow = dbRows.stream()
                    .filter(r -> apiClOrdId.equals(r.get("ClOrdID")))
                    .findFirst()
                    .orElse(null);

            if (dbRow == null) {
                System.out.println("❌ DB NOT FOUND for ClOrdID=" + apiClOrdId);
                continue;
            }

            System.out.println("\n--- TRADE ClOrdID = " + apiClOrdId + " ---");

            System.out.println("API.targetQty = " + apiRow.get("targetQty")
                    + " | DB.OrderQuantity = " + dbRow.get("OrderQuantity"));

            System.out.println("API.filledQty = " + apiRow.get("filledQty")
                    + " | DB.FilledQty = " + dbRow.get("FilledQty"));

            System.out.println("API.avgPrice  = " + apiRow.get("avgPrice")
                    + " | DB.FilledValue = " + dbRow.get("FilledValue"));

            Object apiAvg = apiRow.get("avgPrice");
            Object dbAvg = dbRow.get("FilledValue");

            if (apiAvg != null && dbAvg != null) {
                System.out.println("TYPE avgPrice: API="
                        + apiAvg.getClass().getSimpleName()
                        + " | DB="
                        + dbAvg.getClass().getSimpleName());
            }
        }

        System.out.println("\n================ END DEBUG =================\n");

        // =====================================================
        // 4️⃣ COMPARE (TRADE ONLY)
        // =====================================================
        HistoricalTradeCompareEngine engine =
                new HistoricalTradeCompareEngine();

        TradeCompareEngine.CompareReport report =
                engine.compare(dbRows, apiTrades);

        // 🔥 ghi compare result
        writeJson("out/hist_trade_compare_report.json", report);

        // ASSERT
        assertTrue(report.isAllMatch(), report.toString());
    }
}
