package com.athena.qa.framework.client;

import com.athena.qa.framework.config.TradeMultiConfig;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Build payload cho /api/Trades/Multi
 * Nghiệp vụ CHUẨN:
 * - CHỈ dùng Equity (ZVZZT)
 * - KHÔNG set orderID / allocID / clOrdID (để backend auto-generate)
 */
public class TradePayloadFactory {

    // ✅ Equity duy nhất đã được backend chứng minh là hợp lệ
    private static final int EQUITY_SECURITY_ID = 573;
    private static final String EQUITY_SYMBOL = "ZVZZT";

    public static Map<String, Object> buildTrade(TradeMultiConfig cfg, int seq) {

        ThreadLocalRandom r = ThreadLocalRandom.current();

        // Random theo training
        String transactionType = r.nextBoolean() ? "BY" : "SL";

        boolean confirmed = r.nextBoolean();
        boolean isArchived = r.nextBoolean();
        boolean isExternal = r.nextBoolean();
        boolean isLocked = r.nextBoolean();
        boolean grouped = r.nextBoolean();

        String defaultTag1 = randTag();
        String defaultTag2 = randTag();
        String defaultTag3 = randTag();
        String defaultTag4 = randTag();
        String defaultTag5 = randTag();

        // tradeDate: dùng từ config (đúng như Postman)
        LocalDateTime tradeDate = cfg.tradeDate();
        LocalDateTime settleDate = cfg.settleDate();

        Map<String, Object> trade = new HashMap<>();

        trade.put("id", seq);

        // 🔴 BẮT BUỘC để "" → backend auto-generate
        trade.put("clOrdID", "");

        trade.put("userID", cfg.userId());
        trade.put("userCode", cfg.userCode());
        trade.put("transactionType", transactionType);

        // ✅ Equity
        trade.put("securityID", EQUITY_SECURITY_ID);
        trade.put("symbol", EQUITY_SYMBOL);

        trade.put("targetQty", cfg.targetQty());
        trade.put("filledQty", cfg.filledQty());
        trade.put("avgPrice", cfg.avgPrice());

        trade.put("tradeDate", tradeDate.toString());
        trade.put("settleDate", settleDate.toString());

        trade.put("priceCncyID", cfg.priceCurrencyId());
        trade.put("priceCncy", cfg.priceCurrencyCode());
        trade.put("settleCncyID", cfg.settleCurrencyId());
        trade.put("settleCncy", cfg.settleCurrencyCode());

        trade.put("confirmed", confirmed);
        trade.put("isArchived", isArchived);
        trade.put("isExternal", isExternal);
        trade.put("isLocked", isLocked);
        trade.put("grouped", grouped);

        trade.put("defaultTag1", defaultTag1);
        trade.put("defaultTag2", defaultTag2);
        trade.put("defaultTag3", defaultTag3);
        trade.put("defaultTag4", defaultTag4);
        trade.put("defaultTag5", defaultTag5);

        trade.put("duplicate", true);
        trade.put("contra", false);

        trade.put("orderState", "MANUAL");
        trade.put("allocationMethod", "GroupRatio");

        trade.put("groupAllocations", List.of(
                Map.of(
                        "groupCode", cfg.accountCode(),
                        "quantity", cfg.targetQty()
                )
        ));

        // 🔴 allocID = "" (backend auto-generate)
        trade.put("allocations", List.of(
                Map.of(
                        "allocID", "",
                        "accountCode", cfg.accountCode(),
                        "quantity", cfg.targetQty(),
                        "filledQty", cfg.filledQty(),
                        "netMoney", -304,
                        "currency", cfg.priceCurrencyCode(),
                        "commissions", Map.of(
                                "comm1", -19,
                                "comm2", 0,
                                "comm3", 0,
                                "comm4", 0,
                                "comm5", 0
                        ),
                        "fees", Map.of(
                                "fee1", -19,
                                "fee2", -38,
                                "fee3", -57,
                                "fee4", -76,
                                "fee5", -95
                        )
                )
        ));

        // 🔴 placements: clOrdID / orderID = ""
        trade.put("placements", List.of(
                Map.of(
                        "clOrdID", "",
                        "orderID", "",
                        "brokerCode", "INTERNAL",
                        "manual", true,
                        "status", "MANUAL",
                        "quantity", cfg.targetQty(),
                        "filledQty", cfg.filledQty(),
                        "instruction", "MKT",
                        "timeInForce", "DAY"
                )
        ));

        trade.put("isCashOrderQty", false);
        trade.put("complianceRestrictions", null);

        return trade;
    }

    private static String randTag() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1, 5)); // "1".."4"
    }
}
