package com.swiftstock;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        LoginTest.class,
        AiRecommandTest.class,
        InventoryTest.class,
        ParameterizedInventoryTest.class,
})

public class AllTests {
}