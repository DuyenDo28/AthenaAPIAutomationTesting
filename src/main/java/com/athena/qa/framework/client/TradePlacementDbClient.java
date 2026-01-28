package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;

import java.util.List;
import java.util.Map;

public class TradePlacementDbClient extends BaseDbClient {

    public TradePlacementDbClient(DbConfig dbConfig) {
        super(dbConfig);
    }

    public List<Map<String, Object>> fetchPlacements(boolean isCompressed) {
        return callProc("{ call dbo.ssp_get_intraday_orders_placement_canonical(?) }", isCompressed);
    }

    /*public List<Map<String, Object>> fetchHistoricalPlacements(
            String startDate,
            String endDate) {

        String sql = """
                SELECT
                    th.ClOrdID     AS tradeClOrdID,
                    ph.ClOrdID     AS placementClOrdID,
                    ph.BrokerID    AS brokerId,
                    ph.OrderID     AS orderID,
                    ph.Status      AS status,
                    ph.Instruction AS instruction,
                    ph.Quantity    AS quantity,
                    ph.FilledQty   AS filledQty,
                    ph.FilledValue AS filledValue,
                    ph.LimitPrice  AS limitPrice
                FROM AtPlacementHist ph
                JOIN AtTradeHist th
                    ON th.ID = ph.TradeHistID
                WHERE th.TradeDate >= ?
                  AND th.TradeDate <= ?
                ORDER BY th.ClOrdID, ph.ClOrdID
                """;

        return querySql(sql, startDate, endDate);
    } */

    /*public List<Map<String, Object>> fetchHistoricalPlacements(
            String startDate,
            String endDate) {

        String sql = """
                SELECT
                    th.ClOrdID     AS tradeClOrdID,
                    ph.ClOrdID     AS placementClOrdID,
                    ph.BrokerID    AS brokerId,
                    ph.OrderID     AS orderID,
                    ph.Status      AS status,
                    ph.Instruction AS instruction,
                    ph.Quantity    AS quantity,
                    ph.FilledQty   AS filledQty,
                    ph.FilledValue AS filledValue,
                    ph.LimitPrice  AS limitPrice
                FROM AtPlacementHist ph
                JOIN AtTradeHist th
                    ON th.ID = ph.TradeHistID
                WHERE th.TradeDate >= ?
                  AND th.TradeDate <= ?
                ORDER BY th.ClOrdID, ph.ClOrdID
                """;

        return querySql(sql, startDate, endDate);
    } */
    public List<Map<String, Object>> fetchHistoricalPlacements(
            String startDate,
            String endDate) {

        String sql = """
                SELECT
                    th.ClOrdID        AS tradeClOrdID,
                    ph.ClOrdID        AS placementClOrdID,
                    ph.OrderID        AS OrderID,
                    ph.Status         AS Status,
                    ph.FilledQty      AS FilledQty,
                    ph.FilledValue    AS FilledValue,
                    ph.Instruction    AS Instruction,
                    ph.TimeInForce    AS TimeInForce,
                    ph.LimitPrice     AS LimitPrice,
                    ph.StopPrice      AS StopPrice
                FROM AtPlacementHist ph
                JOIN AtTradeHist th
                    ON th.ID = ph.TradeHistID
                WHERE th.TradeDate >= ?
                  AND th.TradeDate <= ?
                ORDER BY th.ClOrdID, ph.ClOrdID
                """;

        return querySql(sql, startDate, endDate);
    }


}
