package com.athena.qa.tests.createTrade;

import com.athena.qa.framework.config.TradeMultiConfig;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.base.ConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * End-to-end test:
 * - Create multi trades
 * - Log total execution time
 * - Dump payload JSON đã gửi lên backend
 */
public class TradeMultiCreateTests extends BaseApiTest {

    @Test
    void create_many_trades_multi() {

        TradeMultiConfig cfg = ConfigLoader.loadTradeMulti();

        System.out.println(
                "Creating trades: count=" + cfg.count()
                        + ", tradeDate=" + cfg.tradeDate()
                        + ", settleDate=" + cfg.settleDate()
                        + ", batchSize=" + cfg.batchSize()
        );

        long start = System.currentTimeMillis();

        //  gọi method mới để lấy payload
        List<Map<String, Object>> payloads =
                createTradeApiClient.createTradesMultiAndReturnPayload(cfg);

        long end = System.currentTimeMillis();

        long durationMs = end - start;

        System.out.println(" CreateTrades finished in "
                + durationMs + " ms (~" + (durationMs / 1000.0) + " seconds)");

        //  Dump JSON payload đã gửi
        writeJson("out/create_trades_payload.json", payloads);
    }


}
