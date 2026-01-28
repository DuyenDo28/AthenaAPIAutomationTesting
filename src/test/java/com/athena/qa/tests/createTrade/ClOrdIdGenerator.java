package com.athena.qa.tests.createTrade;

import java.util.UUID;

/**
 * Generate unique clOrdID
 * Business rule: clOrdID MUST be unique
 */
public final class ClOrdIdGenerator {

    private ClOrdIdGenerator() {
        // utility class
    }

    public static String next() {
        return "QA-"
                + System.currentTimeMillis()
                + "-"
                + UUID.randomUUID();
    }
}
