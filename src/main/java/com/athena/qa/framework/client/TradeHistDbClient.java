package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;
import com.athena.qa.framework.config.TradeMultiConfig;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TradeHistDbClient extends BaseDbClient {

    public TradeHistDbClient(DbConfig dbConfig) {
        super(dbConfig);
    }

    public List<Map<String, Object>> fetch(LocalDate start, LocalDate end) {
        return callProc(
                "{call dbo.ssp_get_historical_orders_data(?, ?)}",
                Date.valueOf(start),
                Date.valueOf(end)
        );
    }

    /**
     * Build trade payload for /api/Trades/Multi
     * Random fields according to training:
     * - transactionType: BY / SL
     * - securityID + symbol: random (2..30, match)
     * - confirmed / isArchived / isExternal / isLocked / grouped: random boolean
     * - defaultTag1..defaultTag5: random "1".."4"
     */
    public static class TradePayloadFactory {

        // ===== SECURITY LIST (2..30, MATCH ID + SYMBOL) =====

        private record SecurityPick(int securityId, String symbol) {
        }

        private static final SecurityPick[] SECURITY_LIST = new SecurityPick[]{
                new SecurityPick(2, "AED"),
                new SecurityPick(3, "ARS"),
                new SecurityPick(4, "AUD"),
                new SecurityPick(5, "AUD MGMTFEES"),
                new SecurityPick(6, "AUD PERF FEES"),
                new SecurityPick(7, "AUD REDS"),
                new SecurityPick(8, "AUD SUBS"),
                new SecurityPick(9, "AUD/CAD"),
                new SecurityPick(10, "AUD/CAD SPOT"),
                new SecurityPick(11, "AUD/CHF"),
                new SecurityPick(12, "AUD/CHF SPOT"),
                new SecurityPick(13, "AUD/IDR"),
                new SecurityPick(14, "AUD/IDR SPOT"),
                new SecurityPick(15, "AUD/JPY"),
                new SecurityPick(16, "AUD/JPY SPOT"),
                new SecurityPick(17, "AUD/MXN"),
                new SecurityPick(18, "AUD/MXN SPOT"),
                new SecurityPick(19, "AUD/MYR"),
                new SecurityPick(20, "AUD/MYR SPOT"),
                new SecurityPick(21, "AUD/NOK"),
                new SecurityPick(22, "AUD/NOK SPOT"),
                new SecurityPick(23, "AUD/NZD"),
                new SecurityPick(24, "AUD/NZD SPOT"),
                new SecurityPick(25, "AUD/SEK"),
                new SecurityPick(26, "AUD/SEK SPOT"),
                new SecurityPick(27, "AUD/SGD"),
                new SecurityPick(28, "AUD/SGD SPOT"),
                new SecurityPick(29, "AUD/THB"),
                new SecurityPick(30, "AUD/THB SPOT")
        };

        private static SecurityPick randomSecurity() {
            return SECURITY_LIST[
                    ThreadLocalRandom.current().nextInt(SECURITY_LIST.length)
                    ];
        }

        // ===== PUBLIC API =====

        public static Map<String, Object> buildTrade(TradeMultiConfig cfg, int seq) {

            ThreadLocalRandom r = ThreadLocalRandom.current();

            // 🔹 Random theo training
            SecurityPick sec = randomSecurity();
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

            // 🔹 tradeDate luôn mới (tránh backend dedupe)
            LocalDateTime tradeDate = cfg.tradeDate().plusSeconds(seq);
            LocalDateTime settleDate = cfg.settleDate().plusSeconds(seq);

            // 🔹 clOrdID (dùng chung cho trade + placement)
            String clOrdId = generateClOrdId(seq);

            Map<String, Object> trade = new HashMap<>();

            trade.put("clOrdID", clOrdId);
            trade.put("userID", cfg.userId());
            trade.put("userCode", cfg.userCode());
            trade.put("transactionType", transactionType);

            trade.put("securityID", sec.securityId());
            trade.put("symbol", sec.symbol());

            trade.put("targetQty", cfg.targetQty());
            trade.put("filledQty", cfg.filledQty());
            trade.put("avgPrice", cfg.avgPrice());

            // ⚠️ date MUST be String
            trade.put("tradeDate", tradeDate.toString());
            trade.put("settleDate", settleDate.toString());

            trade.put("priceCncyID", cfg.priceCurrencyId());
            trade.put("priceCncy", cfg.priceCurrencyCode());
            trade.put("settleCncyID", cfg.settleCurrencyId());
            trade.put("settleCncy", cfg.settleCurrencyCode());

            // 🔹 random boolean flags
            trade.put("confirmed", confirmed);
            trade.put("isArchived", isArchived);
            trade.put("isExternal", isExternal);
            trade.put("isLocked", isLocked);
            trade.put("grouped", grouped);

            // 🔹 random default tags (1..4)
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

            // 🔥 placement rules đã fix trước đó
            trade.put("placements", List.of(
                    Map.of(
                            "clOrdID", clOrdId,
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

        // ===== HELPERS =====

        private static String randTag() {
            return String.valueOf(ThreadLocalRandom.current().nextInt(1, 5)); // "1".."4"
        }

        private static String generateClOrdId(int seq) {
            return "QA-" + System.currentTimeMillis()
                    + "-" + seq
                    + "-" + UUID.randomUUID();
        }
    }
}
