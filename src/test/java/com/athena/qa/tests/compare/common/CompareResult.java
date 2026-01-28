package com.athena.qa.tests.compare.common;

import java.util.ArrayList;
import java.util.List;

public class CompareResult {

    public String keyName;

    public List<String> missingInDb = new ArrayList<>();
    public List<String> missingInApi = new ArrayList<>();
    public List<String> mismatches = new ArrayList<>();

    public CompareResult(String keyName) {
        this.keyName = keyName;
    }

    // 🔥 BẠN ĐANG THIẾU HÀM NÀY
    public boolean allMatch() {
        return missingInDb.isEmpty()
                && missingInApi.isEmpty()
                && mismatches.isEmpty();
    }

    @Override
    public String toString() {
        return "\nCompareResult{" +
                "\n  allMatch=" + allMatch() +
                ",\n  keyName='" + keyName + '\'' +
                ",\n  missingInDb=" + missingInDb +
                ",\n  missingInApi=" + missingInApi +
                ",\n  mismatches=" + mismatches +
                "\n}";
    }
}
