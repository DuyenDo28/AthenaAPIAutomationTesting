package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.tests.base.BaseCompareEngine;
import com.athena.qa.tests.trade.TradeCompareEngine.CompareReport;

import java.math.BigDecimal;
import java.util.*;

public class HistoricalAllocationCompareEngine extends BaseCompareEngine {

    private static final Map<String, String> API_TO_DB = new LinkedHashMap<>();

    static {
        API_TO_DB.put("allocid", "AllocID");

        API_TO_DB.put("quantity", "Quantity");
        API_TO_DB.put("filledqty", "Quantity");
        API_TO_DB.put("netmoney", "NetMoney");

        API_TO_DB.put("comm1", "Comm1");
        API_TO_DB.put("comm2", "Comm2");
        API_TO_DB.put("comm3", "Comm3");
        API_TO_DB.put("comm4", "Comm4");
        API_TO_DB.put("comm5", "Comm5");

        API_TO_DB.put("fee1", "Fee1");
        API_TO_DB.put("fee2", "Fee2");
        API_TO_DB.put("fee3", "Fee3");
        API_TO_DB.put("fee4", "Fee4");
        API_TO_DB.put("fee5", "Fee5");
    }

    public CompareReport compare(
            List<Map<String, Object>> dbAllocationRows,
            List<Map<String, Object>> apiTrades) {

        // 1️⃣ Flatten API allocations
        List<Map<String, Object>> apiAllocations = new ArrayList<>();
        for (Map<String, Object> t : apiTrades) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocs =
                    (List<Map<String, Object>>) t.get("allocations");
            if (allocs != null) apiAllocations.addAll(allocs);
        }

        // 2️⃣ Index by allocID
        Map<String, Map<String, Object>> dbByKey = indexByDbKey(dbAllocationRows);
        Map<String, Map<String, Object>> apiByKey = indexByApiKey(apiAllocations);

        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(dbByKey.keySet());
        allKeys.addAll(apiByKey.keySet());

        CompareReport report = new CompareReport();

        // 3️⃣ Presence check
        for (String key : allKeys) {
            if (!apiByKey.containsKey(key)) report.onlyInDB.add(key);
            else if (!dbByKey.containsKey(key)) report.onlyInAPI.add(key);
        }

        // 4️⃣ Value compare
        for (String key : allKeys) {

            Map<String, Object> db = dbByKey.get(key);
            Map<String, Object> api = apiByKey.get(key);
            if (db == null || api == null) continue;

            Map<String, Object> diffs = new LinkedHashMap<>();

            for (var e : API_TO_DB.entrySet()) {
                String apiField = e.getKey();
                String dbField = e.getValue();

                Object apiVal;

                if (apiField.startsWith("comm")) {
                    Map<String, Object> comm =
                            (Map<String, Object>) api.get("commissions");
                    apiVal = comm != null ? comm.get(apiField) : null;

                } else if (apiField.startsWith("fee")) {
                    Map<String, Object> fee =
                            (Map<String, Object>) api.get("fees");
                    apiVal = fee != null ? fee.get(apiField) : null;

                } else {
                    apiVal = getIgnoreCase(api, apiField);
                }

                Object dbVal = db.get(dbField);
                System.out.println(
                        "\n[DEBUG ALLOC COMPARE]"
                                + "\n  AllocID   = " + key
                                + "\n  Field     = " + apiField
                                + "\n  API value = " + apiVal
                                + " (" + (apiVal == null ? "null" : apiVal.getClass().getSimpleName()) + ")"
                                + "\n  DB value  = " + dbVal
                                + " (" + (dbVal == null ? "null" : dbVal.getClass().getSimpleName()) + ")"
                );


                // ✅ quantity & filledqty → ABS compare
                if (("quantity".equals(apiField) || "filledqty".equals(apiField))
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

                // ✅ netmoney → tolerance ±0.01
                if ("netmoney".equals(apiField)
                        && apiVal instanceof Number
                        && dbVal instanceof Number) {

                    BigDecimal apiBd = new BigDecimal(apiVal.toString());
                    BigDecimal dbBd = new BigDecimal(dbVal.toString());

                    if (apiBd.subtract(dbBd).abs()
                            .compareTo(new BigDecimal("0.01")) > 0) {
                        diffs.put(apiField, diff(apiVal, dbVal));
                    }
                    continue;
                }

                // default
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

    // ================= helpers =================

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
            Object v = getIgnoreCase(r, "allocid");
            if (v != null) out.put(v.toString(), r);
        }
        return out;
    }

    private Map<String, Map<String, Object>> indexByDbKey(
            List<Map<String, Object>> rows) {

        Map<String, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Object v = r.get("AllocID");
            if (v != null) out.put(v.toString(), r);
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
