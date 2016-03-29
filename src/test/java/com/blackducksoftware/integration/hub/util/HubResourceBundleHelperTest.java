package com.blackducksoftware.integration.hub.util;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class HubResourceBundleHelperTest {
    @Test
    public void testGettingRiskReportKeyWithUSLocale() {
        HubResourceBundleHelper helper = new HubResourceBundleHelper();
        helper.setKeyPrefix("hub.riskreport");
        String high = helper.getString("entry.high");
        assertEquals("High", high);
    }

    @Test
    public void testGettingRiskReportKeyForQaLocale() {
        HubResourceBundleHelper helper = new HubResourceBundleHelper();
        helper.setKeyPrefix("hub.riskreport");
        helper.setLocale(new Locale("qa", "QA"));
        String high = helper.getString("entry.high");
        assertEquals("!!**HHiigghh**!!", high);
    }

    @Test
    public void testGettingRiskReportKeyWithoutPrefix() {
        HubResourceBundleHelper helper = new HubResourceBundleHelper();
        helper.setLocale(new Locale("qa", "QA"));
        String high = helper.getString("hub.riskreport.entry.medium");
        assertEquals("!!**MMeeddiiuumm**!!", high);
    }

}
