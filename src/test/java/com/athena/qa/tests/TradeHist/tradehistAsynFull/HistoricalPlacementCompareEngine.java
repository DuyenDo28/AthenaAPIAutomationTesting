package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.tests.base.BaseCompareEngine;
import com.athena.qa.tests.trade.TradeCompareEngine.CompareReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class HistoricalPlacementCompareEngine extends BaseCompareEngine {

    /**
     * API placement field (lowercase) -> DB column
     */
    public static final Map<String, String> API_TO_DB = new LinkedHashMap<>();

    static {
        API_TO_DB.put("orderid", "OrderID");
        API_TO_DB.put("status", "Status");

        API_TO_DB.put("filledqty", "FilledQty");
        API_TO_DB.put("filledvalue", "FilledValue");

        API_TO_DB.put("instruction", "Instruction");
        API_TO_DB.put("limitprice", "LimitPrice");
        API_TO_DB.put("stopprice", "StopPrice");

        API_TO_DB.put("timeinforce", "TimeInForce");
    }

    /**
     * Compare historical placement values
     * Key = tradeClOrdID | placementClOrdID
     */
    public CompareReport compare(
            List<Map<String, Object>> dbPlacementRows,
            List<Map<String, Object>> apiTrades) {

        // ============================
        // 1️⃣ Flatten API placements
        // ============================
        List<Map<String, Object>> apiPlacements = new ArrayList<>();

        for (Map<String, Object> t : apiTrades) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> plcs =
                    (List<Map<String, Object>>) t.get("placements");
            if (plcs != null) {
                for (Map<String, Object> p : plcs) {
                    p.put("tradeClOrdID", t.get("clOrdID"));
                    apiPlacements.add(p);
                }
            }
        }

        // ============================
        // 2️⃣ Index by COMPOSITE KEY
        // ============================
        Map<String, Map<String, Object>> dbByKey = indexByDbKey(dbPlacementRows);
        Map<String, Map<String, Object>> apiByKey = indexByApiKey(apiPlacements);

        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(dbByKey.keySet());
        allKeys.addAll(apiByKey.keySet());

        CompareReport report = new CompareReport();

        // ============================
        // 3️⃣ Presence check
        // ============================
        for (String key : allKeys) {
            if (!apiByKey.containsKey(key)) report.onlyInDB.add(key);
            else if (!dbByKey.containsKey(key)) report.onlyInAPI.add(key);
        }

        // ============================
        // 4️⃣ Value compare
        // ============================
        for (String key : allKeys) {

            Map<String, Object> db = dbByKey.get(key);
            Map<String, Object> api = apiByKey.get(key);

            if (db == null || api == null) continue;

            Map<String, Object> diffs = new LinkedHashMap<>();

            for (var e : API_TO_DB.entrySet()) {
                String apiField = e.getKey();
                String dbField = e.getValue();

                Object apiVal = getIgnoreCase(api, apiField);
                Object dbVal = db.get(dbField);

                // ==================================================
                // ✅ SPECIAL CASE 1: filledqty → ABS compare
                // ==================================================
                if ("filledqty".equals(apiField)
                        && apiVal instanceof Number
                        && dbVal instanceof Number) {

                    BigDecimal apiBd =
                            new BigDecimal(apiVal.toString()).abs();
                    BigDecimal dbBd =
                            new BigDecimal(dbVal.toString()).abs();

                    if (apiBd.compareTo(dbBd) != 0) {
                        diffs.put(apiField, diff(apiVal, dbVal));
                    }
                    continue;
                }

                // ==================================================
                // ✅ SPECIAL CASE 2: filledvalue → round to 4 decimals
                // ==================================================
                if ("filledvalue".equals(apiField)
                        && apiVal instanceof Number
                        && dbVal instanceof Number) {

                    BigDecimal apiBd =
                            new BigDecimal(apiVal.toString())
                                    .setScale(4, RoundingMode.HALF_UP);

                    BigDecimal dbBd =
                            new BigDecimal(dbVal.toString())
                                    .setScale(4, RoundingMode.HALF_UP);

                    if (apiBd.compareTo(dbBd) != 0) {
                        diffs.put(apiField, diff(apiVal, dbVal));
                    }
                    continue;
                }

                // ===== DEFAULT compare =====
                if (!equalSmart(apiVal, dbVal)) {
                    diffs.put(apiField, diff(apiVal, dbVal));
                }
            }

            if (!diffs.isEmpty()) {
                report.mismatches.put(key, diffs);
            } else {
                report.matchedKeys.add(key);
            }
        }

        report.matchedCount = report.matchedKeys.size();
        report.onlyApiCount = report.onlyInAPI.size();
        report.onlyDbCount = report.onlyInDB.size();
        report.mismatchCount = report.mismatches.size();

        return report;
    }

    // ============================
    // Helpers
    // ============================

    private Map<String, Object> diff(Object api, Object db) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("api", api);
        m.put("db", db);
        return m;
    }

    private Map<String, Map<String, Object>> indexByApiKey(
            List<Map<String, Object>> rows) {

        Map<String, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Object trade = getIgnoreCase(r, "tradeclordid");
            Object plc = getIgnoreCase(r, "clordid");

            if (trade != null && plc != null) {
                out.put(trade + "|" + plc, r);
            }
        }
        return out;
    }

    private Map<String, Map<String, Object>> indexByDbKey(
            List<Map<String, Object>> rows) {

        Map<String, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Object trade = r.get("tradeClOrdID");
            Object plc = r.get("placementClOrdID");

            if (trade != null && plc != null) {
                out.put(trade + "|" + plc, r);
            }
        }
        return out;
    }

    private Object getIgnoreCase(Map<String, Object> row, String keyLower) {
        for (String k : row.keySet()) {
            if (k != null && k.toLowerCase().equals(keyLower)) {
                return row.get(k);
            }
        }
        return null;
    }
}
