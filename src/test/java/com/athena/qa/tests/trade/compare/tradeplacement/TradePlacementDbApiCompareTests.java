package com.athena.qa.tests.trade.compare.tradeplacement;

import com.athena.qa.framework.client.TradePlacementDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.trade.compare.common.CompareResult;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TradePlacementDbApiCompareTests extends BaseApiTest {

    @Test
    public void comparePlacements() {

        System.out.println("WORKDIR = " + new File(".").getAbsolutePath());

        // ===== 1) API =====
        Response res = tradeApiClient.getAllTrades();
        res.then().statusCode(200);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trades =
                (List<Map<String, Object>>) (List<?>)
                        res.jsonPath().getList("");

        writeJson("out/placements_api_raw.json", trades);

        // ===== 2) DB =====
        TradePlacementDbClient dbClient =
                new TradePlacementDbClient(dbConfig);

        List<Map<String, Object>> dbRows =
                dbClient.fetchPlacements(false);

        writeJson("out/placements_db_raw.json", dbRows);

        // ===== 3) API → Canonical (KEY = placements[].clOrdID) =====
        Map<String, CanonicalPlacement> apiMap = new HashMap<>();

        for (Map<String, Object> trade : trades) {

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> placements =
                    (List<Map<String, Object>>) trade.get("placements");
            if (placements == null) continue;

            for (Map<String, Object> p : placements) {
                CanonicalPlacement c = new CanonicalPlacement();

                c.tradeClOrdID = s(trade.get("clOrdID")); // debug only
                c.timeInForce = s(p.get("timeInForce"));

                c.placementClOrdID = s(p.get("clOrdID")); // 🔑 KEY

                c.brokerId = s(p.get("brokerCode"));
                c.orderID = s(p.get("orderID"));
                c.status = s(p.get("status"));
                c.instruction = s(p.get("instruction"));

                c.quantity = bd(p.get("quantity"));
                c.filledQty = bd(p.get("filledQty"));
                c.filledValue = bd(p.get("filledValue"));
                c.limitPrice = bd(p.get("limitPrice"));

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> comms =
                        (List<Map<String, Object>>) p.get("commissions");
                if (comms != null) {
                    for (Map<String, Object> cm : comms) {
                        String code = s(cm.get("code"));
                        BigDecimal val = bd(cm.get("value"));
                        switch (code) {
                            case "Comm1" -> c.comm1 = val;
                            case "Comm2" -> c.comm2 = val;
                            case "Comm3" -> c.comm3 = val;
                            case "Comm4" -> c.comm4 = val;
                            case "Comm5" -> c.comm5 = val;
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> fees =
                        (List<Map<String, Object>>) p.get("fees");
                if (fees != null) {
                    for (Map<String, Object> f : fees) {
                        String code = s(f.get("code"));
                        BigDecimal val = bd(f.get("value"));
                        switch (code) {
                            case "Fee1" -> c.fee1 = val;
                            case "Fee2" -> c.fee2 = val;
                            case "Fee3" -> c.fee3 = val;
                            case "Fee4" -> c.fee4 = val;
                            case "Fee5" -> c.fee5 = val;
                        }
                    }
                }

                apiMap.put(c.key(), c);
            }
        }

        writeJson("out/placements_api_canonical.json", apiMap);

        // ===== 4) DB → Canonical =====
        Map<String, CanonicalPlacement> dbMap = new HashMap<>();
        for (Map<String, Object> row : dbRows) {
            CanonicalPlacement c = CanonicalPlacement.fromDbRow(row);
            dbMap.put(c.key(), c);
        }

        writeJson("out/placements_db_canonical.json", dbMap);

        // ===== 5) COMPARE =====
        CompareResult result =
                PlacementCompareEngine.compare(apiMap, dbMap);

        writeJson("out/placements_compare_result.json", result);

        assertTrue(result.allMatch(), result.toString());
    }

    // ===== helpers =====
    private static BigDecimal bd(Object v) {
        if (v == null) return BigDecimal.ZERO;
        return new BigDecimal(v.toString());
    }

    private static String s(Object v) {
        return v == null ? "" : v.toString();
    }
}
