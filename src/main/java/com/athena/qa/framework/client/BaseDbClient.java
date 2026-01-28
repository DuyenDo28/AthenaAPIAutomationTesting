package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseDbClient {

    protected final DbConfig dbConfig;

    protected BaseDbClient(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    protected List<Map<String, Object>> callProc(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(
                dbConfig.jdbcUrl(),
                dbConfig.user(),
                dbConfig.password());
             //{call dbo.ssp_get_historical_orders_data(?, ?)} from TradeHistDbClient
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // params=2 with TradeHistDbClient.fetch
            //Vòng lặp chạy 2 lần
            //Lần 1 (i = 0)
            //ps.setObject(1, Date.valueOf(startDate));
            //JDBC hiểu:
            //“Gán param số 1 của stored procedure = startDate”
            //Lần 2 (i = 1)
            //ps.setObject(2, Date.valueOf(endDate));
            //JDBC hiểu:“Gán param số 2 của stored procedure = endDate”
            //Sau vòng for
            //dbo.ssp_get_historical_orders_data(
            //    startDate = 2024-01-01,
            //    endDate   = 2024-01-31
            //)
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("DB error calling proc: " + sql, e);
        }

    }

    protected List<Map<String, Object>> querySql(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(
                dbConfig.jdbcUrl(),
                dbConfig.user(),
                dbConfig.password());
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String col = meta.getColumnLabel(i);
                    if (col == null || col.isBlank()) {
                        col = meta.getColumnName(i);
                    }
                    row.put(col, rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("DB error running SQL: " + sql, e);
        }
    }
}
