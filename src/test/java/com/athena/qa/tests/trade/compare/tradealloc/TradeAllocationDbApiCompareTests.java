package com.athena.qa.tests.trade.compare.tradealloc;

import com.athena.qa.framework.client.TradeAllocDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.trade.compare.common.CompareAssert;
import com.athena.qa.tests.trade.compare.common.CompareResult;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeAllocationDbApiCompareTests extends BaseApiTest {

    @Test
    public void compareAllocations() {

        // =====================================================
        // DEBUG WORKDIR
        // =====================================================
        System.out.println("WORKDIR = " + new File(".").getAbsolutePath());

        // =====================================================
        // 1️⃣ API: get all trades
        // =====================================================
        Response res = tradeApiClient.getAllTrades();
        res.then().statusCode(200);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trades =
                (List<Map<String, Object>>) (List<?>)
                        res.jsonPath().getList("");

        // RAW API
        writeJson("out/allocations_api_raw.json", trades);

        // =====================================================
        // 2️⃣ DB: get intraday allocation canonical
        // =====================================================
        TradeAllocDbClient dbClient =
                new TradeAllocDbClient(dbConfig);

        // ⚠️ IMPORTANT: must be false to include unfilled allocations
        List<Map<String, Object>> dbRows =
                dbClient.fetchAllocations(false);

        // 🔥 RAW DB
        writeJson("out/allocations_db_raw.json", dbRows);

        /*System.out.println("\n===== DEBUG DB RAW =====");
        System.out.println("DB ROW COUNT = " + dbRows.size());
        if (!dbRows.isEmpty()) {
            System.out.println("DB FIRST ROW = " + dbRows.get(0));
        }*/

        // =====================================================
        // 3️⃣ API → canonical map (KEY = allocID)
        // =====================================================
        Map<String, CanonicalAllocation> apiMap = new HashMap<>();

        for (Map<String, Object> trade : trades) {

            String tradeClOrdID = s(trade.get("clOrdID"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocations =
                    (List<Map<String, Object>>) trade.get("allocations");
            if (allocations == null) continue;

            for (Map<String, Object> a : allocations) {

                String allocId = s(a.get("allocID"));
                if (allocId.isEmpty()) {
                    System.out.println(" API allocation WITHOUT allocID, skipped");
                    continue;
                }

                CanonicalAllocation c = new CanonicalAllocation();
                c.allocID = allocId;                 // 🔑 KEY
                c.tradeClOrdID = tradeClOrdID;       // chỉ để debug
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

        // 🔥 CANONICAL API
        writeJson("out/allocations_api_canonical.json", apiMap);
        // =====================================================
// 🔍 DEBUG: COUNT allocID FROM API
// =====================================================
        System.out.println("\n========== DEBUG API allocID COUNT ==========");
        System.out.println("API allocID count = " + apiMap.size());

        // System.out.println("\n===== DEBUG API KEYS =====");
      /*  apiMap.keySet().stream().limit(10)
                .forEach(k -> System.out.println("API KEY = " + k));
        System.out.println("API KEY COUNT = " + apiMap.size());*/

        // =====================================================
        // 4️⃣ DB → canonical map (KEY = allocID)
        // =====================================================
        Map<String, CanonicalAllocation> dbMap = new HashMap<>();

        for (Map<String, Object> row : dbRows) {
            CanonicalAllocation c =
                    CanonicalAllocation.fromDbRow(row);

            if (c.allocID.isEmpty()) {
                System.out.println("❌ DB ROW WITH EMPTY allocID: " + row);
            }

            dbMap.put(c.key(), c);
        }

        // 🔥 CANONICAL DB
        writeJson("out/allocations_db_canonical.json", dbMap);
        // =====================================================
// 🔍 DEBUG: COUNT allocID FROM DB
// =====================================================
        System.out.println("\n========== DEBUG DB allocID COUNT ==========");
        System.out.println("DB allocID count = " + dbMap.size());


        //   System.out.println("\n===== DEBUG DB KEYS =====");
        //   dbMap.keySet().stream().limit(10)
        //            .forEach(k -> System.out.println("DB KEY = " + k));
        //    System.out.println("DB KEY COUNT = " + dbMap.size());

        // =====================================================
        // 5️⃣ COMPARE (ALLOCID ONLY)
        // =====================================================
        CompareResult result =
                AllocationCompareEngine.compare(apiMap, dbMap);

        // 🔥 COMPARE RESULT
        writeJson("out/allocations_compare_result.json", result);

        // =====================================================
        // 6️⃣ ASSERT
        // =====================================================
        CompareAssert.assertResult(result);
    }

    // =====================================================
    // helpers
    // =====================================================
    private static BigDecimal bd(Object v) {
        if (v == null) return BigDecimal.ZERO;
        return new BigDecimal(v.toString());
    }

    private static String s(Object v) {
        return v == null ? "" : v.toString();
    }
}
