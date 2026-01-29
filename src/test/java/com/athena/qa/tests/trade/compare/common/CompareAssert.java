package com.athena.qa.tests.trade.compare.common;

public class CompareAssert {

    private static final boolean STRICT =
            Boolean.parseBoolean(
                    System.getProperty("compare.strict", "false")
            );

    /**
     * Allocation / Placement PASS policy
     * <p>
     * strict = true  -> FAIL nếu có missing hoặc mismatch
     * strict = false -> FAIL CHỈ KHI có mismatches (financial)
     */
    public static void assertResult(CompareResult result) {

        if (STRICT) {
            // strict mode: everything must match
            if (!result.allMatch()) {
                throw new AssertionError(result.toString());
            }
            return;
        }

        // relaxed mode: financial correctness only
        if (!result.mismatches.isEmpty()) {
            throw new AssertionError(
                    "Financial mismatches detected:\n" + result
            );
        }

        // non-blocking warnings
        if (!result.missingInDb.isEmpty() || !result.missingInApi.isEmpty()) {
            System.out.println("⚠️ Coverage differences (non-blocking):");
            System.out.println(result);
        }
    }
}
