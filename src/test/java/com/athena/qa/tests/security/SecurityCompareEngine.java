package com.athena.qa.tests.security;

import com.athena.qa.tests.base.BaseCompareEngine;

import java.util.*;

public class SecurityCompareEngine extends BaseCompareEngine {

    /**
     * Map API-key(lowercase) -> DB column name
     */
    private static final Map<String, String> API_TO_DB = new LinkedHashMap<>();

    static {
        API_TO_DB.put("id", "ID");
        API_TO_DB.put("identifier", "Identifier");
        //Duyen we agreed label and description had a limitation that doesn’t make sense, go to the table, It’s not a bug
        //API_TO_DB.put("label", "Label");
        //  API_TO_DB.put("fixsymbolsource", "FIXSymbolSource");
        //  API_TO_DB.put("fixsymbol ", "FIXSymbol ");
        // API_TO_DB.put("description", "Description");
        API_TO_DB.put("typecode", "TypeCode");
        API_TO_DB.put("subtypecode", "SubTypeCode");
        API_TO_DB.put("athenasecuritytypeid", "AthenaSecurityTypeID");
        API_TO_DB.put("exchangeid", "ExchangeID");
        API_TO_DB.put("exchange", "ExchangeMICCode");
        API_TO_DB.put("pricecurrency", "PriceCurrencyCode");
        API_TO_DB.put("settlecurrency", "SettleCurrencyCode");
        API_TO_DB.put("price", "Price");
        API_TO_DB.put("costfactor", "CostFactor");
        API_TO_DB.put("notionalfactor", "NotionalFactor");
        API_TO_DB.put("principalfactor", "PrincipalFactor");
        API_TO_DB.put("underlyingid", "UnderlyingID");
        API_TO_DB.put("cusip", "CUSIP");
        API_TO_DB.put("sedol", "SEDOL");
        API_TO_DB.put("isin", "ISIN");
        API_TO_DB.put("ric", "RIC");
        API_TO_DB.put("bbticker", "BBTicker");
        API_TO_DB.put("region", "Region");
        API_TO_DB.put("precision", "Precision");
        API_TO_DB.put("currencyprecision", "CurrencyPrecision");
        API_TO_DB.put("initiallotsize", "InitialLotSize");
        API_TO_DB.put("lotsize", "LotSize");
        API_TO_DB.put("allowcross", "AllowCross");
        API_TO_DB.put("tag167", "Tag167");
        API_TO_DB.put("rtsymbolidentifier", "RTSymbolIdentifier");
        API_TO_DB.put("defaultdaystosettle", "DefaultDaysToSettle");


        // alias
        API_TO_DB.put("code", "Identifier");

        // code -> Identifier
        //API_TO_DB.put("code", "Identifier");

        // extended: bạn có thể bật lại sau (cần XML/JSON normalize)
        // API_TO_DB.put("extended", "ExtendedData");

    }

    // ======================================================
    // Compare
    // ======================================================

    public CompareReport compare(
            List<Map<String, Object>> dbRows,
            List<Map<String, Object>> apiRows) {

        Map<Integer, Map<String, Object>> dbById = indexById(dbRows, true);
        Map<Integer, Map<String, Object>> apiById = indexById(apiRows, false);

        Set<Integer> allIds = new TreeSet<>();
        allIds.addAll(dbById.keySet());
        allIds.addAll(apiById.keySet());

        CompareReport report = new CompareReport();

        report.onlyInDb.addAll(diff(dbById.keySet(), apiById.keySet()));
        report.onlyInApi.addAll(diff(apiById.keySet(), dbById.keySet()));

        for (Integer id : allIds) {
            Map<String, Object> db = dbById.get(id);
            Map<String, Object> api = apiById.get(id);
            if (db == null || api == null) continue;

            Map<String, Object> mismatches = new LinkedHashMap<>();

            for (var e : API_TO_DB.entrySet()) {
                String apiKey = e.getKey();
                String dbKey = e.getValue();

                if (!db.containsKey(dbKey) && !"code".equals(apiKey)) continue;

                Object apiVal = getIgnoreCase(api, apiKey);
                Object dbVal = db.get(dbKey);

                if ("code".equals(apiKey) && dbVal == null) {
                    dbVal = db.get("Identifier");
                }

                if (!equalSmart(apiVal, dbVal)) {
                    Map<String, Object> diff = new LinkedHashMap<>();
                    diff.put("apiKey", apiKey);
                    diff.put("dbKey", dbKey);
                    diff.put("api", apiVal);
                    diff.put("db", dbVal);
                    mismatches.put(apiKey, diff);
                }
            }

            if (!mismatches.isEmpty()) {
                report.mismatchById.put(id, mismatches);
            }
        }

        return report;
    }

    // ======================================================
    // Helpers
    // ======================================================

    private Map<Integer, Map<String, Object>> indexById(List<Map<String, Object>> rows, boolean isDb) {
        Map<Integer, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer id = isDb
                    ? safeInt(row, "ID", "SecurityID", "SecurityId", "Id", "id")
                    : safeInt(row, "id", "securityID", "securityId", "ID", "Id");
            if (id != null) out.putIfAbsent(id, row);
        }
        return out;
    }

    private Integer safeInt(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v instanceof Number) return ((Number) v).intValue();
            if (v != null) {
                try {
                    return Integer.parseInt(v.toString());
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private Object getIgnoreCase(Map<String, Object> row, String keyLower) {
        for (String k : row.keySet()) {
            if (k != null && k.toLowerCase(Locale.ROOT).equals(keyLower)) {
                return row.get(k);
            }
        }
        return null;
    }

    private Set<Integer> diff(Set<Integer> a, Set<Integer> b) {
        Set<Integer> out = new TreeSet<>(a);
        out.removeAll(b);
        return out;
    }

    // ======================================================
    // Report (JSON ONLY)
    // ======================================================

    public static class CompareReport {
        public final Set<Integer> onlyInDb = new TreeSet<>();
        public final Set<Integer> onlyInApi = new TreeSet<>();
        public final Map<Integer, Map<String, Object>> mismatchById = new TreeMap<>();

        public boolean isAllMatch() {
            return onlyInDb.isEmpty()
                    && onlyInApi.isEmpty()
                    && mismatchById.isEmpty();
        }
    }
}
