package com.synopsys.integration.blackduck.signaturescanner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ScanBatchBuilderTest {
    @Test
    public void testValidHubScanConfig() throws Exception {
        final ScanBatchBuilder builder = new ScanBatchBuilder();
        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));
        assertTrue(builder.isValid());
    }

    @Test
    public void testInvalidHubScanConfig() throws Exception {
        final ScanBatchBuilder builder = new ScanBatchBuilder();

        assertFalse(builder.isValid());
    }

    @Test
    public void testInvalidProxy() throws Exception {
        final ScanBatchBuilder builder = new ScanBatchBuilder();
        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));
        builder.blackDuckUrl(new URL("http://www.google.com"));
        builder.blackDuckApiToken("apitoken");
        builder.proxyInfo(null);
        assertFalse(builder.isValid());

        builder.proxyInfo(ProxyInfo.NO_PROXY_INFO);
        assertTrue(builder.isValid());
    }

    @Test
    public void testCredentials() throws Exception {
        final ScanBatchBuilder builder = new ScanBatchBuilder();
        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));
        builder.blackDuckUrl(new URL("http://www.google.com"));
        builder.proxyInfo(ProxyInfo.NO_PROXY_INFO);
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
