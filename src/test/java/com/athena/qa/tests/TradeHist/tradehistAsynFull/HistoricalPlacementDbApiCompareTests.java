package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.framework.client.TradePlacementDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.compare.tradeplacement.CanonicalPlacement;
import com.athena.qa.tests.trade.TradeCompareEngine;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
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
        // 4️⃣ API → canonical map
        // =====================================================
        Map<String, CanonicalPlacement> apiMap = new HashMap<>();

        for (Map<String, Object> trade : trades) {

            String tradeClOrdID = s(trade.get("clOrdID"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> placements =
                    (List<Map<String, Object>>) trade.get("placements");

            if (placements == null) continue;

            for (Map<String, Object> p : placements) {

                CanonicalPlacement c = new CanonicalPlacement();
                c.tradeClOrdID = tradeClOrdID;
                c.placementClOrdID = s(p.get("clOrdID"));

                c.brokerId = s(p.get("brokerCode"));
                c.orderID = s(p.get("orderID"));
                c.status = s(p.get("status"));
                c.instruction = s(p.get("instruction"));

                c.quantity = bd(p.get("quantity"));
                c.filledQty = bd(p.get("filledQty"));
                c.filledValue = bd(p.get("filledValue"));
                c.limitPrice = bd(p.get("limitPrice"));

                apiMap.put(c.key(), c);
            }
        }

        writeJson("out/hist_plc_api_canonical.json", apiMap);

        // =====================================================
        // 5️⃣ DB → canonical map
        // =====================================================
        Map<String, CanonicalPlacement> dbMap = new HashMap<>();

        for (Map<String, Object> row : dbRows) {
            CanonicalPlacement c =
                    CanonicalPlacement.fromDbRow(row);
            dbMap.put(c.key(), c);
        }

        writeJson("out/hist_plc_db_canonical.json", dbMap);

        // =====================================================
        // 🔍 DEBUG LIKE ALLOCATION (IN TEST)
        // =====================================================
        System.out.println("\n========== DEBUG HIST PLACEMENT (TEST LEVEL) ==========");
        for (String key : apiMap.keySet()) {
            CanonicalPlacement api = apiMap.get(key);
            CanonicalPlacement db = dbMap.get(key);

            System.out.println("\nKEY = " + key);
            System.out.println("  API = " + api);
            System.out.println("  DB  = " + db);
        }

        // =====================================================
        // 6️⃣ COMPARE (ENGINE)
        // =====================================================
        HistoricalPlacementCompareEngine engine =
                new HistoricalPlacementCompareEngine();

        System.out.println(">>> CALLING HistoricalPlacementCompareEngine.compare()");

        TradeCompareEngine.CompareReport report =
                engine.compare(dbRows, trades);

        System.out.println("<<< FINISHED HistoricalPlacementCompareEngine.compare()");

        // 🔥 GHI FILE TRƯỚC
        writeJson("out/hist_plc_compare_report.json", report);

        // =====================================================
        // 7️⃣ ASSERT CUỐI
        // =====================================================
        assertTrue(report.isAllMatch(), report.toString());
    }

    // =====================================================
    // helper methods
    // =====================================================
    private static BigDecimal bd(Object v) {
        if (v == null) return BigDecimal.ZERO;
        return new BigDecimal(v.toString());
    }

    private static String s(Object v) {
        return v == null ? "" : v.toString();
    }
}
