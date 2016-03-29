package com.blackducksoftware.integration.hub.util;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class HubResourceBundleHelperTest {
    @Test
    public void testGettingRiskReportKeyWithDefaultLocale() {
        HubResourceBundleHelper helper = new HubResourceBundleHelper();
        String high = helper.getRiskReportKey("entry.high");
        assertEquals("High", high);
    }

    @Test
    public void testGettingRiskReportKeyWithUSLocale() {
        HubResourceBundleHelper helper = new HubResourceBundleHelper();
        String high = helper.getRiskReportKey("entry.high", Locale.US);
        assertEquals("High", high);
    }

    @Test
    public void testGettingRiskReportKeyForQaLocale() {
        HubResourceBundleHelper helper = new HubResourceBundleHelper();
        String high = helper.getRiskReportKey("entry.high", new Locale("qa", "QA"));
        assertEquals("!!**HHiigghh**!!", high);
    }

}
