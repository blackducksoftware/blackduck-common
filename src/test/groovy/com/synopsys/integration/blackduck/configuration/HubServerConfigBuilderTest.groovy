package com.synopsys.integration.blackduck.configuration

import org.junit.Assert
import org.junit.Test

class HubServerConfigBuilderTest {
    @Test
    void testSettingFromPropertiesMap() {
        def properties = [BLACKDUCK_HUB_URL: 'test url', BLACKDUCK_HUB_USERNAME: 'user', BLACKDUCK_HUB_PASSWORD: 'password']

        def hubServerConfigBuilder = new HubServerConfigBuilder()
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        Assert.assertEquals('test url', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        Assert.assertEquals('user', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        Assert.assertEquals('password', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))
    }

    @Test
    void testSettingFromPropertiesMapWithMixed() {
        def properties = [BLACKDUCK_HUB_URL: 'test url', "blackduck.hub.username": 'user', BLACKDUCK_HUB_PASSWORD: 'password']

        def hubServerConfigBuilder = new HubServerConfigBuilder()
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        Assert.assertEquals('test url', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        Assert.assertEquals('user', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        Assert.assertEquals('password', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))
    }

    @Test
    void testSettingFromPropertiesWithoutHubPiece() {
        def properties = [BLACKDUCK_URL: 'test url', "blackduck.username": 'user', BLACKDUCK_PASSWORD: 'password']

        def hubServerConfigBuilder = new HubServerConfigBuilder()
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        Assert.assertNull(hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))

        hubServerConfigBuilder.setFromProperties(properties)

        Assert.assertEquals('test url', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.URL))
        Assert.assertEquals('user', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.USERNAME))
        Assert.assertEquals('password', hubServerConfigBuilder.values.get(HubServerConfigBuilder.Property.PASSWORD))
    }

}
