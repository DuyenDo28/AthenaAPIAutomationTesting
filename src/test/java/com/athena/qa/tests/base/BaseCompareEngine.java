package com.athena.qa.tests.base;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public abstract class BaseCompareEngine {

   /* protected boolean equalSmart(Object a, Object b) {

        // 1) null-like == null-like
        if (isNullLike(a) && isNullLike(b)) return true;

        // 2) null-like == zero
        if (isNullLike(a) && isZeroLike(b)) return true;
        if (isNullLike(b) && isZeroLike(a)) return true;

        // 3) date-like normalize (YYYY-MM-DD)
        String da = normalizeDateString(a);
        String db = normalizeDateString(b);
        if (da != null && db != null) {
            return da.equals(db);
        }

        // 4) boolean <-> number (true=1, false=0)
        Integer ba = normalizeTo01(a);
        Integer bb = normalizeTo01(b);
        if (ba != null && bb != null) {
            return ba.equals(bb);
        }

        // 5) number <-> number (tolerance)
        if (a instanceof Number && b instanceof Number) {
            double x = ((Number) a).doubleValue();
            double y = ((Number) b).doubleValue();
            return Math.abs(x - y) <= 0.00001;
        }

        // 6) fallback string compare
        return Objects.toString(a, "").trim()
                .equalsIgnoreCase(Objects.toString(b, "").trim());
    }*/

    /**
     * Normalize ISO date-time or date to YYYY-MM-DD
     * Returns null if value is not date-like
     */
    protected String normalizeDateString(Object v) {
        if (v == null) return null;

        String s = v.toString().trim();
        if (s.isEmpty()) return null;

        // ISO datetime: 2024-11-01T00:00:00
        int tIdx = s.indexOf('T');
        if (tIdx > 0) {
            return s.substring(0, tIdx);
        }

        // Plain date: 2024-11-01
        if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return s;
        }

        return null;
    }


    // ======================================================
    // Helpers
    // ======================================================

    protected boolean isNullLike(Object v) {
        if (v == null) return true;
        String s = v.toString().trim();
        return s.isEmpty() || "null".equalsIgnoreCase(s);
    }

    protected boolean isZeroLike(Object v) {
        if (v == null) return false;
        if (v instanceof Number) {
            return new BigDecimal(v.toString())
                    .compareTo(BigDecimal.ZERO) == 0;
        }
        try {
            return new BigDecimal(v.toString().trim())
                    .compareTo(BigDecimal.ZERO) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Normalize boolean / number / string to 0 or 1
     * true  -> 1
     * false -> 0
     * null  -> null
     */
    protected Integer normalizeTo01(Object v) {
        if (v == null) return null;

        if (v instanceof Boolean) {
            return (Boolean) v ? 1 : 0;
        }

        if (v instanceof Number) {
            int n = ((Number) v).intValue();
            if (n == 0) return 0;
            if (n == 1) return 1;
            return null;
        }

        String s = v.toString().trim().toLowerCase(Locale.ROOT);
        if ("true".equals(s) || "1".equals(s) || "yes".equals(s)) return 1;
        if ("false".equals(s) || "0".equals(s) || "no".equals(s)) return 0;

        return null;
    }

    public boolean equalSmart(Object a, Object b) {

        // 1) null-like == null-like
        if (isNullLike(a) && isNullLike(b)) return true;

        // 2) null-like == zero-like   cover api=0 vs db=null
        if (isNullLike(a) && isZeroLike(b)) return true;
        if (isNullLike(b) && isZeroLike(a)) return true;

        // 3) date normalize  cover "2024-11-01T00:00:00" vs "2024-11-01"
        String da = normalizeDateString(a);
        String db = normalizeDateString(b);
        if (da != null && db != null) {
            return da.equals(db);
        }

        // 4) boolean <-> number (true=1 false=0)
        Integer ba = normalizeTo01(a);
        Integer bb = normalizeTo01(b);
        if (ba != null && bb != null) {
            return ba.equals(bb);
        }

        // 5) number <-> number tolerance
        if (a instanceof Number && b instanceof Number) {
            double x = ((Number) a).doubleValue();
            double y = ((Number) b).doubleValue();
            return Math.abs(x - y) <= 0.00001;
        }

        // 6) fallback string compare
        return Objects.toString(a, "").trim()
                .equalsIgnoreCase(Objects.toString(b, "").trim());
    }

}
