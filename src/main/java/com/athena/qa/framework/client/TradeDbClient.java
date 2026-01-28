package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;

import java.util.List;
import java.util.Map;

public class TradeDbClient extends BaseDbClient {

    public TradeDbClient(DbConfig dbConfig) {
        super(dbConfig);
    }

    /**
     * Intraday trades
     */
    public List<Map<String, Object>> fetchTrades() {
        return callProc("EXEC ssp_get_intraday_orders_data;");
    }

    public List<Map<String, Object>> fetchIntradayOrders() {
        // @isCompressed = 0
        return callProc(
                "{ call dbo.ssp_get_intraday_orders_data(?) }",
                false
        );

    }

    public List<Map<String, Object>> fetchHistoricalOrders(
            String startDate,
            String endDate) {

        return callProc(
                "{ call dbo.ssp_get_historical_orders_data(?, ?) }",
                startDate,
                endDate
        );
    }

}
