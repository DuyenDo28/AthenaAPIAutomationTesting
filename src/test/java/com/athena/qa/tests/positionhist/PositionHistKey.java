package com.athena.qa.tests.positionhist;

import java.util.Objects;

public class PositionHistKey {

    public Integer accountID;
    public Integer securityID;

    public Integer tag1ID, tag2ID, tag3ID, tag4ID, tag5ID;
    public String tag1, tag2, tag3, tag4, tag5;

    // ✅ NEW
    public String side;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionHistKey)) return false;
        PositionHistKey k = (PositionHistKey) o;
        return Objects.equals(accountID, k.accountID)
                && Objects.equals(securityID, k.securityID)
                && Objects.equals(tag1ID, k.tag1ID)
                && Objects.equals(tag2ID, k.tag2ID)
                && Objects.equals(tag3ID, k.tag3ID)
                && Objects.equals(tag4ID, k.tag4ID)
                && Objects.equals(tag5ID, k.tag5ID)
                && Objects.equals(tag1, k.tag1)
                && Objects.equals(tag2, k.tag2)
                && Objects.equals(tag3, k.tag3)
                && Objects.equals(tag4, k.tag4)
                && Objects.equals(tag5, k.tag5)
                && Objects.equals(side, k.side);   // ✅
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                accountID, securityID,
                tag1ID, tag2ID, tag3ID, tag4ID, tag5ID,
                tag1, tag2, tag3, tag4, tag5,
                side // ✅
        );
    }

    @Override
    public String toString() {
        return accountID + "|" + securityID + "|"
                + tag1ID + "|" + tag2ID + "|" + tag3ID + "|"
                + tag4ID + "|" + tag5ID + "|"
                + side; // ✅
    }
}
