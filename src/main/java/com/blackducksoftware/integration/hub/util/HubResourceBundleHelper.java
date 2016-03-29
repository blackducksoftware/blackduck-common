package com.blackducksoftware.integration.hub.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class HubResourceBundleHelper {
    private static final String RESOURCE_BASE_NAME = "com.blackducksoftware.integration.hub.resources.HubResources";

    private Locale locale = Locale.US;

    private String keyPrefix;

    public String getString(String key) {
        if (null != keyPrefix) {
            key = keyPrefix + "." + key;
        }

        return ResourceBundle.getBundle(RESOURCE_BASE_NAME, locale).getString(key);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

}
