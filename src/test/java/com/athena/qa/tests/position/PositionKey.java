package com.athena.qa.tests.position;

import java.util.Objects;

public class PositionKey {
    public Integer accountID;
    public Integer securityID;
    public Integer tag1ID, tag2ID, tag3ID, tag4ID, tag5ID;
    public String side; // include side in key

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionKey)) return false;
        PositionKey k = (PositionKey) o;
        return Objects.equals(accountID, k.accountID)
                && Objects.equals(securityID, k.securityID)
                && Objects.equals(tag1ID, k.tag1ID)
                && Objects.equals(tag2ID, k.tag2ID)
                && Objects.equals(tag3ID, k.tag3ID)
                && Objects.equals(tag4ID, k.tag4ID)
                && Objects.equals(tag5ID, k.tag5ID)
                && Objects.equals(norm(side), norm(k.side));
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                accountID, securityID,
                tag1ID, tag2ID, tag3ID, tag4ID, tag5ID,
                norm(side)
        );
    }

    @Override
    public String toString() {
        return accountID + "|" + securityID + "|"
                + tag1ID + "|" + tag2ID + "|" + tag3ID + "|" + tag4ID + "|" + tag5ID
                + "|" + norm(side);
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim();
    }
}
