package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;

import java.util.List;
import java.util.Map;

public class TradeAllocDbClient extends BaseDbClient {

    public TradeAllocDbClient(DbConfig dbConfig) {
        super(dbConfig);
    }

    public List<Map<String, Object>> fetchAllocations(boolean isCompressed) {
        return callProc("{ call dbo.ssp_get_intraday_orders_alloc_canonical(?) }", isCompressed);
    }

    /*public List<Map<String, Object>> fetchHistoricalAllocations(
            String startDate,
            String endDate) {

        return callProc(
                "{ call dbo.ssp_get_historical_orders_alloc_canonical(?, ?) }",
                startDate,
                endDate
        );
    } */


    public List<Map<String, Object>> fetchHistoricalAllocation(
            String startDate,
            String endDate) {

        String sql = """
                SELECT
                    th.ClOrdID        AS tradeClOrdID,
                    sa.AllocID        AS allocID,
                    sa.AccountID      AS accountId,
                    sa.Quantity       AS quantity,
                    sa.Quantity       AS filledQty,
                    sa.NetMoney       AS netMoney,
                    sa.Comm1          AS comm1,
                    sa.Comm2          AS comm2,
                    sa.Comm3          AS comm3,
                    sa.Comm4          AS comm4,
                    sa.Comm5          AS comm5,
                    sa.Fee1           AS fee1,
                    sa.Fee2           AS fee2,
                    sa.Fee3           AS fee3,
                    sa.Fee4           AS fee4,
                    sa.Fee5           AS fee5
                FROM AtStrategyAllocation sa
                JOIN AtTradeHist th
                    ON th.ID = sa.TradeHistID
                WHERE th.TradeDate >= ?
                  AND th.TradeDate <= ?
                ORDER BY th.ClOrdID, sa.AllocID
                """;

        return querySql(sql, startDate, endDate);
    }

    public List<Map<String, Object>> fetchHistoricalAllocations(
            String startDate,
            String endDate) {

        String sql = """
                SELECT
                    th.ClOrdID        AS tradeClOrdID,
                    sa.AllocID        AS AllocID,
                    sa.AccountID      AS AccountID,
                    sa.Quantity       AS Quantity,
                    sa.Quantity       AS FilledQty,
                    sa.NetMoney       AS NetMoney,
                    sa.Comm1          AS Comm1,
                    sa.Comm2          AS Comm2,
                    sa.Comm3          AS Comm3,
                    sa.Comm4          AS Comm4,
                    sa.Comm5          AS Comm5,
                    sa.Fee1           AS Fee1,
                    sa.Fee2           AS Fee2,
                    sa.Fee3           AS Fee3,
                    sa.Fee4           AS Fee4,
                    sa.Fee5           AS Fee5
                FROM AtStrategyAllocation sa
                JOIN AtTradeHist th
                    ON th.ID = sa.TradeHistID
                WHERE th.TradeDate >= ?
                  AND th.TradeDate <= ?
                ORDER BY th.ClOrdID, sa.AllocID
                """;

        return querySql(sql, startDate, endDate);
    }


}
