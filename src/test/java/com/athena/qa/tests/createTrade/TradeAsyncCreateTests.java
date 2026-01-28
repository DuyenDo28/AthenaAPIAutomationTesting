package com.athena.qa.tests.createTrade;

import com.athena.qa.framework.config.TradeMultiConfig;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.base.ConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * End-to-end test cho /api/Trades/Async
 */
public class TradeAsyncCreateTests extends BaseApiTest {

    @Test
    void create_many_trades_async() {

        TradeMultiConfig cfg = ConfigLoader.loadTradeMulti();

        System.out.println(
                "Submitting async trades: count=" + cfg.count()
                        + ", tradeDate=" + cfg.tradeDate()
                        + ", settleDate=" + cfg.settleDate()
        );

        long start = System.currentTimeMillis();

        // submit async + lấy payload để dump
        List<Map<String, Object>> payloads =
                createTradeAsyncApiClient.createTradesAsyncAndReturnPayload(cfg);

        long end = System.currentTimeMillis();

        System.out.println("✅ Trades/Async submitted in "
                + (end - start) + " ms");

        // dump payload đã gửi backend
        writeJson("out/create_trades_async_payload.json", payloads);
    }
}
