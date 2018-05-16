package com.blackducksoftware.integration.hub.configuration

import org.junit.Assert
import org.junit.Test

class HubServerConfigBuilderTest {
    @Test
    void testSettingFromPropertiesMap() {
        def properties = [BLACKDUCK_HUB_URL: 'test url', BLACKDUCK_HUB_USERNAME: 'user', BLACKDUCK_HUB_PASSWORD: 'password']

        def hubServerConfigBuilder = new HubServerConfigBuilder()
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigProperty.URL))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigProperty.USERNAME))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigProperty.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        Assert.assertEquals('test url', hubServerConfigBuilder.values.get(HubServerConfigProperty.URL))
        Assert.assertEquals('user', hubServerConfigBuilder.values.get(HubServerConfigProperty.USERNAME))
        Assert.assertEquals('password', hubServerConfigBuilder.values.get(HubServerConfigProperty.PASSWORD))
    }

    @Test
    void testSettingFromPropertiesMapWithMixed() {
        def properties = [BLACKDUCK_HUB_URL: 'test url', "blackduck.hub.username": 'user', BLACKDUCK_HUB_PASSWORD: 'password']

        def hubServerConfigBuilder = new HubServerConfigBuilder()
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigProperty.URL))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigProperty.USERNAME))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigProperty.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        Assert.assertEquals('test url', hubServerConfigBuilder.values.get(HubServerConfigProperty.URL))
        Assert.assertEquals('user', hubServerConfigBuilder.values.get(HubServerConfigProperty.USERNAME))
        Assert.assertEquals('password', hubServerConfigBuilder.values.get(HubServerConfigProperty.PASSWORD))
    }
}
