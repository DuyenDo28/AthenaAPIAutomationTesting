package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.tests.base.BaseCompareEngine;
import com.athena.qa.tests.trade.TradeCompareEngine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class HistoricalTradeCompareEngine extends BaseCompareEngine {

    private static final Map<String, String> API_TO_DB = new LinkedHashMap<>();

    static {
        API_TO_DB.put("id", "ID");
        API_TO_DB.put("clordid", "ClOrdID");
        API_TO_DB.put("originalclordid", "OriginalClOrdID");

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

        API_TO_DB.put("orderstate", "OrderState");
        API_TO_DB.put("confirmed", "Confirmed");
        API_TO_DB.put("isarchived", "IsArchived");
        API_TO_DB.put("isexternal", "IsExternal");
        API_TO_DB.put("islocked", "Locked");
        API_TO_DB.put("grouped", "Grouped");

        API_TO_DB.put("duplicate", "Duplicate");
        API_TO_DB.put("contra", "Contra");
        API_TO_DB.put("iscashorderqty", "IsCashOrderQty");

        // API_TO_DB.put("defaulttag1", "DefaultTag1ID");
        //   API_TO_DB.put("defaulttag2", "DefaultTag2ID");
        //  API_TO_DB.put("defaulttag3", "DefaultTag3ID");
        //   API_TO_DB.put("defaulttag4", "DefaultTag4ID");
        //   API_TO_DB.put("defaulttag5", "DefaultTag5ID");

        API_TO_DB.put("allocationmethod", "AllocationMethod");
    }

    public TradeCompareEngine.CompareReport compare(
            List<Map<String, Object>> dbRows,
            List<Map<String, Object>> apiRows) {

        Map<String, Map<String, Object>> dbByKey = indexByKey(dbRows, true);
        Map<String, Map<String, Object>> apiByKey = indexByKey(apiRows, false);

        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(dbByKey.keySet());
        allKeys.addAll(apiByKey.keySet());

        TradeCompareEngine.CompareReport report =
                new TradeCompareEngine.CompareReport();

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

                // ===== avgprice: round to Async precision =====
                if ("avgprice".equals(apiKey)
                        && apiVal instanceof Number
                        && dbVal instanceof Number) {

                    BigDecimal apiBd = new BigDecimal(apiVal.toString())
                            .setScale(4, RoundingMode.HALF_UP);

                    BigDecimal dbBd = new BigDecimal(dbVal.toString())
                            .setScale(4, RoundingMode.HALF_UP);

                    if (apiBd.compareTo(dbBd) != 0) {
                        diffs.put(apiKey, diff(apiKey, dbKey, apiBd, dbBd));
                    }
                    continue;
                }

                // ===== targetqty & filledqty: numeric normalize =====
                if (("targetqty".equals(apiKey) || "filledqty".equals(apiKey))
                        && apiVal instanceof Number
                        && dbVal instanceof Number) {

                    BigDecimal apiBd = new BigDecimal(apiVal.toString())
                            .stripTrailingZeros();

                    BigDecimal dbBd = new BigDecimal(dbVal.toString())
                            .stripTrailingZeros();

                    if (apiBd.compareTo(dbBd) != 0) {
                        diffs.put(apiKey, diff(apiKey, dbKey, apiBd, dbBd));
                    }
                    continue;
                }

                if (!equalSmart(apiVal, dbVal)) {
                    diffs.put(apiKey, diff(apiKey, dbKey, apiVal, dbVal));
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

    private Map<String, Object> diff(
            String apiKey, String dbKey, Object api, Object db) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("apiKey", apiKey);
        m.put("dbKey", dbKey);
        m.put("api", api);
        m.put("db", db);
        return m;
    }

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
}
