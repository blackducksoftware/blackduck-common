/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.useragent;

import com.blackduck.integration.util.NameVersion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlackDuckCommon {
    public static final String NAME = "BlackDuckCommon";
    public static final String VERSION_RESOURCE_PATH = "com/blackduck/integration/blackduck/version.txt";
    public static final List<String> JAVA_PROPERTIES = Arrays.asList("java.vendor", "java.version", "os.arch", "os.name", "os.version");

    public static UserAgentItem createUserAgentItem() {
        String version = null;
        try (InputStream inputStream = BlackDuckCommon.class.getClassLoader().getResourceAsStream(VERSION_RESOURCE_PATH)) {
            if (inputStream != null) {
                version = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
            // version will just be 'unknown'
        }

        BlackDuckCommon blackDuckCommon = new BlackDuckCommon();
        return blackDuckCommon.createUserAgentItem(version, System::getProperty);
    }

    public UserAgentItem createUserAgentItem(String version, Function<String, String> valueLookup) {
        version = StringUtils.defaultString(StringUtils.trimToNull(version), UserAgentItem.UNKNOWN);
        NameVersion nameVersion = new NameVersion(NAME, version);

        String comments = JAVA_PROPERTIES
                              .stream()
                              .map(valueLookup)
                              .collect(Collectors.joining(" "));

        return new UserAgentItem(nameVersion, comments);
    }

}
