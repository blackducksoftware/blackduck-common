package com.synopsys.integration.blackduck.configuration

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class BlackDuckServerConfigBuilderTest {
    @Test
    void testSettingFromPropertiesMap() {
        def properties = [BLACKDUCK_URL: 'test url', BLACKDUCK_USERNAME: 'user', BLACKDUCK_PASSWORD: 'password']

        def hubServerConfigBuilder = new BlackDuckServerConfigBuilder()
        assertNull(hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.URL))
        assertNull(hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.USERNAME))
        assertNull(hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        assertEquals('test url', hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.URL))
        assertEquals('user', hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.USERNAME))
        assertEquals('password', hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.PASSWORD))
    }

    @Test
    void testSettingFromPropertiesMapWithMixed() {
        def properties = [BLACKDUCK_URL: 'test url', "blackduck.username": 'user', BLACKDUCK_PASSWORD: 'password']

        def hubServerConfigBuilder = new BlackDuckServerConfigBuilder()
        assertNull(hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.URL))
        assertNull(hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.USERNAME))
        assertNull(hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        assertEquals('test url', hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.URL))
        assertEquals('user', hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.USERNAME))
        assertEquals('password', hubServerConfigBuilder.values.get(BlackDuckServerConfigBuilder.Property.PASSWORD))
    }

}
