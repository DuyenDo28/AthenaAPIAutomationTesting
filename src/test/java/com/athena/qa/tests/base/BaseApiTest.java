package com.athena.qa.tests.base;

import com.athena.qa.framework.client.*;
import com.athena.qa.framework.config.ApiConfig;
import com.athena.qa.framework.config.DbConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

/*
Khi bạn run test, JUnit làm:
TradeHistDbApiCompareTests testInstance =
        new TradeHistDbApiCompareTests();
run @BeforeAll
run @Test
KHÔNG tạo thêm object nào khác

 So sánh trực quan
Nếu KHÔNG có PER_CLASS
new TradeHistDbApiCompareTests()
 → @BeforeEach
 → test()

new TradeHistDbApiCompareTests()
 → @BeforeEach
 → test()

Với PER_CLASS (my code )
new TradeHistDbApiCompareTests()
 → @BeforeAll setup()
 → test()

 */
//JUnit tạo 1 instance duy nhất của TradeHistDbApiCompareTests
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseApiTest {

    protected ApiConfig apiConfig;
    protected DbConfig dbConfig;

    protected AuthApiClient authApiClient;
    protected TokenProvider tokenProvider;
    protected SecurityApiClient securityApiClient;
    protected SecurityDbClient securityDbClient;
    protected TradeHistDbClient tradeHistDbClient;
    // ✅ THÊM FIELD NÀY
    protected TradeApiClient tradeApiClient;

    protected static final ObjectMapper FILE_MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    protected PositionApiClient positionApiClient;
    protected PositionDbClient positionDbClient;


    protected CreateTradeApiClient createTradeApiClient;
    // thêm field
    protected CreateTradeAsyncApiClient createTradeAsyncApiClient;


    @BeforeAll
    void setup() {
        apiConfig = ConfigLoader.loadApi();
        dbConfig = ConfigLoader.loadDb();

        authApiClient = new AuthApiClient(apiConfig);
        tokenProvider = new TokenProvider(authApiClient);

        securityApiClient = new SecurityApiClient(apiConfig, tokenProvider);
        securityDbClient = new SecurityDbClient(dbConfig);

        tradeHistDbClient = new TradeHistDbClient(dbConfig);
        // ✅ INIT TradeApiClient Ở ĐÂY
        tradeApiClient = new TradeApiClient(apiConfig, tokenProvider);

        positionApiClient = new PositionApiClient(apiConfig, tokenProvider);
        positionDbClient = new PositionDbClient(dbConfig);
        createTradeApiClient = new CreateTradeApiClient(apiConfig, tokenProvider);
        // ✅ client async mới
        createTradeAsyncApiClient = new CreateTradeAsyncApiClient(apiConfig, tokenProvider);


    }


    protected void writeJson(String path, Object data) {
        try {
            File f = new File(path);
            if (f.getParentFile() != null) {
                f.getParentFile().mkdirs();
            }
            FILE_MAPPER.writeValue(f, data);
            System.out.println("📄 Written: " + f.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON to " + path, e);
        }
    }
}
