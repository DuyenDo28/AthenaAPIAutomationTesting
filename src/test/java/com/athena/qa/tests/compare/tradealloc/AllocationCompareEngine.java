package com.athena.qa.tests.compare.tradealloc;

import com.athena.qa.tests.compare.common.CompareResult;

import java.math.BigDecimal;
import java.util.Map;

public class AllocationCompareEngine {

    public static CompareResult compare(
            Map<String, CanonicalAllocation> apiMap,
            Map<String, CanonicalAllocation> dbMap) {

        CompareResult r = new CompareResult("allocID");
        BigDecimal tol = new BigDecimal("0.01");

        // 🔍 DEBUG: API KEY vs DB KEY
      /*  System.out.println("\n========== DEBUG API vs DB KEYS ==========");

        System.out.println("\n--- API KEYS ---");
        for (Map.Entry<String, CanonicalAllocation> e : apiMap.entrySet()) {
            CanonicalAllocation a = e.getValue();
            System.out.println(
                    "API KEY = [" + e.getKey() + "]"
                            + " | allocID=" + a.allocID
                            + " | qty=" + a.quantity
                            + " | netMoney=" + a.netMoney
            );
        }

        System.out.println("\n--- DB KEYS ---");
        for (Map.Entry<String, CanonicalAllocation> e : dbMap.entrySet()) {
            CanonicalAllocation d = e.getValue();
            System.out.println(
                    "DB  KEY = [" + e.getKey() + "]"
                            + " | allocID=" + d.allocID
                            + " | qty=" + d.quantity
                            + " | netMoney=" + d.netMoney
            );
        }
        // ===================================================== */
// 🔍 DEBUG: API vs DB VALUE BY allocID
// =====================================================
        System.out.println("\n========== DEBUG API vs DB VALUE (allocID) ==========");

        for (String key : apiMap.keySet()) {
            CanonicalAllocation api = apiMap.get(key);
            CanonicalAllocation db = dbMap.get(key);

            System.out.println("\nALLOCID = [" + key + "]");

            if (api != null) {
                System.out.println("API VALUE:");
                //  System.out.println("  accountId = " + api.accountId);
                System.out.println("  quantity  = " + api.quantity);
                System.out.println("  filledQty = " + api.filledQty);
                System.out.println("  netMoney  = " + api.netMoney);
                System.out.println("  comm1..5  = "
                        + api.comm1 + ", "
                        + api.comm2 + ", "
                        + api.comm3 + ", "
                        + api.comm4 + ", "
                        + api.comm5);
                System.out.println("  fee1..5   = "
                        + api.fee1 + ", "
                        + api.fee2 + ", "
                        + api.fee3 + ", "
                        + api.fee4 + ", "
                        + api.fee5);
            } else {
                System.out.println("API VALUE: <NULL>");
            }

            if (db != null) {
                System.out.println("DB VALUE:");
                System.out.println("  accountId = " + db.accountId);
                System.out.println("  quantity  = " + db.quantity);
                System.out.println("  filledQty = " + db.filledQty);
                System.out.println("  netMoney  = " + db.netMoney);
                System.out.println("  comm1..5  = "
                        + db.comm1 + ", "
                        + db.comm2 + ", "
                        + db.comm3 + ", "
                        + db.comm4 + ", "
                        + db.comm5);
                System.out.println("  fee1..5   = "
                        + db.fee1 + ", "
                        + db.fee2 + ", "
                        + db.fee3 + ", "
                        + db.fee4 + ", "
                        + db.fee5);
            } else {
                System.out.println("DB VALUE: <NULL>");
            }
        }


        // 🔁 COMPARE
        for (String key : apiMap.keySet()) {
            CanonicalAllocation api = apiMap.get(key);
            CanonicalAllocation db = dbMap.get(key);

            //  System.out.println("\n===== COMPARE allocID = [" + key + "] =====");
            //  System.out.println("API = " + api);
            //   System.out.println("DB  = " + db);

            if (db == null) {
                r.missingInDb.add(key);
            } else if (!api.equalsWithTolerance(db, tol)) {
                r.mismatches.add(key);
            }
        }

        for (String key : dbMap.keySet()) {
            if (!apiMap.containsKey(key)) {
                r.missingInApi.add(key);
            }
        }

        return r;
    }
}
