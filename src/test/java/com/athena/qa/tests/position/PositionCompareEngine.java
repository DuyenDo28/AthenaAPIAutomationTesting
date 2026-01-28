package com.athena.qa.tests.position;

import com.athena.qa.tests.base.BaseCompareEngine;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class PositionCompareEngine extends BaseCompareEngine {

    // API(lowercase) -> DB column
    private static final Map<String, String> API_TO_DB = new LinkedHashMap<>();
    private static final boolean DEBUG = true;

    static {
        API_TO_DB.put("accountid", "AccountID");
        API_TO_DB.put("accountcode", "AccountCode");
        API_TO_DB.put("accountbasecurrency", "AccountBaseCurrency");

        API_TO_DB.put("securityid", "SecurityID");
        API_TO_DB.put("symbol", "Symbol");

        API_TO_DB.put("price", "Price");
        API_TO_DB.put("pricecurrencyid", "PriceCurrencyID");
        API_TO_DB.put("pricecurrencycode", "PriceCurrencyCode");

        API_TO_DB.put("settlecurrencyid", "SettleCurrencyID");
        API_TO_DB.put("settlecurrencycode", "SettleCurrencyCode");

        API_TO_DB.put("lastavailfxrate", "LastAvailFXRate");
        API_TO_DB.put("lastavailsettlefxrate", "LastAvailSettleFXRate");
        API_TO_DB.put("lastavailfxratemult", "LastAvailFXRateMult");
        API_TO_DB.put("lastavailsettlefxratemult", "LastAvailSettleFXRateMult");

        API_TO_DB.put("accountfxrate", "AcctFXRate");
        API_TO_DB.put("allowcross", "AllowCross");
        API_TO_DB.put("cashimpact", "CashImpact");

        API_TO_DB.put("closecostbasis", "CloseCostBasis");
        API_TO_DB.put("closesecuritycostbasis", "CloseSecurityCostBasis");

        API_TO_DB.put("factormdfprice", "FactorMDFPrice");
        API_TO_DB.put("factornotional", "FactorNotional");
        API_TO_DB.put("factorexposure", "FactorExposure");

        API_TO_DB.put("intradayincome", "IntradayIncome");
        API_TO_DB.put("intradaysettleincome", "IntradaySettleIncome");

        API_TO_DB.put("itdsettleinterestunrealized", "ITDSettleInterestUnrealized");
        API_TO_DB.put("itdsettleunrealized", "ITDSettleUnrealized");

        API_TO_DB.put("opencostbasis", "OpenCostBasis");
        API_TO_DB.put("opensecuritycostbasis", "OpenSecurityCostBasis");
        API_TO_DB.put("opensettlecostbasis", "OpenSettleCostBasis");

        API_TO_DB.put("prevdayclosepxloc", "PrevDayClosePxLoc");

        API_TO_DB.put("sectypecode", "SecTypeCode");
        API_TO_DB.put("sectypesubcode", "SecTypeSubCode");

        API_TO_DB.put("settlecostbasis", "SettleCostBasis");

        API_TO_DB.put("tradecloseqty", "TradeCloseQty");
        API_TO_DB.put("tradeopenqty", "TradeOpenQty");

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

        // bạn có dòng "CounterCurrencyID/Code" ở mapping position,
        // nhưng DB mapping bạn gửi có settleCurrency... (đã map ở trên)
        // Nếu DB có CounterCurrencyID/Code thật, bạn add tiếp ở đây.

        API_TO_DB.put("sodqty", "SODQty");
        API_TO_DB.put("currentqty", "CurrQty");
        API_TO_DB.put("proposedqty", "ProposedQty");
        API_TO_DB.put("dayssinceinception", "DaysSinceInception");

        API_TO_DB.put("itdunrealized", "ITDUnrealized");
        API_TO_DB.put("itdrealized", "ITDRealized");
        API_TO_DB.put("itdfxunrealized", "ITDFXUnrealized");
        API_TO_DB.put("itdfxrealized", "ITDFXRealized");
        API_TO_DB.put("itddividend", "ITDDividend");
        API_TO_DB.put("itdinterestunrealized", "ITDInterestUnrealized");
        API_TO_DB.put("itdinterestrealized", "ITDInterestRealized");

        API_TO_DB.put("ytdunrealized", "YTDUnrealized");
        API_TO_DB.put("ytdrealized", "YTDRealized");
        API_TO_DB.put("ytdfxunrealized", "YTDFXUnrealized");
        API_TO_DB.put("ytdfxrealized", "YTDFXRealized");
        API_TO_DB.put("ytddividend", "YTDDividend");
        API_TO_DB.put("ytdinterestunrealized", "YTDInterestUnrealized");
        API_TO_DB.put("ytdinterestrealized", "YTDInterestRealized");

        API_TO_DB.put("qtdunrealized", "QTDUnrealized");
        API_TO_DB.put("qtdrealized", "QTDRealized");
        API_TO_DB.put("qtdfxunrealized", "QTDFXUnrealized");
        API_TO_DB.put("qtdfxrealized", "QTDFXRealized");
        API_TO_DB.put("qtddividend", "QTDDividend");
        API_TO_DB.put("qtdinterestunrealized", "QTDInterestUnrealized");
        API_TO_DB.put("qtdinterestrealized", "QTDInterestRealized");

        API_TO_DB.put("mtdunrealized", "MTDUnrealized");
        API_TO_DB.put("mtdrealized", "MTDRealized");
        API_TO_DB.put("mtdfxunrealized", "MTDFXUnrealized");
        API_TO_DB.put("mtdfxrealized", "MTDFXRealized");
        API_TO_DB.put("mtddividend", "MTDDividend");
        API_TO_DB.put("mtdinterestunrealized", "MTDInterestUnrealized");
        API_TO_DB.put("mtdinterestrealized", "MTDInterestRealized");
    }

    private static final Pattern NUM = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    public CompareReport compare(
            List<Map<String, Object>> dbRows,
            List<Map<String, Object>> apiRows,
            boolean debug
    ) {

        Map<PositionKey, Map<String, Object>> dbMap = indexByKey(dbRows, true);
        Map<PositionKey, Map<String, Object>> apiMap = indexByKey(apiRows, false);

        Set<PositionKey> allKeys = new TreeSet<>(Comparator.comparing(Object::toString));
        allKeys.addAll(dbMap.keySet());
        allKeys.addAll(apiMap.keySet());

        CompareReport report = new CompareReport();
        report.debugEnabled = debug;
        report.debugComparedFields = new ArrayList<>(API_TO_DB.keySet());

        for (PositionKey key : allKeys) {
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

            Map<String, Object> mismatches = new LinkedHashMap<>();

            for (var e : API_TO_DB.entrySet()) {
                String apiKeyLower = e.getKey();
                String dbKey = e.getValue();

                Object apiValRaw = getIgnoreCase(api, apiKeyLower);
                Object dbValRaw = db.get(dbKey);

                Object apiVal = normalizeValue(apiValRaw);
                Object dbVal = normalizeValue(dbValRaw);
              /*  if (DEBUG) {
                    System.out.println(
                            "    Field=" + apiKeyLower
                                    + " | API=" + apiVal
                                    + " (" + (apiVal == null ? "null" : apiVal.getClass().getSimpleName()) + ")"
                                    + " | DB=" + dbVal
                                    + " (" + (dbVal == null ? "null" : dbVal.getClass().getSimpleName()) + ")"
                    );
                } */
                System.out.println(
                        "[DEBUG TRADE COMPARE] key=" + key +
                                " | API." + apiKeyLower + "=" + apiVal +
                                " | DB." + dbKey + "=" + dbVal
                );

                boolean eq = equalSmart(apiVal, dbVal);

                if (debug) {
                    report.debugComparedCount++;
                    if (report.debugSamples.size() < 50) { // tránh quá to
                        report.debugSamples.add(
                                key + " | " + apiKeyLower + " -> " + dbKey
                                        + " | api=" + apiVal + " | db=" + dbVal
                                        + " | equal=" + eq
                        );
                    }
                }

                if (!eq) {
                    Map<String, Object> diff = new LinkedHashMap<>();
                    diff.put("apiKey", apiKeyLower);
                    diff.put("dbKey", dbKey);
                    diff.put("api", apiVal);
                    diff.put("db", dbVal);
                    mismatches.put(apiKeyLower, diff);
                }
            }

            if (!mismatches.isEmpty()) {
                report.mismatchByKey.put(key.toString(), mismatches);
            }
        }

        return report;
    }

    // ======================================================
    // Index & Key
    // ======================================================

    private Map<PositionKey, Map<String, Object>> indexByKey(
            List<Map<String, Object>> rows,
            boolean isDb
    ) {
        Map<PositionKey, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> r : rows) {
            PositionKey k = isDb ? buildDbKey(r) : buildApiKey(r);
            if (k != null) out.putIfAbsent(k, r);
        }
        return out;
    }

    private PositionKey buildDbKey(Map<String, Object> r) {
        PositionKey k = new PositionKey();
        k.accountID = intVal(r.get("AccountID"));
        k.securityID = intVal(r.get("SecurityID"));

        k.tag1ID = intVal(r.get("Tag1ID"));
        k.tag2ID = intVal(r.get("Tag2ID"));
        k.tag3ID = intVal(r.get("Tag3ID"));
        k.tag4ID = intVal(r.get("Tag4ID"));
        k.tag5ID = intVal(r.get("Tag5ID"));

        Object side = r.get("Side");
        k.side = side == null ? "" : side.toString();

        // nếu thiếu 2 field chính thì bỏ
        if (k.accountID == null || k.securityID == null) return null;
        return k;
    }

    private PositionKey buildApiKey(Map<String, Object> r) {
        PositionKey k = new PositionKey();
        k.accountID = intVal(getIgnoreCase(r, "accountid"));
        k.securityID = intVal(getIgnoreCase(r, "securityid"));

        k.tag1ID = intVal(getIgnoreCase(r, "tag1id"));
        k.tag2ID = intVal(getIgnoreCase(r, "tag2id"));
        k.tag3ID = intVal(getIgnoreCase(r, "tag3id"));
        k.tag4ID = intVal(getIgnoreCase(r, "tag4id"));
        k.tag5ID = intVal(getIgnoreCase(r, "tag5id"));

        Object side = getIgnoreCase(r, "side");
        k.side = side == null ? "" : side.toString();

        if (k.accountID == null || k.securityID == null) return null;
        return k;
    }

    private Integer intVal(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (Exception ignored) {
            return null;
        }
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
    // Normalize numbers (covers 6199.42990587181 vs 6199.429905871807)
    // ======================================================

    private Object normalizeValue(Object v) {
        if (v == null) return null;

        // dates: rely on BaseCompareEngine normalizeDateString in equalSmart
        String ds = normalizeDateString(v);
        if (ds != null) return ds;

        // numbers: BigDecimal normalize (even if string)
        if (v instanceof Number) {
            try {
                return new BigDecimal(v.toString());
            } catch (Exception ignored) {
                return v;
            }
        }

        String s = v.toString().trim();
        if (NUM.matcher(s).matches()) {
            try {
                return new BigDecimal(s);
            } catch (Exception ignored) {
                return s;
            }
        }

        return v;
    }

    // ======================================================
    // Report
    // ======================================================

    public static class CompareReport {
        public final List<String> onlyInDb = new ArrayList<>();
        public final List<String> onlyInApi = new ArrayList<>();
        public final Map<String, Map<String, Object>> mismatchByKey = new LinkedHashMap<>();

        // debug
        public boolean debugEnabled;
        public int debugComparedCount;
        public List<String> debugComparedFields;
        public final List<String> debugSamples = new ArrayList<>();

        public boolean isAllMatch() {
            return onlyInDb.isEmpty()
                    && onlyInApi.isEmpty()
                    && mismatchByKey.isEmpty();
        }
    }
}
