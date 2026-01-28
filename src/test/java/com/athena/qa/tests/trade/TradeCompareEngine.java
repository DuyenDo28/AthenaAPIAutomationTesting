package com.athena.qa.tests.trade;

import com.athena.qa.tests.base.BaseCompareEngine;

import java.util.*;

public class TradeCompareEngine extends BaseCompareEngine {

    /**
     * API-key (lowercase) -> DB column
     * FULL trade-level mapping based on GET /api/Trades response
     */
    private static final Map<String, String> API_TO_DB = new LinkedHashMap<>();

    static {
        // ======================================================
        // Identity / Business key
        // ======================================================
        API_TO_DB.put("id", "ID");

        API_TO_DB.put("clordid", "ClOrdID");

        API_TO_DB.put("originalclordid", "OriginalClOrdID");

        API_TO_DB.put("userid", "UserID");
        API_TO_DB.put("usercode", "UserCode");

        // ======================================================
        // Transaction / Instrument
        // ======================================================
        API_TO_DB.put("transactiontype", "TransactionType");
        API_TO_DB.put("securityid", "SecurityID");
        API_TO_DB.put("symbol", "Ticker");

        // ======================================================
        // Quantity / Fill
        // ======================================================
        API_TO_DB.put("targetqty", "OrderQuantity");
        API_TO_DB.put("filledqty", "FilledQty");

        // NOTE:
        // API.avgPrice ~= DB.FilledValue (NOT OrderQuantity-based avg)
        API_TO_DB.put("avgprice", "FilledValue");

        // ======================================================
        // Dates
        // ======================================================
        API_TO_DB.put("tradedate", "TradeDate");
        API_TO_DB.put("settledate", "SettleDate");

        // ======================================================
        // Currency
        // ======================================================
        API_TO_DB.put("pricecncyid", "PriceCurrencyID");
        API_TO_DB.put("pricecncy", "PriceCurrencyCode");

        API_TO_DB.put("settlecncyid", "SettleCurrencyID");
        API_TO_DB.put("settlecncy", "SettleCurrencyCode");

        // ======================================================
        // State / Flags
        // ======================================================
        API_TO_DB.put("orderstate", "OrderState");

        API_TO_DB.put("confirmed", "Confirmed");
        API_TO_DB.put("isarchived", "IsArchived");
        API_TO_DB.put("isexternal", "IsExternal");
        API_TO_DB.put("islocked", "Locked");
        API_TO_DB.put("grouped", "Grouped");

        API_TO_DB.put("duplicate", "Duplicate");
        API_TO_DB.put("contra", "Contra");
        API_TO_DB.put("iscashorderqty", "IsCashOrderQty");

        // ======================================================
        // Default Tags (API string vs DB ID/nullable)
        // ======================================================
        API_TO_DB.put("defaulttag1", "DefaultTag1ID");
        API_TO_DB.put("defaulttag2", "DefaultTag2ID");
        API_TO_DB.put("defaulttag3", "DefaultTag3ID");
        API_TO_DB.put("defaulttag4", "DefaultTag4ID");
        API_TO_DB.put("defaulttag5", "DefaultTag5ID");

        // ======================================================
        // Allocation method (trade-level)
        // ======================================================
        API_TO_DB.put("allocationmethod", "AllocationMethod");
    }

    // ======================================================
    // Compare (FORMAT GIỐNG TradeHist)
    // ======================================================
    public CompareReport compare(
            List<Map<String, Object>> dbRows,
            List<Map<String, Object>> apiRows) {

        Map<String, Map<String, Object>> dbByKey = indexByKey(dbRows, true);
        Map<String, Map<String, Object>> apiByKey = indexByKey(apiRows, false);

        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(dbByKey.keySet());
        allKeys.addAll(apiByKey.keySet());

        CompareReport report = new CompareReport();

        for (String key : allKeys) {
            if (!apiByKey.containsKey(key)) report.onlyInDB.add(key);
            else if (!dbByKey.containsKey(key)) report.onlyInAPI.add(key);
        }

        for (String key : allKeys) {
            Map<String, Object> db = dbByKey.get(key);
            Map<String, Object> api = apiByKey.get(key);
            if (db == null || api == null) continue;

            Map<String, Object> diffs = new LinkedHashMap<>();

            for (var e : API_TO_DB.entrySet()) {
                String apiKey = e.getKey();
                String dbKey = e.getValue();

                if (!db.containsKey(dbKey)) continue;

                Object apiVal = getIgnoreCase(api, apiKey);
                Object dbVal = db.get(dbKey);

                // ===== DEBUG: PRINT ALL API vs DB VALUES =====
                System.out.println(
                        "[DEBUG TRADE COMPARE] key=" + key +
                                " | API." + apiKey + "=" + apiVal +
                                " | DB." + dbKey + "=" + dbVal
                );


                if (!equalSmart(apiVal, dbVal)) {
                    Map<String, Object> diff = new LinkedHashMap<>();
                    diff.put("apiKey", apiKey);
                    diff.put("dbKey", dbKey);
                    diff.put("api", apiVal);
                    diff.put("db", dbVal);
                    diffs.put(apiKey, diff);
                }
            }

            if (!diffs.isEmpty()) report.mismatches.put(key, diffs);
            else report.matchedKeys.add(key);
        }

        report.matchedCount = report.matchedKeys.size();
        report.onlyApiCount = report.onlyInAPI.size();
        report.onlyDbCount = report.onlyInDB.size();
        report.mismatchCount = report.mismatches.size();

        return report;
    }

    // ======================================================
    // Index (giống TradeHist)
    // ======================================================
    private Map<String, Map<String, Object>> indexByKey(
            List<Map<String, Object>> rows, boolean isDb) {

        Map<String, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String key = isDb ? dbKeyOf(row) : apiKeyOf(row);
            if (key != null && !key.isBlank()) {
                out.putIfAbsent(key, row);
            }
        }
        return out;
    }

    private String apiKeyOf(Map<String, Object> api) {
        Object cl = getIgnoreCase(api, "clordid");
        if (cl != null) return cl.toString();
        Object id = getIgnoreCase(api, "id");
        return id != null ? id.toString() : null;
    }

    private String dbKeyOf(Map<String, Object> db) {
        Object cl = db.get("ClOrdID");
        if (cl != null) return cl.toString();
        Object id = db.get("ID");
        return id != null ? id.toString() : null;
    }

    private Object getIgnoreCase(Map<String, Object> row, String keyLower) {
        for (String k : row.keySet()) {
            if (k != null && k.toLowerCase(Locale.ROOT).equals(keyLower)) {
                return row.get(k);
            }
        }
        return null;
    }

    // ======================================================
    // Report (GIỐNG TradeHist)
    // ======================================================
    public static class CompareReport {
        public int matchedCount;
        public int onlyApiCount;
        public int onlyDbCount;
        public int mismatchCount;

        public final List<String> matchedKeys = new ArrayList<>();
        public final List<String> onlyInAPI = new ArrayList<>();
        public final List<String> onlyInDB = new ArrayList<>();
        public final Map<String, Map<String, Object>> mismatches =
                new LinkedHashMap<>();

        public boolean isAllMatch() {
            return mismatchCount == 0
                    && onlyApiCount == 0
                    && onlyDbCount == 0;
        }
    }
}
