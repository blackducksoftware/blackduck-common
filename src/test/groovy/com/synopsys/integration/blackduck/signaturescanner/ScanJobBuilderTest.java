package com.synopsys.integration.blackduck.signaturescanner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanJobBuilder;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ScanJobBuilderTest {
    @Test
    public void testValidHubScanConfig() throws Exception {
        final ScanJobBuilder builder = new ScanJobBuilder();
        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));

        assertTrue(builder.isValid());
    }

    @Test
    public void testInvalidHubScanConfig() throws Exception {
        final ScanJobBuilder builder = new ScanJobBuilder();

        assertFalse(builder.isValid());
    }

    @Test
    public void testInvalidProxy() throws Exception {
        final ScanJobBuilder builder = new ScanJobBuilder();
        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));
        builder.blackDuckUrl(new URL("http://www.google.com"));
        builder.blackDuckApiToken("apitoken");
        builder.shouldUseProxy(true);
        assertFalse(builder.isValid());

        builder.proxyInfo(ProxyInfo.NO_PROXY_INFO);
        assertTrue(builder.isValid());
    }

    @Test
    public void testCredentials() throws Exception {
        final ScanJobBuilder builder = new ScanJobBuilder();
        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));
        builder.blackDuckUrl(new URL("http://www.google.com"));
        assertFalse(builder.isValid());

        builder.blackDuckApiToken("apitoken");
        assertTrue(builder.isValid());
        builder.blackDuckApiToken(null);

        builder.blackDuckUsername("username");
        assertFalse(builder.isValid());

        builder.blackDuckPassword("password");
        assertTrue(builder.isValid());
    }

}
