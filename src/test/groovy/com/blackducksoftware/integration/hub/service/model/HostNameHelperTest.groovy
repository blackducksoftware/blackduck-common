package com.blackducksoftware.integration.hub.service.model

import org.apache.commons.lang3.StringUtils
import org.junit.Test

class HostNameHelperTest extends GroovyTestCase {
    private static final String EXPECTED_MESSAGE = 'You must provided the host name of the machine this is running on.';
    @Test
    void testNullHostName() {
        def exceptionMessage = shouldFail(IllegalArgumentException) { HostNameHelper.assertHostNamePopulated((String)null) }
        assertEquals EXPECTED_MESSAGE, exceptionMessage
    }

    @Test
    void testEmptyHostName() {
        def exceptionMessage = shouldFail(IllegalArgumentException) { HostNameHelper.assertHostNamePopulated('') }
        assertEquals EXPECTED_MESSAGE, exceptionMessage
    }

    @Test
    void testEmptyOptionalHostName() {
        def exceptionMessage = shouldFail(IllegalArgumentException) { HostNameHelper.assertHostNamePopulated(Optional.empty()) }
        assertEquals EXPECTED_MESSAGE, exceptionMessage
    }

    @Test
    void testPopulatedOptionalHostName() {
        HostNameHelper.assertHostNamePopulated(Optional.of("test"));
    }

    @Test
    void testPopulatedHostName() {
        HostNameHelper.assertHostNamePopulated("test");
    }

    void testGettingHostName() {
        assertTrue(StringUtils.isNotBlank(HostNameHelper.getMyHostName().get()))
    }
}