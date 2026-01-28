package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.framework.client.TradeAllocDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.compare.tradealloc.CanonicalAllocation;
import com.athena.qa.tests.trade.TradeCompareEngine;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoricalAllocationDbApiCompareTests extends BaseApiTest {

    @Test
    public void compareHistoricalAllocations() throws Exception {

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
            writeJson("out/hist_alloc_async_not_ready.json", root);
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
        writeJson("out/hist_alloc_api_raw.json", trades);

        // =====================================================
        // 3️⃣ DB: get historical allocations
        // =====================================================
        TradeAllocDbClient dbClient =
                new TradeAllocDbClient(dbConfig);

        List<Map<String, Object>> dbRows =
                dbClient.fetchHistoricalAllocations("2024-11-01", "2024-11-02");

        // 🔥 RAW DB
        writeJson("out/hist_alloc_db_raw.json", dbRows);

        // =====================================================
        // 4️⃣ API → canonical map
        // =====================================================
        Map<String, CanonicalAllocation> apiMap = new HashMap<>();

        for (Map<String, Object> trade : trades) {

            String tradeClOrdID = s(trade.get("clOrdID"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocations =
                    (List<Map<String, Object>>) trade.get("allocations");

            if (allocations == null) continue;

            for (Map<String, Object> a : allocations) {

                CanonicalAllocation c = new CanonicalAllocation();
                c.tradeClOrdID = tradeClOrdID;
                c.allocID = s(a.get("allocID"));
                c.accountId = s(a.get("accountCode"));

                c.quantity = bd(a.get("quantity"));
                c.filledQty = bd(a.get("filledQty"));
                c.netMoney = bd(a.get("netMoney"));

                @SuppressWarnings("unchecked")
                Map<String, Object> comm =
                        (Map<String, Object>) a.get("commissions");

                @SuppressWarnings("unchecked")
                Map<String, Object> fee =
                        (Map<String, Object>) a.get("fees");

                if (comm != null) {
                    c.comm1 = bd(comm.get("comm1"));
                    c.comm2 = bd(comm.get("comm2"));
                    c.comm3 = bd(comm.get("comm3"));
                    c.comm4 = bd(comm.get("comm4"));
                    c.comm5 = bd(comm.get("comm5"));
                }

                if (fee != null) {
                    c.fee1 = bd(fee.get("fee1"));
                    c.fee2 = bd(fee.get("fee2"));
                    c.fee3 = bd(fee.get("fee3"));
                    c.fee4 = bd(fee.get("fee4"));
                    c.fee5 = bd(fee.get("fee5"));
                }

                apiMap.put(c.key(), c);
            }
        }

        writeJson("out/hist_alloc_api_canonical.json", apiMap);

        // =====================================================
        // 5️⃣ DB → canonical map
        // =====================================================
        Map<String, CanonicalAllocation> dbMap = new HashMap<>();

        for (Map<String, Object> row : dbRows) {
            CanonicalAllocation c =
                    CanonicalAllocation.fromDbRow(row);
            dbMap.put(c.key(), c);
        }

        writeJson("out/hist_alloc_db_canonical.json", dbMap);

        // =====================================================
        // 6️⃣ COMPARE (HISTORICAL ENGINE)
        // =====================================================
        HistoricalAllocationCompareEngine engine =
                new HistoricalAllocationCompareEngine();

        System.out.println(">>> CALLING HistoricalAllocationCompareEngine.compare()");

        TradeCompareEngine.CompareReport report =
                engine.compare(dbRows, trades);

        System.out.println("<<< FINISHED HistoricalAllocationCompareEngine.compare()");

        // 🔥 GHI FILE TRƯỚC (LUÔN CHẠY)
        writeJson("out/hist_alloc_compare_report.json", report);

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
