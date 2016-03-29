package com.blackducksoftware.integration.hub.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

public class HubResourceBundleHelper {
    private static final String RESOURCE_BASE_NAME = "com.blackducksoftware.integration.hub.resources.HubResources";

    public static void main(String[] args) {
        HubResourceBundleHelper helper = new HubResourceBundleHelper();
        helper.generateQaResources();
    }

    public String getRiskReportKey(String key, Locale locale) {
        return getBundle(locale).getString("hub.riskreport." + key);
    }

    public String getRiskReportKey(String key) {
        return getBundle().getString("hub.riskreport." + key);
    }

    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle(RESOURCE_BASE_NAME);
    }

    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(RESOURCE_BASE_NAME, locale);
    }

    private void generateQaResources() {
        ResourceBundle bundle = getBundle(Locale.US);
        Enumeration<String> keys = bundle.getKeys();
        StringBuilder content = new StringBuilder();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String message = bundle.getString(key);

            StringBuilder revisedMessage = new StringBuilder();
            revisedMessage.append("!!**");
            for (char c : message.toCharArray()) {
                revisedMessage.append(c);
                if (StringUtils.isAlphanumeric(String.valueOf(c)) && message.length() > 3) {
                    revisedMessage.append(c);
                }
            }
            revisedMessage.append("**!!");
            content.append(key + "=" + revisedMessage.toString() + "\n");
        }

        System.out.println(content);
    }

}
