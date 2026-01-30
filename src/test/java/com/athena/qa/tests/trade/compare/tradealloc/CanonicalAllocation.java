package com.athena.qa.tests.trade.compare.tradealloc;

import java.math.BigDecimal;
import java.util.Map;

public class CanonicalAllocation {

    // Identity
    public String tradeClOrdID;   // chỉ để debug
    public String allocID;        // 🔑 KEY DUY NHẤT
    public String accountId;

    // Quantities
    public BigDecimal quantity = BigDecimal.ZERO;
    public BigDecimal filledQty = BigDecimal.ZERO;
    public BigDecimal netMoney = BigDecimal.ZERO;

    // Commissions
    public BigDecimal comm1 = BigDecimal.ZERO;
    public BigDecimal comm2 = BigDecimal.ZERO;
    public BigDecimal comm3 = BigDecimal.ZERO;
    public BigDecimal comm4 = BigDecimal.ZERO;
    public BigDecimal comm5 = BigDecimal.ZERO;

    // Fees
    public BigDecimal fee1 = BigDecimal.ZERO;
    public BigDecimal fee2 = BigDecimal.ZERO;
    public BigDecimal fee3 = BigDecimal.ZERO;
    public BigDecimal fee4 = BigDecimal.ZERO;
    public BigDecimal fee5 = BigDecimal.ZERO;

    /**
     * 🔑 KEY CHUẨN
     */
    public String key() {
        return allocID;
    }

    /**
     * DB → Canonical
     */
    public static CanonicalAllocation fromDbRow(Map<String, Object> r) {
        CanonicalAllocation c = new CanonicalAllocation();

        c.tradeClOrdID = s(r.get("tradeClOrdID"));
        c.allocID = s(r.get("allocID"));
        c.accountId = s(r.get("accountId"));

        c.quantity = bd(r.get("quantity"));
        c.filledQty = bd(r.get("filledQty"));
        c.netMoney = bd(r.get("netMoney"));

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

        return c;
    }

    /**
     * 🔍 COMPARE VALUE CỦA TẤT CẢ FIELD
     */
    public boolean equalsWithTolerance(CanonicalAllocation o, BigDecimal tol) {
        return
                // eq(quantity, o.quantity, tol)
                //&& eq(filledQty, o.filledQty, tol)
                eqAbs(quantity, o.quantity, tol)
                        && eqAbs(filledQty, o.filledQty, tol)

                        && eq(netMoney, o.netMoney, tol)
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

    // ================= helpers =================

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

    @Override
    public String toString() {
        return "CanonicalAllocation{" +
                "allocID='" + allocID + '\'' +
                ", accountId='" + accountId + '\'' +
                ", quantity=" + quantity +
                ", filledQty=" + filledQty +
                ", netMoney=" + netMoney +
                ", comm1=" + comm1 +
                ", comm2=" + comm2 +
                ", comm3=" + comm3 +
                ", comm4=" + comm4 +
                ", comm5=" + comm5 +
                ", fee1=" + fee1 +
                ", fee2=" + fee2 +
                ", fee3=" + fee3 +
                ", fee4=" + fee4 +
                ", fee5=" + fee5 +
                '}';
    }

    private static boolean eqAbs(BigDecimal a, BigDecimal b, BigDecimal tol) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.abs().subtract(b.abs()).abs().compareTo(tol) <= 0;
    }

}
