package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.SnippetMatching;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;

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
        builder.snippetMatching(SnippetMatching.FULL_SNIPPET_MATCHING_ONLY);
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

    @Test
    public void testDryRunValueCorrect() {
        ScanBatchBuilder scanBatchBuilder = new ScanBatchBuilder();

        scanBatchBuilder.dryRun(false);
        assertFalse(scanBatchBuilder.isDryRun());

        scanBatchBuilder.additionalScanArguments("--dryRunWriteDir /test/");
        assertTrue(scanBatchBuilder.isDryRun());
    }

}
