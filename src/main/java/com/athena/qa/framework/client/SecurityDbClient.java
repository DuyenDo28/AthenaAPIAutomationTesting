package com.athena.qa.framework.client;

import com.athena.qa.framework.config.DbConfig;

import java.util.List;
import java.util.Map;

public class SecurityDbClient extends BaseDbClient {

    public SecurityDbClient(DbConfig dbConfig) {
        super(dbConfig);
    }

    public List<Map<String, Object>> fetchSecurities() {
        return callProc("EXEC ssp_get_securities;");
    }

    public List<Map<String, Object>> fetchAllSecurity() {
        return querySql(
                "SELECT ID, Symbol FROM AtSecurity WHERE Symbol IS NOT NULL"
        );
    }
}
