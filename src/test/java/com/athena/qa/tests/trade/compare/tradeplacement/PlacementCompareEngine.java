package com.athena.qa.tests.trade.compare.tradeplacement;

import com.athena.qa.tests.trade.compare.common.CompareResult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class PlacementCompareEngine {

    public static CompareResult compare(
            Map<String, CanonicalPlacement> apiMap,
            Map<String, CanonicalPlacement> dbMap) {

        CompareResult r = new CompareResult("placementClOrdID");
        BigDecimal tol = new BigDecimal("0.01");

        for (String key : apiMap.keySet()) {

            CanonicalPlacement api = apiMap.get(key);
            CanonicalPlacement db = dbMap.get(key);

            System.out.println("\n===== COMPARE PLACEMENT key=" + key + " =====");

            if (db == null) {
                System.out.println("❌ DB MISSING");
                r.missingInDb.add(key);
                continue;
            }

            boolean allOk = true;

            // =========================
            // 🔥 timeInForce (LOG + SO)
            // =========================
            boolean tifOk = Objects.equals(api.timeInForce, db.timeInForce);
            System.out.printf(
                    "%-12s : API=%-10s | DB=%-10s => %s%n",
                    "timeInForce",
                    api.timeInForce,
                    db.timeInForce,
                    tifOk ? "OK" : "FAIL"
            );
            allOk &= tifOk;

            // =========================
            // Numeric compare helper
            // =========================
            BiFunction<BigDecimal, BigDecimal, Boolean> eq =
                    (a, b) -> {
                        if (a == null) a = BigDecimal.ZERO;
                        if (b == null) b = BigDecimal.ZERO;
                        return a.subtract(b).abs().compareTo(tol) <= 0;
                    };

            // =========================
            // NUMERIC FIELDS
            // =========================
            allOk &= log("quantity", api.quantity, db.quantity, eq);
            allOk &= log("filledQty", api.filledQty, db.filledQty, eq);
            allOk &= log("filledValue", api.filledValue, db.filledValue, eq);
            allOk &= log("limitPrice", api.limitPrice, db.limitPrice, eq);

            // =========================
            // COMMISSIONS
            // =========================
            allOk &= log("comm1", api.comm1, db.comm1, eq);
            allOk &= log("comm2", api.comm2, db.comm2, eq);
            allOk &= log("comm3", api.comm3, db.comm3, eq);
            allOk &= log("comm4", api.comm4, db.comm4, eq);
            allOk &= log("comm5", api.comm5, db.comm5, eq);

            // =========================
            // FEES
            // =========================
            allOk &= log("fee1", api.fee1, db.fee1, eq);
            allOk &= log("fee2", api.fee2, db.fee2, eq);
            allOk &= log("fee3", api.fee3, db.fee3, eq);
            allOk &= log("fee4", api.fee4, db.fee4, eq);
            allOk &= log("fee5", api.fee5, db.fee5, eq);

            if (!allOk) {
                r.mismatches.add(key);
                System.out.println("❌ RESULT: MISMATCH");
            } else {
                System.out.println("✅ RESULT: MATCH");
            }

            System.out.println("====================================================");
        }

        // =========================
        // DB keys missing in API
        // =========================
        for (String key : dbMap.keySet()) {
            if (!apiMap.containsKey(key)) {
                System.out.println("❌ API MISSING key=" + key);
                r.missingInApi.add(key);
            }
        }

        return r;
    }

    // =====================================================
    // Helper log method
    // =====================================================
    private static boolean log(
            String field,
            BigDecimal api,
            BigDecimal db,
            BiFunction<BigDecimal, BigDecimal, Boolean> eq) {

        boolean ok = eq.apply(api, db);

        System.out.printf(
                "%-12s : API=%-10s | DB=%-10s => %s%n",
                field,
                api,
                db,
                ok ? "OK" : "FAIL"
        );

        return ok;
    }
}
