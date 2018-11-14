package com.synopsys.integration.blackduck.configuration

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test

class HubServerConfigBuilderTest {
    @Test
    void testSettingFromPropertiesMap() {
        def properties = [BLACKDUCK_URL: 'test url', BLACKDUCK_USERNAME: 'user', BLACKDUCK_PASSWORD: 'password']

        def hubServerConfigBuilder = new HubServerConfigBuilder()
        assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        assertEquals('test url', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        assertEquals('user', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        assertEquals('password', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))
    }

    @Test
    void testSettingFromPropertiesMapWithMixed() {
        def properties = [BLACKDUCK_URL: 'test url', "blackduck.username": 'user', BLACKDUCK_PASSWORD: 'password']

        def hubServerConfigBuilder = new HubServerConfigBuilder()
        assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        assertEquals('test url', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        assertEquals('user', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        assertEquals('password', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))
    }

}
