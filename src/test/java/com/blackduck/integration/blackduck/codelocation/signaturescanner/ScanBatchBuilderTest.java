package com.blackduck.integration.blackduck.codelocation.signaturescanner;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.blackduck.integration.blackduck.codelocation.signaturescanner.command.SnippetMatching;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TimingExtension.class)
public class ScanBatchBuilderTest {
    @Test
    public void testValidBlackDuckScanConfig() {
        ScanBatchBuilder builder = createInitialValidBuilder();
        assertTrue(builder.isValid());
    }

    @Test
    public void testInvalidBlackDuckScanConfig() {
        ScanBatchBuilder builder = new ScanBatchBuilder();

        assertFalse(builder.isValid());
    }

    @Test
    public void testInvalidProxy() {
        ScanBatchBuilder builder = createInitialValidBuilder();
        try {
            builder.blackDuckUrl(new HttpUrl("http://www.google.com"));
        } catch (IntegrationException e) {
            fail("The test URL was bad.", e);
        }

        builder.blackDuckApiToken("apitoken");
        builder.proxyInfo(null);
        assertFalse(builder.isValid());

        builder.proxyInfo(ProxyInfo.NO_PROXY_INFO);
        assertTrue(builder.isValid());
    }

    @Test
    public void testCredentials() {
        ScanBatchBuilder builder = createInitialValidBuilder();
        try {
            builder.blackDuckUrl(new HttpUrl("http://www.google.com"));
        } catch (IntegrationException e) {
            fail("The test URL was bad.", e);
        }
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
    public void testUploadSourceWithLicenseSearch() {
        ScanBatchBuilder builder = createInitialValidBuilder();
        builder.uploadSource(true);
        builder.licenseSearch(true);

        assertTrue(builder.isValid());
    }

    @Test
    public void testUploadSourceWithSnippetMatching() {
        ScanBatchBuilder builder = createInitialValidBuilder();
        builder.uploadSource(true);
        builder.snippetMatching(SnippetMatching.SNIPPET_MATCHING_ONLY);
        assertTrue(builder.isValid());
    }

    private ScanBatchBuilder createInitialValidBuilder() {
        ScanBatchBuilder builder = new ScanBatchBuilder();
        try {
            File testScanFile = File.createTempFile("test_scan", null);
            testScanFile.deleteOnExit();
            builder.addTarget(ScanTarget.createBasicTarget(testScanFile.getCanonicalPath()));
        } catch (IOException e) {
            fail("Couldn't add the target", e);
        }

        return builder;
    }

}
