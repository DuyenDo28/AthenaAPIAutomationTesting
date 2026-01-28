package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PositionHistDbClient extends BaseDbClient {

    public PositionHistDbClient(DbConfig dbConfig) {
        super(dbConfig);
    }

    public List<Map<String, Object>> fetchPositionHist(LocalDate asOfDate) {
        return callProc(
                "EXEC dbo.ssp_view_back_dated_position_all_accounts ?;",
                java.sql.Date.valueOf(asOfDate)
        );
    }
}
