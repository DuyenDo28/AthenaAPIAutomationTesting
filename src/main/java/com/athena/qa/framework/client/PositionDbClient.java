package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;

import java.util.List;
import java.util.Map;

public class PositionDbClient extends BaseDbClient {

    public PositionDbClient(DbConfig dbConfig) {
        super(dbConfig);
    }

    public List<Map<String, Object>> fetchPositions() {
        // exec ssp_view_get_position_all_accounts
        //   @singleLineParam = 1,
        //   @includeUDAColumns = 0,
        //   @ForceNullAsOfDateValue = 0,
        //   @UserCode = null,
        //   @viewCashAtTag0 = 1

        return callProc(
                "EXEC dbo.ssp_view_get_position_all_accounts " +
                        "@singleLineParam = ?, " +
                        "@includeUDAColumns = ?, " +
                        "@ForceNullAsOfDateValue = ?, " +
                        "@UserCode = ?, " +
                        "@viewCashAtTag0 = ?;",
                1, 0, 0, null, 1
        );
    }
}
