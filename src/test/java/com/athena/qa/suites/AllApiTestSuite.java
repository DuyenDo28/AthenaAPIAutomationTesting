package com.athena.qa.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.athena.qa.tests.createTrade",
        "com.athena.qa.tests.trade",
        "com.athena.qa.tests.position",
        "com.athena.qa.tests.positionhist",
        "com.athena.qa.tests.security",
        "com.athena.qa.tests.trade.compare.tradealloc",
        "com.athena.qa.tests.trade.compare.tradeplacement",
        "com.athena.qa.tests.TradeHist.Asyn",
        "com.athena.qa.tests.TradeHist.tradehistAsynFull"
})
public class AllApiTestSuite {
}
