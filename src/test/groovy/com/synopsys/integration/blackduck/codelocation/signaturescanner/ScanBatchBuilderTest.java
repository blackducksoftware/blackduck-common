package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@ExtendWith(TimingExtension.class)
public class ScanBatchBuilderTest {
    @Test
    public void testValidBlackDuckScanConfig() throws Exception {
        ScanBatchBuilder builder = new ScanBatchBuilder();
        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));
        assertTrue(builder.isValid());
    }

    @Test
    public void testInvalidBlackDuckScanConfig() throws Exception {
        ScanBatchBuilder builder = new ScanBatchBuilder();

        assertFalse(builder.isValid());
    }

    @Test
    public void testInvalidProxy() throws Exception {
        ScanBatchBuilder builder = new ScanBatchBuilder();
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
        ScanBatchBuilder builder = new ScanBatchBuilder();
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

    @Test
    public void testUploadSourceWithoutSnippetFails() throws Exception {
        ScanBatchBuilder builder = new ScanBatchBuilder();

        builder.addTarget(ScanTarget.createBasicTarget(File.createTempFile("test_scan", null).getCanonicalPath()));
        builder.uploadSource(null, true);

        assertFalse(builder.isValid());
    }

}
