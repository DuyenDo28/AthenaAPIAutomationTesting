package com.athena.qa.tests.positionhist;

import com.athena.qa.tests.base.BaseCompareEngine;

import java.util.*;

public class PositionHistCompareEngine extends BaseCompareEngine {

    /**
     * API field (lowercase) -> DB column
     */
    private static final Map<String, String> API_TO_DB = new LinkedHashMap<>();
    private static final boolean DEBUG = true;


    static {
        API_TO_DB.put("securityid", "SecurityID");
        API_TO_DB.put("asofdate", "AsOfDate");

        API_TO_DB.put("accountid", "AccountID");
        API_TO_DB.put("accountcode", "AccountCode");
        API_TO_DB.put("accountbasecurrency", "AccountCCY");

        API_TO_DB.put("symbol", "Symbol");

        API_TO_DB.put("price", "Price");
        API_TO_DB.put("pricecurrencyid", "PriceCurrencyID");
        API_TO_DB.put("pricecurrencycode", "PriceCurrencyCode");

        API_TO_DB.put("settlecurrencyid", "CounterCurrencyID");
        API_TO_DB.put("settlecurrencycode", "CounterCurrencyCode");

        API_TO_DB.put("lastavailfxrate", "LastAvailFXRate");
        API_TO_DB.put("lastavailfxratemult", "LastAvailFXRateMult");
        API_TO_DB.put("accountfxrate", "AcctFXRate");

        API_TO_DB.put("allowcross", "AllowCross");
        API_TO_DB.put("cashimpact", "CashImpact");

        API_TO_DB.put("factornotional", "FactorNotional");

        API_TO_DB.put("sectypecode", "SecTypeCode");
        API_TO_DB.put("sectypesubcode", "SecTypeSubCode");

        API_TO_DB.put("costbasis", "CostBasis");
        API_TO_DB.put("settlecostbasis", "SettleCostBasis");
        API_TO_DB.put("securitycostbasis", "SecurityCostBasis");

        API_TO_DB.put("tag1id", "Tag1ID");
        API_TO_DB.put("tag2id", "Tag2ID");
        API_TO_DB.put("tag3id", "Tag3ID");
        API_TO_DB.put("tag4id", "Tag4ID");
        API_TO_DB.put("tag5id", "Tag5ID");

        API_TO_DB.put("tag1", "Tag1");
        API_TO_DB.put("tag2", "Tag2");
        API_TO_DB.put("tag3", "Tag3");
        API_TO_DB.put("tag4", "Tag4");
        API_TO_DB.put("tag5", "Tag5");

        API_TO_DB.put("side", "Side");

        API_TO_DB.put("sodqty", "SODQty");
        API_TO_DB.put("currentqty", "CurrQty");

        API_TO_DB.put("itdsettleinterestunrealized", "ITDSettleInterestUnrealized");
        API_TO_DB.put("itdsettleunrealized", "ITDSettleUnrealized");
        API_TO_DB.put("itdunrealized", "ITDUnrealized");
        API_TO_DB.put("itdrealized", "ITDRealized");
        API_TO_DB.put("itddividend", "ITDDividend");
        API_TO_DB.put("itdinterestunrealized", "ITDInterestUnrealized");
        API_TO_DB.put("itdinterestrealized", "ITDInterestRealized");

        API_TO_DB.put("ytdunrealized", "YTDUnrealized");
        API_TO_DB.put("ytdrealized", "YTDRealized");
        API_TO_DB.put("ytddividend", "YTDDividend");
        API_TO_DB.put("ytdinterestunrealized", "YTDInterestUnrealized");
        API_TO_DB.put("ytdinterestrealized", "YTDInterestRealized");

        API_TO_DB.put("qtdunrealized", "QTDUnrealized");
        API_TO_DB.put("qtdrealized", "QTDRealized");
        API_TO_DB.put("qtddividend", "QTDDividend");
        API_TO_DB.put("qtdinterestunrealized", "QTDInterestUnrealized");
        API_TO_DB.put("qtdinterestrealized", "QTDInterestRealized");

        API_TO_DB.put("mtdunrealized", "MTDUnrealized");
        API_TO_DB.put("mtdrealized", "MTDRealized");
        API_TO_DB.put("mtddividend", "MTDDividend");
        API_TO_DB.put("mtdinterestunrealized", "MTDInterestUnrealized");
        API_TO_DB.put("mtdinterestrealized", "MTDInterestRealized");

        API_TO_DB.put("dtdunrealized", "DTDUnrealized");
        API_TO_DB.put("dtdrealized", "DTDRealized");
        API_TO_DB.put("dtddividend", "DTDDividend");
        API_TO_DB.put("dtdinterestunrealized", "DTDInterestUnrealized");
        API_TO_DB.put("dtdinterestrealized", "DTDInterestRealized");

        API_TO_DB.put("pxyestclose", "PrevDayClosePxLoc");
    }

    // ======================================================
    // Compare
    // ======================================================

    public CompareReport compare(
            List<Map<String, Object>> dbRows,
            List<Map<String, Object>> apiRows
    ) {

        Map<PositionHistKey, Map<String, Object>> dbMap = index(dbRows);
        Map<PositionHistKey, Map<String, Object>> apiMap = index(apiRows);

        Set<PositionHistKey> allKeys = new TreeSet<>(Comparator.comparing(Object::toString));
        allKeys.addAll(dbMap.keySet());
        allKeys.addAll(apiMap.keySet());

        CompareReport report = new CompareReport();

        for (PositionHistKey key : allKeys) {

            Map<String, Object> db = dbMap.get(key);
            Map<String, Object> api = apiMap.get(key);

            if (db == null) {
                report.onlyInApi.add(key.toString());
                continue;
            }
            if (api == null) {
                report.onlyInDb.add(key.toString());
                continue;
            }

            Map<String, Object> diffs = new LinkedHashMap<>();

            for (var e : API_TO_DB.entrySet()) {
                String apiKey = e.getKey();
                String dbKey = e.getValue();

                Object apiVal = getIgnoreCase(api, apiKey);
                Object dbVal = db.get(dbKey);


              /*  if (DEBUG) {
                    System.out.println(
                            "    Field=" + apiKey
                                    + " | API=" + apiVal
                                    + " (" + (apiVal == null ? "null" : apiVal.getClass().getSimpleName()) + ")"
                                    + " | DB=" + dbVal
                                    + " (" + (dbVal == null ? "null" : dbVal.getClass().getSimpleName()) + ")"
                    );
                } */
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

            if (!diffs.isEmpty()) {
                report.mismatchByKey.put(key.toString(), diffs);
            }
        }

        return report;
    }

    // ======================================================
    // Helpers
    // ======================================================

    private Map<PositionHistKey, Map<String, Object>> index(
            List<Map<String, Object>> rows
    ) {
        Map<PositionHistKey, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> r : rows) {
            PositionHistKey k = buildKey(r);
            if (k != null) out.putIfAbsent(k, r);
        }
        return out;
    }

    private PositionHistKey buildKey(Map<String, Object> r) {
        PositionHistKey k = new PositionHistKey();

        k.accountID = intVal(r, "accountID", "AccountID");
        k.securityID = intVal(r, "securityID", "SecurityID");

        k.tag1ID = intVal(r, "tag1ID", "Tag1ID");
        k.tag2ID = intVal(r, "tag2ID", "Tag2ID");
        k.tag3ID = intVal(r, "tag3ID", "Tag3ID");
        k.tag4ID = intVal(r, "tag4ID", "Tag4ID");
        k.tag5ID = intVal(r, "tag5ID", "Tag5ID");

        k.tag1 = strVal(r, "tag1", "Tag1");
        k.tag2 = strVal(r, "tag2", "Tag2");
        k.tag3 = strVal(r, "tag3", "Tag3");
        k.tag4 = strVal(r, "tag4", "Tag4");
        k.tag5 = strVal(r, "tag5", "Tag5");

        k.side = strVal(r, "side", "Side");

        return k;
    }

    private Integer intVal(Map<String, Object> r, String... keys) {
        for (String k : keys) {
            Object v = r.get(k);
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

    private String strVal(Map<String, Object> r, String... keys) {
        for (String k : keys) {
            Object v = r.get(k);
            if (v != null) return v.toString();
        }
        return "";
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
    // Report
    // ======================================================

    public static class CompareReport {
        public final List<String> onlyInDb = new ArrayList<>();
        public final List<String> onlyInApi = new ArrayList<>();
        public final Map<String, Map<String, Object>> mismatchByKey = new LinkedHashMap<>();

        public boolean isAllMatch() {
            return onlyInDb.isEmpty()
                    && onlyInApi.isEmpty()
                    && mismatchByKey.isEmpty();
        }
    }
}
