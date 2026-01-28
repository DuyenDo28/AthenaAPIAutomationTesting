package com.athena.qa.framework.config;

import java.time.LocalDateTime;

public class TradeMultiConfig {

    private final int count;
    private final int userId;
    private final String userCode;

    private final int targetQty;
    private final int filledQty;
    private final double avgPrice;

    private final LocalDateTime tradeDate;
    private final LocalDateTime settleDate;

    private final int priceCurrencyId;
    private final String priceCurrencyCode;
    private final int settleCurrencyId;
    private final String settleCurrencyCode;

    private final String accountCode;
    private final int batchSize;

    public TradeMultiConfig(
            int count,
            int userId,
            String userCode,
            int targetQty,
            int filledQty,
            double avgPrice,
            LocalDateTime tradeDate,
            LocalDateTime settleDate,
            int priceCurrencyId,
            String priceCurrencyCode,
            int settleCurrencyId,
            String settleCurrencyCode,
            String accountCode,
            int batchSize
    ) {
        this.count = Math.max(1, count);
        this.userId = userId;
        this.userCode = userCode;
        this.targetQty = targetQty;
        this.filledQty = filledQty;
        this.avgPrice = avgPrice;
        this.tradeDate = tradeDate;
        this.settleDate = settleDate;
        this.priceCurrencyId = priceCurrencyId;
        this.priceCurrencyCode = priceCurrencyCode;
        this.settleCurrencyId = settleCurrencyId;
        this.settleCurrencyCode = settleCurrencyCode;
        this.accountCode = accountCode;
        this.batchSize = batchSize <= 0 ? 200 : batchSize;
    }

    public int count() {
        return count;
    }

    public int userId() {
        return userId;
    }

    public String userCode() {
        return userCode;
    }

    public int targetQty() {
        return targetQty;
    }

    public int filledQty() {
        return filledQty;
    }

    public double avgPrice() {
        return avgPrice;
    }

    public LocalDateTime tradeDate() {
        return tradeDate;
    }

    public LocalDateTime settleDate() {
        return settleDate;
    }

    public int priceCurrencyId() {
        return priceCurrencyId;
    }

    public String priceCurrencyCode() {
        return priceCurrencyCode;
    }

    public int settleCurrencyId() {
        return settleCurrencyId;
    }

    public String settleCurrencyCode() {
        return settleCurrencyCode;
    }

    public String accountCode() {
        return accountCode;
    }

    public int batchSize() {
        return batchSize;
    }
}
