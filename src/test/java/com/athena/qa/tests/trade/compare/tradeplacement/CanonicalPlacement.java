package com.athena.qa.tests.trade.compare.tradeplacement;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class CanonicalPlacement {

    // ===== Identity =====
    public String tradeClOrdID;        // giữ để debug, KHÔNG dùng làm key
    public String placementClOrdID;    // 🔑 KEY DUY NHẤT

    // ===== Common fields =====
    public String brokerId;
    public String orderID;
    public String status;
    public String instruction;

    public BigDecimal quantity = BigDecimal.ZERO;
    public BigDecimal filledQty = BigDecimal.ZERO;
    public BigDecimal filledValue = BigDecimal.ZERO;
    public BigDecimal limitPrice = BigDecimal.ZERO;

    // ===== Commissions (value) =====
    public BigDecimal comm1 = BigDecimal.ZERO;
    public BigDecimal comm2 = BigDecimal.ZERO;
    public BigDecimal comm3 = BigDecimal.ZERO;
    public BigDecimal comm4 = BigDecimal.ZERO;
    public BigDecimal comm5 = BigDecimal.ZERO;

    // ===== Fees (value) =====
    public BigDecimal fee1 = BigDecimal.ZERO;
    public BigDecimal fee2 = BigDecimal.ZERO;
    public BigDecimal fee3 = BigDecimal.ZERO;
    public BigDecimal fee4 = BigDecimal.ZERO;
    public BigDecimal fee5 = BigDecimal.ZERO;
    public String timeInForce;


    // ===== KEY =====
    public String key() {
        return placementClOrdID;
    }

    // ===== DB → Canonical =====
    public static CanonicalPlacement fromDbRow(Map<String, Object> r) {
        CanonicalPlacement c = new CanonicalPlacement();

        c.tradeClOrdID = s(r.get("tradeClOrdID"));
        c.placementClOrdID = s(r.get("placementClOrdID"));

        c.brokerId = s(r.get("brokerId"));
        c.orderID = s(r.get("orderID"));
        c.status = s(r.get("status"));
        c.instruction = s(r.get("instruction"));

        c.quantity = bd(r.get("quantity"));
        c.filledQty = bd(r.get("filledQty"));
        c.filledValue = bd(r.get("filledValue"));
        c.limitPrice = bd(r.get("limitPrice"));

        c.comm1 = bd(r.get("comm1"));
        c.comm2 = bd(r.get("comm2"));
        c.comm3 = bd(r.get("comm3"));
        c.comm4 = bd(r.get("comm4"));
        c.comm5 = bd(r.get("comm5"));

        c.fee1 = bd(r.get("fee1"));
        c.fee2 = bd(r.get("fee2"));
        c.fee3 = bd(r.get("fee3"));
        c.fee4 = bd(r.get("fee4"));
        c.fee5 = bd(r.get("fee5"));
        c.timeInForce = s(r.get("timeInForce"));


        return c;
    }

    // ===== STRICT COMPARE (so hết field API & DB cùng có) =====
    public boolean equalsWithTolerance(CanonicalPlacement o, BigDecimal tol) {
        return Objects.equals(timeInForce, o.timeInForce)
                && eq(quantity, o.quantity, tol)
                && eq(filledQty, o.filledQty, tol)
                && eq(filledValue, o.filledValue, tol)
                && eq(limitPrice, o.limitPrice, tol)
                && eq(comm1, o.comm1, tol)
                && eq(comm2, o.comm2, tol)
                && eq(comm3, o.comm3, tol)
                && eq(comm4, o.comm4, tol)
                && eq(comm5, o.comm5, tol)
                && eq(fee1, o.fee1, tol)
                && eq(fee2, o.fee2, tol)
                && eq(fee3, o.fee3, tol)
                && eq(fee4, o.fee4, tol)
                && eq(fee5, o.fee5, tol);
    }

    // ===== helpers =====
    private static boolean eq(BigDecimal a, BigDecimal b, BigDecimal tol) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.subtract(b).abs().compareTo(tol) <= 0;
    }

    private static BigDecimal bd(Object v) {
        if (v == null) return BigDecimal.ZERO;
        return new BigDecimal(v.toString());
    }

    private static String s(Object v) {
        return v == null ? "" : v.toString();
    }
}
