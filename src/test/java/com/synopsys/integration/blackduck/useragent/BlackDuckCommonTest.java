package com.synopsys.integration.blackduck.useragent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class BlackDuckCommonTest {
    @Test
    public void testDefaultBlackDuckCommon() {
        UserAgentItem defaultUserAgentItem = BlackDuckCommon.createUserAgentItem();
        assertNotNull(defaultUserAgentItem.getProduct().getName());
        assertNotNull(defaultUserAgentItem.getProduct().getVersion());
        assertNotNull(defaultUserAgentItem.getComments());
    }

    @Test
    public void testSpecificBlackDuckCommon() {
        Map<String, String> properties = BlackDuckCommon.JAVA_PROPERTIES
                                             .stream()
                                             .collect(Collectors.toMap(Function.identity(), Function.identity()));

        BlackDuckCommon BlackDuckCommon = new BlackDuckCommon();
        UserAgentItem userAgentItem = BlackDuckCommon.createUserAgentItem("testVersion", properties::get);
        assertEquals(BlackDuckCommon.NAME, userAgentItem.getProduct().getName());
        assertEquals("testVersion", userAgentItem.getProduct().getVersion());

        String expectedComments = BlackDuckCommon.JAVA_PROPERTIES
                                      .stream()
                                      .collect(Collectors.joining(" "));
        assertEquals(expectedComments, userAgentItem.getComments().get());

        String expectedUserAgentString = String.format("%s/%s (%s)", BlackDuckCommon.NAME, "testVersion", expectedComments);
        assertEquals(expectedUserAgentString, userAgentItem.createUserAgentString());
    }

}
