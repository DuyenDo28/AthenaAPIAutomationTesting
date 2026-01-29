package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.framework.client.TradePlacementDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.trade.TradeCompareEngine;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoricalPlacementDbApiCompareTests extends BaseApiTest {

    @Test
    public void compareHistoricalPlacements() throws Exception {

        // =====================================================
        // DEBUG WORKDIR
        // =====================================================
        System.out.println("WORKDIR = " + new File(".").getAbsolutePath());

        // =====================================================
        // 1️⃣ START ASYNC (AsyncFull)
        // =====================================================
        Response startRes =
                tradeApiClient.startHistoricalAsync("2024-11-01", "2024-11-02");

        startRes.then().statusCode(202);

        String operationId = startRes.jsonPath().getString("operationId");

        System.out.println("⏳ Waiting 10 seconds for historical async to complete...");
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

        if (!"Successful".equalsIgnoreCase(status)) {
            writeJson("out/hist_plc_async_not_ready.json", root);
            throw new RuntimeException(
                    "Historical async not ready, status=" + status
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result =
                (Map<String, Object>) root.get("result");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trades =
                (List<Map<String, Object>>) result.get("elements");

        // 🔥 RAW API
        writeJson("out/hist_plc_api_raw.json", trades);

        // =====================================================
        // 3️⃣ DB: get historical placements
        // =====================================================
        TradePlacementDbClient dbClient =
                new TradePlacementDbClient(dbConfig);

        List<Map<String, Object>> dbRows =
                dbClient.fetchHistoricalPlacements("2024-11-01", "2024-11-02");

        // 🔥 RAW DB
        writeJson("out/hist_plc_db_raw.json", dbRows);

        // =====================================================
        // 🔍 DEBUG: API vs DB (PLACEMENT – SAME STYLE AS TRADE)
        // =====================================================
        System.out.println(
                "\n================ DEBUG HIST PLACEMENT API vs DB =================");

        for (Map<String, Object> trade : trades) {

            Object tradeClOrdId = trade.get("clOrdID");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> placements =
                    (List<Map<String, Object>>) trade.get("placements");

            if (placements == null) continue;

            for (Map<String, Object> apiPlc : placements) {

                Object plcClOrdId = apiPlc.get("clOrdID");
                if (plcClOrdId == null) continue;

                Map<String, Object> dbRow = null;
                for (Map<String, Object> r : dbRows) {
                    if (tradeClOrdId.equals(r.get("tradeClOrdID"))
                            && plcClOrdId.equals(r.get("placementClOrdID"))) {
                        dbRow = r;
                        break;
                    }
                }

                if (dbRow == null) {
                    System.out.println("❌ DB NOT FOUND for "
                            + tradeClOrdId + "|" + plcClOrdId);
                    continue;
                }

                System.out.println("\n--- PLACEMENT "
                        + tradeClOrdId + "|" + plcClOrdId + " ---");

                for (Map.Entry<String, String> e
                        : HistoricalPlacementCompareEngine.API_TO_DB.entrySet()) {

                    String apiKey = e.getKey();
                    String dbKey = e.getValue();

                    Object apiVal = null;
                    for (String k : apiPlc.keySet()) {
                        if (k != null && k.equalsIgnoreCase(apiKey)) {
                            apiVal = apiPlc.get(k);
                            break;
                        }
                    }

                    Object dbVal = dbRow.get(dbKey);

                    if (apiVal == null && dbVal == null) continue;

                    System.out.println(
                            "API." + apiKey + " = " + apiVal
                                    + " | DB." + dbKey + " = " + dbVal
                                    + (apiVal != null && dbVal != null
                                    ? " | TYPE API="
                                    + apiVal.getClass().getSimpleName()
                                    + " DB="
                                    + dbVal.getClass().getSimpleName()
                                    : "")
                    );
                }
            }
        }

        System.out.println("\n================ END DEBUG =================\n");

        // =====================================================
        // 6️⃣ COMPARE (ENGINE)
        // =====================================================
        HistoricalPlacementCompareEngine engine =
                new HistoricalPlacementCompareEngine();

        TradeCompareEngine.CompareReport report =
                engine.compare(dbRows, trades);

        // 🔥 GHI FILE
        writeJson("out/hist_plc_compare_report.json", report);

        // =====================================================
        // 7️⃣ ASSERT CUỐI
        // =====================================================
        assertTrue(report.isAllMatch(), report.toString());
    }
}
