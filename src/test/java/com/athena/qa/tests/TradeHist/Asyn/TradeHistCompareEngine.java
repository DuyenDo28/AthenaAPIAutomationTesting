package com.athena.qa.tests.TradeHist.Asyn;

import com.athena.qa.tests.base.BaseCompareEngine;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

public class TradeHistCompareEngine extends BaseCompareEngine {

    /**
     * API-key (lowercase) -> DB column
     */
    private static final Map<String, String> API_TO_DB = new LinkedHashMap<>();

    static {
        API_TO_DB.put("id", "ID");
        API_TO_DB.put("clordid", "ClOrdID");
        API_TO_DB.put("userid", "UserID");
        API_TO_DB.put("usercode", "UserCode");
        API_TO_DB.put("transactiontype", "TransactionType");
        API_TO_DB.put("securityid", "SecurityID");
        API_TO_DB.put("symbol", "Ticker");

        API_TO_DB.put("targetqty", "OrderQuantity");
        API_TO_DB.put("filledqty", "FilledQty");
        API_TO_DB.put("avgprice", "FilledValue");

        API_TO_DB.put("tradedate", "TradeDate");
        API_TO_DB.put("settledate", "SettleDate");

        API_TO_DB.put("pricecncyid", "PriceCurrencyID");
        API_TO_DB.put("pricecncy", "PriceCurrencyCode");
        API_TO_DB.put("settlecncyid", "SettleCurrencyID");
        API_TO_DB.put("settlecncy", "SettleCurrencyCode");

        API_TO_DB.put("confirmed", "Confirmed");
        API_TO_DB.put("isarchived", "IsArchived");
        API_TO_DB.put("isexternal", "IsExternal");
        API_TO_DB.put("islocked", "Locked");
        API_TO_DB.put("grouped", "Grouped");

        API_TO_DB.put("orderstate", "OrderState");
    }

    // ======================================================
    // Compare
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

                Object apiVal = normalizeApi(apiKey, getIgnoreCase(api, apiKey));
                Object dbVal = normalizeDb(dbKey, db.get(dbKey));

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
    // Index
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
    // Normalize
    // ======================================================

    private Object normalizeApi(String apiKey, Object v) {
        if (v == null) return null;
        switch (apiKey) {
            case "tradedate":
            case "settledate":
                return normalizeDate(v);
            case "targetqty":
            case "filledqty":
            case "avgprice":
                return new BigDecimal(v.toString());
            default:
                return v;
        }
    }

    private Object normalizeDb(String dbKey, Object v) {
        if (v == null) return null;
        switch (dbKey) {
            case "TradeDate":
            case "SettleDate":
                return normalizeDate(v);
            default:
                return v;
        }
    }

    private Object normalizeDate(Object v) {
        if (v instanceof java.sql.Date) return v.toString();
        if (v instanceof java.util.Date)
            return Instant.ofEpochMilli(((Date) v).getTime())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .toString();
        return v.toString();
    }

    // ======================================================
    // Report (JSON ONLY)
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
